/**
 * Logistics Management System - Customer Dashboard
 * View-only dashboard for customers to track their shipments
 * Uses modular architecture following SOLID principles
 */

import { api, requireRole, getUser, formatDate, formatCurrency, getStatusClass } from './api.js';
import { showLoading, showError, renderCustomerShipmentsTable, renderSummaryCards, renderTable } from './ui.js';
import { initNavigation, getContentArea, getSummaryRow } from './navigation.js';

// ==========================================
// UI HELPER FUNCTIONS
// ==========================================

/**
 * Merge sent and received shipments, removing duplicates by ID
 * This is a simple UI utility, not business logic
 */
function mergeShipments(sent, received) {
    const merged = [...sent];
    received.forEach(r => {
        if (!merged.find(s => s.id === r.id)) {
            merged.push(r);
        }
    });
    return merged;
}

/**
 * Sort shipments by registration date (newest first)
 */
function sortByDate(shipments) {
    return [...shipments].sort((a, b) =>
        new Date(b.registeredAt || 0) - new Date(a.registeredAt || 0)
    );
}

/**
 * Get recent shipments sorted by date
 */
function getRecentShipments(shipments, limit = 5) {
    return sortByDate(shipments).slice(0, limit);
}

/**
 * Get all shipments from sent and received lists, sorted by date
 */
function getAllShipmentsSorted(sent, received) {
    return sortByDate(mergeShipments(sent, received));
}

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
    if (!requireRole('CUSTOMER')) return;

    currentUser = getUser();
    initNavigation(handleView);

    await loadCustomerData();
    loadDashboard();
});

// ==========================================
// LOAD CUSTOMER DATA
// ==========================================

async function loadCustomerData() {
    try {
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
    const viewHandlers = {
        'dashboard': loadDashboard,
        'sent': renderSentShipments,
        'received': renderReceivedShipments,
        'expected': renderExpectedShipments,
        'all': renderAllShipments
    };

    const handler = viewHandlers[view];
    if (handler) handler();
}

// ==========================================
// DASHBOARD
// ==========================================

async function loadDashboard() {
    const summaryRow = getSummaryRow();
    const contentArea = getContentArea();

    showLoading(summaryRow, 'Loading summary...');
    showLoading(contentArea, 'Loading dashboard...');

    try {
        if (!customerData) {
            throw new Error('Customer profile not found. Please contact support.');
        }

        // Fetch metrics from backend and shipment lists for recent activity display
        const [metrics, sent, received] = await Promise.all([
            api.reports.customerMetrics(),
            api.reports.sentByCustomer(customerData.id),
            api.reports.receivedByCustomer(customerData.id)
        ]);

        cachedShipments.sent = sent;
        cachedShipments.received = received;
        cachedShipments.all = mergeShipments(sent, received);

        summaryRow.innerHTML = renderSummaryCards([
            { title: 'Sent', value: metrics.totalSent },
            { title: 'Received', value: metrics.totalReceived },
            { title: 'In Transit', value: metrics.inTransit },
            { title: 'Total Spent (BGN)', value: parseFloat(metrics.totalSpent).toFixed(2) }
        ]);

        contentArea.innerHTML = `
            <h3>Customer Dashboard</h3>
            <p style="color:#465c66;margin-bottom:16px">
                Welcome back, ${currentUser.username}! Here you can track all your shipments.
            </p>

            ${metrics.inTransit > 0 ? `
                <div class="alert alert-info">
                    You have <strong>${metrics.inTransit}</strong> shipment(s) currently in transit.
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
        showError(contentArea, `Error loading dashboard: ${error.message}`);
    }
}

// ==========================================
// HELPER FUNCTIONS
// ==========================================

function renderRecentActivity(sent, received) {
    const combined = mergeShipments(sent, received);
    const recent = getRecentShipments(combined, 5);

    if (recent.length === 0) {
        return '<p class="no-data">No recent activity.</p>';
    }

    return renderTable({
        columns: [
            { key: 'id', label: 'ID', render: s => `#${s.id}` },
            { key: 'type', label: 'Type', render: s => {
                const isSent = sent.find(item => item.id === s.id);
                return isSent
                    ? '<span class="type-sent">Sent</span>'
                    : '<span class="type-received">Incoming</span>';
            }},
            { key: 'fromTo', label: 'From/To', render: s => {
                const isSent = sent.find(item => item.id === s.id);
                return isSent ? s.receiverName : s.senderName;
            }},
            { key: 'status', label: 'Status', render: s => `<span class="status-pill ${getStatusClass(s.status)}">${s.status}</span>` },
            { key: 'registeredAt', label: 'Date', render: s => formatDate(s.registeredAt) }
        ],
        data: recent
    });
}

// ==========================================
// SENT SHIPMENTS
// ==========================================

async function renderSentShipments() {
    const contentArea = getContentArea();
    showLoading(contentArea, 'Loading sent shipments...');

    try {
        if (!customerData) {
            throw new Error('Customer profile not found.');
        }

        const shipments = await api.reports.sentByCustomer(customerData.id);
        cachedShipments.sent = shipments;

        contentArea.innerHTML = `
            <h3>Sent Shipments</h3>
            <p class="section-subtitle">Packages you have sent to others</p>
            ${renderCustomerShipmentsTable(shipments, 'sent')}
        `;
    } catch (error) {
        console.error('Error loading sent shipments:', error);
        showError(contentArea, `Error: ${error.message}`);
    }
}

// ==========================================
// RECEIVED SHIPMENTS
// ==========================================

async function renderReceivedShipments() {
    const contentArea = getContentArea();
    showLoading(contentArea, 'Loading received shipments...');

    try {
        if (!customerData) {
            throw new Error('Customer profile not found.');
        }

        const shipments = await api.reports.receivedByCustomer(customerData.id);
        cachedShipments.received = shipments;

        contentArea.innerHTML = `
            <h3>Received Shipments</h3>
            <p class="section-subtitle">Packages addressed to you</p>
            ${renderCustomerShipmentsTable(shipments, 'received')}
        `;
    } catch (error) {
        console.error('Error loading received shipments:', error);
        showError(contentArea, `Error: ${error.message}`);
    }
}

// ==========================================
// EXPECTED / IN TRANSIT SHIPMENTS
// ==========================================

async function renderExpectedShipments() {
    const contentArea = getContentArea();
    showLoading(contentArea, 'Loading shipments...');

    try {
        if (!customerData) {
            throw new Error('Customer profile not found.');
        }

        const [sent, received] = await Promise.all([
            api.reports.sentByCustomer(customerData.id),
            api.reports.receivedByCustomer(customerData.id)
        ]);

        const isInTransit = s => s.status === 'REGISTERED' || s.status === 'IN_TRANSIT';
        const inTransitSent = sent.filter(isInTransit);
        const inTransitReceived = received.filter(isInTransit);
        const inTransitAll = mergeShipments(inTransitSent, inTransitReceived);

        contentArea.innerHTML = `
            <h3>Expected / In Transit</h3>
            <p class="section-subtitle">Shipments that are currently being processed or delivered</p>

            ${inTransitSent.length > 0 ? `
                <h4 class="subsection-title">Your Sent Shipments in Transit (${inTransitSent.length})</h4>
                ${renderCustomerShipmentsTable(inTransitSent, 'sent')}
            ` : ''}

            ${inTransitReceived.length > 0 ? `
                <h4 class="subsection-title">Incoming Shipments (${inTransitReceived.length})</h4>
                ${renderCustomerShipmentsTable(inTransitReceived, 'received')}
            ` : ''}

            ${inTransitAll.length === 0 ? '<p class="no-data">No shipments in transit.</p>' : ''}
        `;
    } catch (error) {
        console.error('Error loading expected shipments:', error);
        showError(contentArea, `Error: ${error.message}`);
    }
}

// ==========================================
// ALL SHIPMENTS
// ==========================================

async function renderAllShipments() {
    const contentArea = getContentArea();
    showLoading(contentArea, 'Loading all shipments...');

    try {
        if (!customerData) {
            throw new Error('Customer profile not found.');
        }

        const [sent, received] = await Promise.all([
            api.reports.sentByCustomer(customerData.id),
            api.reports.receivedByCustomer(customerData.id)
        ]);

        const all = getAllShipmentsSorted(sent, received);

        Object.assign(cachedShipments, { sent, received, all });

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

        document.getElementById('searchInput').addEventListener('input', () => filterAllShipments(sent));
        document.getElementById('statusFilter').addEventListener('change', () => filterAllShipments(sent));
    } catch (error) {
        console.error('Error loading all shipments:', error);
        showError(contentArea, `Error: ${error.message}`);
    }
}

function filterAllShipments(sentList) {
    const search = document.getElementById('searchInput').value.toLowerCase();
    const status = document.getElementById('statusFilter').value;

    // Simple client-side filtering for display purposes
    let filtered = cachedShipments.all;

    if (search) {
        filtered = filtered.filter(s =>
            s.id.toString().includes(search) ||
            (s.senderName && s.senderName.toLowerCase().includes(search)) ||
            (s.receiverName && s.receiverName.toLowerCase().includes(search))
        );
    }

    if (status) {
        filtered = filtered.filter(s => s.status === status);
    }

    document.getElementById('shipmentsContainer').innerHTML = renderAllShipmentsTable(filtered, sentList);
}

function renderAllShipmentsTable(shipments, sentList) {
    if (shipments.length === 0) {
        return '<p class="no-data">No shipments found.</p>';
    }

    return renderTable({
        columns: [
            { key: 'id', label: 'ID', render: s => `#${s.id}` },
            { key: 'type', label: 'Type', render: s => {
                const isSent = sentList.find(sent => sent.id === s.id);
                return isSent
                    ? '<span class="type-sent">Sent</span>'
                    : '<span class="type-received">Incoming</span>';
            }},
            { key: 'senderName', label: 'Sender', render: s => s.senderName || 'N/A' },
            { key: 'receiverName', label: 'Receiver', render: s => s.receiverName || 'N/A' },
            { key: 'originOfficeName', label: 'Origin', render: s => s.originOfficeName || 'N/A' },
            { key: 'destination', label: 'Destination', render: s => s.deliverToAddress ? s.deliveryAddress : (s.destinationOfficeName || 'N/A') },
            { key: 'weight', label: 'Weight', render: s => `${s.weight} kg` },
            { key: 'price', label: 'Price', render: s => `${formatCurrency(s.price)} BGN` },
            { key: 'status', label: 'Status', render: s => `<span class="status-pill ${getStatusClass(s.status)}">${s.status}</span>` }
        ],
        data: shipments,
        emptyMessage: 'No shipments found.'
    });
}
