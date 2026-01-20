/**
 * Logistics Management System - Customer Dashboard
 * =================================================
 * View-only dashboard for customers to track their shipments
 * Customers can only view shipments they sent or will receive
 */

import { api, requireRole, logout, getUser, formatDate, formatCurrency, getStatusClass, showNotification } from './api.js';

// ==========================================
// STATE
// ==========================================

let currentUser = null;
let customerData = null;
let cachedShipments = {
    sent: [],
    received: [],
    all: []
};

// ==========================================
// INIT
// ==========================================

document.addEventListener('DOMContentLoaded', async () => {
    // Require CUSTOMER role
    if (!requireRole('CUSTOMER')) return;

    currentUser = getUser();
    initUI();
    setupNavigation();
    setupLogout();

    // Load customer data and dashboard
    await loadCustomerData();
    loadDashboard();
});

function initUI() {
    document.getElementById('userName').textContent = currentUser.username;
    document.getElementById('welcomeName').textContent = currentUser.username;
}

function setupNavigation() {
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            handleView(btn.dataset.view);
        });
    });

    // Mobile menu toggle
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', () => sidebar.classList.toggle('open'));
    }
}

function setupLogout() {
    document.getElementById('logoutBtn').addEventListener('click', () => {
        logout();
    });
}

// ==========================================
// LOAD CUSTOMER DATA
// ==========================================

async function loadCustomerData() {
    try {
        // Find customer record by user ID
        const customers = await api.customers.getAll();
        customerData = customers.find(c => c.userId === currentUser.userId);

        if (!customerData) {
            console.warn('Customer profile not found for user:', currentUser.userId);
        }
    } catch (error) {
        console.error('Error loading customer data:', error);
    }
}

// ==========================================
// NAVIGATION HANDLER
// ==========================================

function handleView(view) {
    switch (view) {
        case 'dashboard': loadDashboard(); break;
        case 'sent': renderSentShipments(); break;
        case 'received': renderReceivedShipments(); break;
        case 'expected': renderExpectedShipments(); break;
        case 'all': renderAllShipments(); break;
    }
}

// ==========================================
// DASHBOARD
// ==========================================

async function loadDashboard() {
    const summaryRow = document.getElementById('summaryRow');
    const contentArea = document.getElementById('contentArea');

    summaryRow.innerHTML = '<div class="loading">Loading summary...</div>';
    contentArea.innerHTML = '<div class="loading">Loading dashboard...</div>';

    try {
        if (!customerData) {
            throw new Error('Customer profile not found. Please contact support.');
        }

        // Load sent and received shipments in parallel
        const [sent, received] = await Promise.all([
            api.reports.sentByCustomer(customerData.id),
            api.reports.receivedByCustomer(customerData.id)
        ]);

        cachedShipments.sent = sent;
        cachedShipments.received = received;
        cachedShipments.all = [...sent, ...received.filter(r => !sent.find(s => s.id === r.id))];

        // Calculate stats
        const totalSent = sent.length;
        const totalReceived = received.filter(r => r.status === 'DELIVERED').length;
        const inTransit = [...sent, ...received].filter(s =>
            s.status === 'REGISTERED' || s.status === 'IN_TRANSIT'
        ).length;
        const totalSpent = sent.reduce((sum, s) => sum + parseFloat(s.price || 0), 0);

        // Render summary
        summaryRow.innerHTML = `
            <div class="sum-card"><h4>Sent</h4><p>${totalSent}</p></div>
            <div class="sum-card"><h4>Received</h4><p>${totalReceived}</p></div>
            <div class="sum-card"><h4>In Transit</h4><p>${inTransit}</p></div>
            <div class="sum-card"><h4>Total Spent (BGN)</h4><p>${totalSpent.toFixed(2)}</p></div>
        `;

        // Render dashboard content
        contentArea.innerHTML = `
            <h3>Customer Dashboard</h3>
            <p style="color:#465c66;margin-bottom:16px">
                Welcome back, ${currentUser.username}! Here you can track all your shipments.
            </p>

            ${inTransit > 0 ? `
                <div class="alert alert-info">
                    You have <strong>${inTransit}</strong> shipment(s) currently in transit.
                </div>
            ` : ''}

            <div class="recent-section">
                <h4>Recent Activity</h4>
                ${renderRecentActivity(sent, received)}
            </div>
        `;

    } catch (error) {
        console.error('Dashboard error:', error);
        summaryRow.innerHTML = '';
        contentArea.innerHTML = `<div class="error-message">Error loading dashboard: ${error.message}</div>`;
    }
}

function renderRecentActivity(sent, received) {
    // Combine and sort by date (most recent first)
    const combined = [...sent, ...received.filter(r => !sent.find(s => s.id === r.id))];
    const recent = combined
        .sort((a, b) => new Date(b.registeredAt || 0) - new Date(a.registeredAt || 0))
        .slice(0, 5);

    if (recent.length === 0) {
        return '<p class="no-data">No recent activity.</p>';
    }

    return `
        <table class="table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Type</th>
                    <th>From/To</th>
                    <th>Status</th>
                    <th>Date</th>
                </tr>
            </thead>
            <tbody>
                ${recent.map(s => {
                    const isSent = sent.find(sent => sent.id === s.id);
                    return `
                        <tr>
                            <td>#${s.id}</td>
                            <td>${isSent ? '<span class="type-sent">Sent</span>' : '<span class="type-received">Incoming</span>'}</td>
                            <td>${isSent ? s.receiverName : s.senderName}</td>
                            <td><span class="status-pill ${getStatusClass(s.status)}">${s.status}</span></td>
                            <td>${formatDate(s.registeredAt)}</td>
                        </tr>
                    `;
                }).join('')}
            </tbody>
        </table>
    `;
}

// ==========================================
// SENT SHIPMENTS
// ==========================================

async function renderSentShipments() {
    const contentArea = document.getElementById('contentArea');
    contentArea.innerHTML = '<div class="loading">Loading sent shipments...</div>';

    try {
        if (!customerData) {
            throw new Error('Customer profile not found.');
        }

        const shipments = await api.reports.sentByCustomer(customerData.id);
        cachedShipments.sent = shipments;

        contentArea.innerHTML = `
            <h3>Sent Shipments</h3>
            <p class="section-subtitle">Packages you have sent to others</p>
            ${renderShipmentsTable(shipments, 'sent')}
        `;

    } catch (error) {
        console.error('Error loading sent shipments:', error);
        contentArea.innerHTML = `<div class="error-message">Error: ${error.message}</div>`;
    }
}

// ==========================================
// RECEIVED SHIPMENTS
// ==========================================

async function renderReceivedShipments() {
    const contentArea = document.getElementById('contentArea');
    contentArea.innerHTML = '<div class="loading">Loading received shipments...</div>';

    try {
        if (!customerData) {
            throw new Error('Customer profile not found.');
        }

        const shipments = await api.reports.receivedByCustomer(customerData.id);
        cachedShipments.received = shipments;

        contentArea.innerHTML = `
            <h3>Received Shipments</h3>
            <p class="section-subtitle">Packages addressed to you</p>
            ${renderShipmentsTable(shipments, 'received')}
        `;

    } catch (error) {
        console.error('Error loading received shipments:', error);
        contentArea.innerHTML = `<div class="error-message">Error: ${error.message}</div>`;
    }
}

// ==========================================
// EXPECTED / IN TRANSIT SHIPMENTS
// ==========================================

async function renderExpectedShipments() {
    const contentArea = document.getElementById('contentArea');
    contentArea.innerHTML = '<div class="loading">Loading shipments...</div>';

    try {
        if (!customerData) {
            throw new Error('Customer profile not found.');
        }

        // Get both sent and received, filter for in-transit
        const [sent, received] = await Promise.all([
            api.reports.sentByCustomer(customerData.id),
            api.reports.receivedByCustomer(customerData.id)
        ]);

        const inTransitSent = sent.filter(s => s.status === 'REGISTERED' || s.status === 'IN_TRANSIT');
        const inTransitReceived = received.filter(s => s.status === 'REGISTERED' || s.status === 'IN_TRANSIT');

        // Remove duplicates (shipments that appear in both)
        const inTransitAll = [...inTransitSent];
        inTransitReceived.forEach(r => {
            if (!inTransitAll.find(s => s.id === r.id)) {
                inTransitAll.push(r);
            }
        });

        contentArea.innerHTML = `
            <h3>Expected / In Transit</h3>
            <p class="section-subtitle">Shipments that are currently being processed or delivered</p>

            ${inTransitSent.length > 0 ? `
                <h4 class="subsection-title">Your Sent Shipments in Transit (${inTransitSent.length})</h4>
                ${renderShipmentsTable(inTransitSent, 'sent')}
            ` : ''}

            ${inTransitReceived.length > 0 ? `
                <h4 class="subsection-title">Incoming Shipments (${inTransitReceived.length})</h4>
                ${renderShipmentsTable(inTransitReceived, 'received')}
            ` : ''}

            ${inTransitAll.length === 0 ? '<p class="no-data">No shipments in transit.</p>' : ''}
        `;

    } catch (error) {
        console.error('Error loading expected shipments:', error);
        contentArea.innerHTML = `<div class="error-message">Error: ${error.message}</div>`;
    }
}

// ==========================================
// ALL SHIPMENTS
// ==========================================

async function renderAllShipments() {
    const contentArea = document.getElementById('contentArea');
    contentArea.innerHTML = '<div class="loading">Loading all shipments...</div>';

    try {
        if (!customerData) {
            throw new Error('Customer profile not found.');
        }

        const [sent, received] = await Promise.all([
            api.reports.sentByCustomer(customerData.id),
            api.reports.receivedByCustomer(customerData.id)
        ]);

        // Combine without duplicates
        const all = [...sent];
        received.forEach(r => {
            if (!all.find(s => s.id === r.id)) {
                all.push(r);
            }
        });

        // Sort by date
        all.sort((a, b) => new Date(b.registeredAt || 0) - new Date(a.registeredAt || 0));

        cachedShipments.sent = sent;
        cachedShipments.received = received;
        cachedShipments.all = all;

        contentArea.innerHTML = `
            <h3>All Shipments</h3>
            <p class="section-subtitle">Complete history of your shipments</p>

            <div class="filter-controls">
                <input type="text" id="searchInput" placeholder="Search by ID or name..." class="search-input">
                <select id="statusFilter" class="filter-select">
                    <option value="">All Statuses</option>
                    <option value="REGISTERED">Registered</option>
                    <option value="IN_TRANSIT">In Transit</option>
                    <option value="DELIVERED">Delivered</option>
                    <option value="CANCELLED">Cancelled</option>
                </select>
            </div>

            <div id="shipmentsContainer">
                ${renderAllShipmentsTable(all, sent)}
            </div>
        `;

        // Setup filters
        document.getElementById('searchInput').addEventListener('input', () => filterAllShipments(sent));
        document.getElementById('statusFilter').addEventListener('change', () => filterAllShipments(sent));

    } catch (error) {
        console.error('Error loading all shipments:', error);
        contentArea.innerHTML = `<div class="error-message">Error: ${error.message}</div>`;
    }
}

function filterAllShipments(sentList) {
    const search = document.getElementById('searchInput').value.toLowerCase();
    const status = document.getElementById('statusFilter').value;

    let filtered = cachedShipments.all.filter(s => {
        const matchesSearch = !search ||
            s.id.toString().includes(search) ||
            (s.senderName && s.senderName.toLowerCase().includes(search)) ||
            (s.receiverName && s.receiverName.toLowerCase().includes(search));
        const matchesStatus = !status || s.status === status;
        return matchesSearch && matchesStatus;
    });

    document.getElementById('shipmentsContainer').innerHTML = renderAllShipmentsTable(filtered, sentList);
}

function renderAllShipmentsTable(shipments, sentList) {
    if (shipments.length === 0) {
        return '<p class="no-data">No shipments found.</p>';
    }

    return `
        <table class="table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Type</th>
                    <th>Sender</th>
                    <th>Receiver</th>
                    <th>Origin</th>
                    <th>Destination</th>
                    <th>Weight</th>
                    <th>Price</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                ${shipments.map(s => {
                    const isSent = sentList.find(sent => sent.id === s.id);
                    return `
                        <tr>
                            <td>#${s.id}</td>
                            <td>${isSent ? '<span class="type-sent">Sent</span>' : '<span class="type-received">Incoming</span>'}</td>
                            <td>${s.senderName || 'N/A'}</td>
                            <td>${s.receiverName || 'N/A'}</td>
                            <td>${s.originOfficeName || 'N/A'}</td>
                            <td>${s.deliverToAddress ? s.deliveryAddress : (s.destinationOfficeName || 'N/A')}</td>
                            <td>${s.weight} kg</td>
                            <td>${formatCurrency(s.price)} BGN</td>
                            <td><span class="status-pill ${getStatusClass(s.status)}">${s.status}</span></td>
                        </tr>
                    `;
                }).join('')}
            </tbody>
        </table>
    `;
}

// ==========================================
// SHARED TABLE RENDERER
// ==========================================

function renderShipmentsTable(shipments, type) {
    if (shipments.length === 0) {
        return '<p class="no-data">No shipments found.</p>';
    }

    const isSent = type === 'sent';

    return `
        <table class="table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>${isSent ? 'Receiver' : 'Sender'}</th>
                    <th>Origin</th>
                    <th>Destination</th>
                    <th>Weight</th>
                    <th>Price</th>
                    <th>Status</th>
                    <th>Date</th>
                </tr>
            </thead>
            <tbody>
                ${shipments.map(s => `
                    <tr>
                        <td>#${s.id}</td>
                        <td>${isSent ? (s.receiverName || 'N/A') : (s.senderName || 'N/A')}</td>
                        <td>${s.originOfficeName || 'N/A'}</td>
                        <td>${s.deliverToAddress ? s.deliveryAddress : (s.destinationOfficeName || 'N/A')}</td>
                        <td>${s.weight} kg</td>
                        <td>${formatCurrency(s.price)} BGN</td>
                        <td><span class="status-pill ${getStatusClass(s.status)}">${s.status}</span></td>
                        <td>${formatDate(s.registeredAt)}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}
