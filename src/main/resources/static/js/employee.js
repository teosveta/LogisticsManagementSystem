/**
 * Logistics Management System - Employee Dashboard
 * Uses modular architecture following SOLID principles
 */

import { api, requireRole, getUser, formatDate, formatCurrency, getStatusClass } from './api.js';
import { showLoading, showError, showModal, closeModal, renderTable, renderShipmentsTable, renderSummaryCards, createSelectOptions, showNotification } from './ui.js';
import { validateShipmentForm, validateDifferent } from './validation.js';
import { initNavigation, getContentArea, getSummaryRow } from './navigation.js';

// Pricing info will be fetched from backend
let pricingInfo = null;

// ==========================================
// STATE
// ==========================================

let currentUser = null;
let cachedData = {
    companies: [],
    offices: [],
    employees: [],
    customers: [],
    shipments: []
};

// ==========================================
// INIT
// ==========================================

document.addEventListener('DOMContentLoaded', () => {
    if (!requireRole('EMPLOYEE')) return;

    currentUser = getUser();
    initNavigation(handleView);
    loadDashboard();
});

// ==========================================
// NAVIGATION HANDLER
// ==========================================

function handleView(view) {
    const viewHandlers = {
        'dashboard': loadDashboard,
        'register': renderRegisterShipment,
        'all': renderAllShipments,
        'companies': renderCompanies,
        'offices': renderOffices,
        'employees': renderEmployees,
        'clients': renderCustomers,
        'reports': renderReports,
        'config': renderConfiguration
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
        // Fetch dashboard metrics from backend - all calculations done server-side
        const [metrics, employees, customers] = await Promise.all([
            api.reports.dashboardMetrics(),
            api.employees.getAll(),
            api.customers.getAll()
        ]);

        Object.assign(cachedData, { employees, customers });

        summaryRow.innerHTML = renderSummaryCards([
            { title: 'Total Shipments', value: metrics.totalShipments },
            { title: 'Pending', value: metrics.pendingShipments },
            { title: 'Delivered', value: metrics.deliveredShipments },
            { title: 'Revenue (BGN)', value: parseFloat(metrics.totalRevenue).toFixed(2) }
        ]);

        contentArea.innerHTML = `
            <h3>Employee Dashboard</h3>
            <p style="color:#465c66;margin-bottom:16px">Welcome back, ${currentUser.username}! Use the sidebar to manage shipments, customers, and view reports.</p>
            <div class="dashboard-stats">
                <div class="stat-section">
                    <h4>Quick Stats</h4>
                    <ul>
                        <li>Employees: ${employees.length}</li>
                        <li>Customers: ${customers.length}</li>
                        <li>Active Shipments: ${metrics.pendingShipments}</li>
                    </ul>
                </div>
            </div>
            <div style="margin-top:20px">
                <button class="primary-btn" onclick="document.querySelector('[data-view=register]').click()">
                    Register New Shipment
                </button>
            </div>
        `;
    } catch (error) {
        console.error('Dashboard error:', error);
        summaryRow.innerHTML = '';
        showError(contentArea, `Error loading dashboard: ${error.message}`);
    }
}

// ==========================================
// SHIPMENT REGISTRATION
// ==========================================

async function renderRegisterShipment() {
    const contentArea = getContentArea();
    showLoading(contentArea, 'Loading form...');

    try {
        // Fetch pricing info from backend along with other data
        const [customers, offices, pricing] = await Promise.all([
            api.customers.getAll(),
            api.offices.getAll(),
            api.pricing.getInfo()
        ]);

        Object.assign(cachedData, { customers, offices });
        pricingInfo = pricing;

        const basePrice = parseFloat(pricing.basePrice).toFixed(2);
        const pricePerKg = parseFloat(pricing.pricePerKg).toFixed(2);
        const deliveryFee = parseFloat(pricing.addressDeliveryFee).toFixed(2);

        contentArea.innerHTML = `
            <h3>Register New Shipment</h3>
            <form id="shipmentForm" class="parcel-form active">
                <div class="form-row">
                    <div class="form-group">
                        <label>Sender (Customer)</label>
                        <select id="senderId" required>
                            ${createSelectOptions(customers, 'id', c => `${c.name || c.username} (${c.phone || 'No phone'})`, null, 'Select sender...')}
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Receiver (Customer)</label>
                        <select id="receiverId" required>
                            ${createSelectOptions(customers, 'id', c => `${c.name || c.username} (${c.phone || 'No phone'})`, null, 'Select receiver...')}
                        </select>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Origin Office</label>
                        <select id="originOfficeId" required>
                            ${createSelectOptions(offices, 'id', o => `${o.name} - ${o.address}`, null, 'Select origin office...')}
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Destination Office</label>
                        <select id="destinationOfficeId" required>
                            ${createSelectOptions(offices, 'id', o => `${o.name} - ${o.address}`, null, 'Select destination office...')}
                        </select>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Weight (kg)</label>
                        <input type="number" id="weight" step="0.01" min="0.01" max="10000" required placeholder="e.g. 2.5">
                    </div>
                    <div class="form-group">
                        <label>Delivery Type</label>
                        <select id="deliverToAddress" required>
                            <option value="false">To Office (Free)</option>
                            <option value="true">To Address (+${deliveryFee} BGN)</option>
                        </select>
                    </div>
                </div>
                <div class="form-group" id="addressGroup" style="display:none">
                    <label>Delivery Address</label>
                    <input type="text" id="deliveryAddress" placeholder="Enter delivery address">
                </div>
                <div class="price-preview" id="pricePreview">
                    <strong>Estimated Price:</strong> <span id="estimatedPrice">0.00</span> BGN
                    <small>(Base: ${basePrice} + Weight x ${pricePerKg} + Delivery Fee)</small>
                </div>
                <button type="submit" class="primary-btn">Register Shipment</button>
                <div id="formMessage"></div>
            </form>
        `;

        setupShipmentForm();
    } catch (error) {
        console.error('Load form error:', error);
        showError(contentArea, `Error loading form: ${error.message}`);
    }
}

function setupShipmentForm() {
    const form = document.getElementById('shipmentForm');
    const weightInput = document.getElementById('weight');
    const deliveryType = document.getElementById('deliverToAddress');
    const addressGroup = document.getElementById('addressGroup');
    const addressInput = document.getElementById('deliveryAddress');

    // Price preview calculation using backend pricing info
    // Note: This is just for UI preview - actual price is calculated by backend
    function updatePricePreview() {
        const weight = parseFloat(weightInput.value) || 0;
        const toAddress = deliveryType.value === 'true';

        if (!pricingInfo) return;

        const basePrice = parseFloat(pricingInfo.basePrice) || 0;
        const pricePerKg = parseFloat(pricingInfo.pricePerKg) || 0;
        const deliveryFee = toAddress ? (parseFloat(pricingInfo.addressDeliveryFee) || 0) : 0;

        const total = basePrice + (weight * pricePerKg) + deliveryFee;
        document.getElementById('estimatedPrice').textContent = total.toFixed(2);
    }

    deliveryType.addEventListener('change', () => {
        const toAddress = deliveryType.value === 'true';
        addressGroup.style.display = toAddress ? 'block' : 'none';
        addressInput.required = toAddress;
        updatePricePreview();
    });

    weightInput.addEventListener('input', updatePricePreview);

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const messageDiv = document.getElementById('formMessage');
        const toAddress = deliveryType.value === 'true';

        const shipmentData = {
            senderId: parseInt(document.getElementById('senderId').value),
            recipientId: parseInt(document.getElementById('receiverId').value),
            weight: parseFloat(weightInput.value)
        };

        if (toAddress) {
            shipmentData.deliveryAddress = addressInput.value;
        } else {
            shipmentData.deliveryOfficeId = parseInt(document.getElementById('destinationOfficeId').value);
        }

        const validation = validateDifferent(shipmentData.senderId, shipmentData.recipientId, 'Sender and receiver must be different.');
        if (!validation.isValid) {
            messageDiv.innerHTML = `<div class="error-message">${validation.message}</div>`;
            return;
        }

        try {
            messageDiv.innerHTML = '<div class="loading">Registering shipment...</div>';
            const result = await api.shipments.create(shipmentData);
            messageDiv.innerHTML = `<div class="success-message">Shipment registered successfully! ID: ${result.id}, Price: ${result.price} BGN</div>`;
            form.reset();
            document.getElementById('estimatedPrice').textContent = '0.00';
            showNotification('Shipment registered successfully!');
        } catch (error) {
            console.error('Registration error:', error);
            messageDiv.innerHTML = `<div class="error-message">Error: ${error.message}</div>`;
        }
    });
}

// ==========================================
// ALL SHIPMENTS
// ==========================================

async function renderAllShipments() {
    const contentArea = getContentArea();
    showLoading(contentArea, 'Loading shipments...');

    try {
        const [shipments, customers, offices] = await Promise.all([
            api.shipments.getAll(),
            api.customers.getAll(),
            api.offices.getAll()
        ]);

        Object.assign(cachedData, { shipments, customers, offices });

        contentArea.innerHTML = `
            <h3>All Shipments</h3>
            <div class="table-controls">
                <input type="text" id="shipmentSearch" placeholder="Search shipments..." class="search-input">
                <select id="statusFilter">
                    <option value="">All Statuses</option>
                    <option value="REGISTERED">Registered</option>
                    <option value="IN_TRANSIT">In Transit</option>
                    <option value="DELIVERED">Delivered</option>
                    <option value="CANCELLED">Cancelled</option>
                </select>
            </div>
            <div id="shipmentsTableContainer">
                ${renderShipmentsTable(shipments, { showStatusSelect: true, showActions: true })}
            </div>
            <div id="shipmentModal" class="modal" style="display:none"></div>
        `;

        document.getElementById('shipmentSearch').addEventListener('input', filterShipments);
        document.getElementById('statusFilter').addEventListener('change', filterShipments);
        setupStatusChangeHandlers();
        setupShipmentLinkHandlers();
    } catch (error) {
        console.error('Load shipments error:', error);
        showError(contentArea, `Error loading shipments: ${error.message}`);
    }
}

function filterShipments() {
    const search = document.getElementById('shipmentSearch').value.toLowerCase();
    const status = document.getElementById('statusFilter').value;

    // Simple client-side filtering for display purposes
    let filtered = cachedData.shipments;

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

    document.getElementById('shipmentsTableContainer').innerHTML = renderShipmentsTable(filtered, { showStatusSelect: true, showActions: true });
    setupStatusChangeHandlers();
    setupShipmentLinkHandlers();
}

function setupStatusChangeHandlers() {
    document.querySelectorAll('.status-select').forEach(select => {
        select.addEventListener('change', async (e) => {
            const id = e.target.dataset.id;
            const newStatus = e.target.value;
            if (!newStatus) return;

            try {
                await api.shipments.updateStatus(id, newStatus);
                showNotification(`Shipment ${id} status updated to ${newStatus}`);
                renderAllShipments();
            } catch (error) {
                console.error('Status update error:', error);
                showNotification(`Error: ${error.message}`, 'error');
                e.target.value = '';
            }
        });
    });
}

function setupShipmentLinkHandlers() {
    document.querySelectorAll('.shipment-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const id = parseInt(e.target.dataset.id);
            viewShipmentDetails(id);
        });
    });
}

// ==========================================
// SHIPMENT DETAILS VIEW
// ==========================================

window.viewShipmentDetails = async function(id) {
    const modal = document.getElementById('shipmentModal');
    if (!modal) return;

    showModal('shipmentModal', '<div class="modal-content"><div class="loading">Loading shipment details...</div></div>');

    try {
        const shipment = await api.shipments.getById(id);

        const content = `
            <div class="modal-content shipment-details">
                <h4>Shipment #${shipment.id} Details</h4>

                <div class="detail-section">
                    <h5>Status</h5>
                    <span class="status-pill ${getStatusClass(shipment.status)}">${shipment.status}</span>
                </div>

                <div class="detail-grid">
                    <div class="detail-section">
                        <h5>Sender</h5>
                        <p><strong>Name:</strong> ${shipment.senderName || 'N/A'}</p>
                        <p><strong>ID:</strong> ${shipment.senderId}</p>
                    </div>

                    <div class="detail-section">
                        <h5>Recipient</h5>
                        <p><strong>Name:</strong> ${shipment.receiverName || 'N/A'}</p>
                        <p><strong>ID:</strong> ${shipment.recipientId}</p>
                    </div>
                </div>

                <div class="detail-grid">
                    <div class="detail-section">
                        <h5>Origin</h5>
                        <p>${shipment.originOfficeName || 'N/A'}</p>
                    </div>

                    <div class="detail-section">
                        <h5>Destination</h5>
                        ${shipment.deliverToAddress
                            ? `<p><strong>Address:</strong> ${shipment.deliveryAddress}</p>`
                            : `<p><strong>Office:</strong> ${shipment.destinationOfficeName || 'N/A'}</p>`
                        }
                    </div>
                </div>

                <div class="detail-section">
                    <h5>Shipment Details</h5>
                    <p><strong>Weight:</strong> ${shipment.weight} kg</p>
                    <p><strong>Delivery Type:</strong> ${shipment.deliverToAddress ? 'To Address' : 'To Office'}</p>
                </div>

                <div class="detail-section price-breakdown">
                    <h5>Price Breakdown</h5>
                    <p><strong>Base Price:</strong> ${pricingInfo ? parseFloat(pricingInfo.basePrice).toFixed(2) : '5.00'} BGN</p>
                    <p><strong>Weight Fee:</strong> ${shipment.weight} kg x ${pricingInfo ? parseFloat(pricingInfo.pricePerKg).toFixed(2) : '2.00'} = ${(shipment.weight * (pricingInfo ? parseFloat(pricingInfo.pricePerKg) : 2)).toFixed(2)} BGN</p>
                    ${shipment.deliverToAddress ? `<p><strong>Address Delivery Fee:</strong> ${pricingInfo ? parseFloat(pricingInfo.addressDeliveryFee).toFixed(2) : '10.00'} BGN</p>` : ''}
                    <p class="total-price"><strong>Total Price:</strong> ${formatCurrency(shipment.price)} BGN</p>
                </div>

                <div class="detail-section">
                    <h5>Timestamps</h5>
                    <p><strong>Registered:</strong> ${formatDate(shipment.registeredAt)}</p>
                    <p><strong>Registered By:</strong> ${shipment.registeredByName || 'N/A'}</p>
                    ${shipment.deliveredAt ? `<p><strong>Delivered:</strong> ${formatDate(shipment.deliveredAt)}</p>` : ''}
                    <p><strong>Last Updated:</strong> ${formatDate(shipment.updatedAt)}</p>
                </div>

                <div class="modal-buttons">
                    ${shipment.status !== 'DELIVERED' && shipment.status !== 'CANCELLED' ?
                        `<button class="primary-btn" onclick="editShipment(${shipment.id})">Edit Shipment</button>` : ''}
                    <button class="btn-cancel" onclick="closeShipmentModal()">Close</button>
                </div>
            </div>
        `;

        showModal('shipmentModal', content);
    } catch (error) {
        console.error('Error loading shipment:', error);
        showModal('shipmentModal', `
            <div class="modal-content">
                <div class="error-message">Error loading shipment: ${error.message}</div>
                <div class="modal-buttons">
                    <button class="btn-cancel" onclick="closeShipmentModal()">Close</button>
                </div>
            </div>
        `);
    }
};

// ==========================================
// EDIT SHIPMENT
// ==========================================

window.editShipment = async function(id) {
    const shipment = cachedData.shipments.find(s => s.id === id);
    if (!shipment) {
        showNotification('Shipment not found', 'error');
        return;
    }

    if (shipment.status === 'DELIVERED' || shipment.status === 'CANCELLED') {
        showNotification('Cannot edit delivered or cancelled shipments', 'error');
        return;
    }

    // Ensure we have customers and offices data
    if (!cachedData.customers.length || !cachedData.offices.length) {
        const [customers, offices] = await Promise.all([
            api.customers.getAll(),
            api.offices.getAll()
        ]);
        Object.assign(cachedData, { customers, offices });
    }

    // Fetch pricing info if not available
    if (!pricingInfo) {
        pricingInfo = await api.pricing.getInfo();
    }

    const deliveryFee = parseFloat(pricingInfo.addressDeliveryFee).toFixed(2);

    const content = `
        <div class="modal-content">
            <h4>Edit Shipment #${shipment.id}</h4>
            <form id="editShipmentForm">
                <input type="hidden" id="editShipmentId" value="${shipment.id}">

                <div class="form-row">
                    <div class="form-group">
                        <label>Sender (Customer)</label>
                        <select id="editSenderId" required>
                            ${createSelectOptions(cachedData.customers, 'id', c => `${c.name || c.username} (${c.phone || 'No phone'})`, shipment.senderId, 'Select sender...')}
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Receiver (Customer)</label>
                        <select id="editReceiverId" required>
                            ${createSelectOptions(cachedData.customers, 'id', c => `${c.name || c.username} (${c.phone || 'No phone'})`, shipment.recipientId, 'Select receiver...')}
                        </select>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Weight (kg)</label>
                        <input type="number" id="editWeight" step="0.01" min="0.01" max="10000" required value="${shipment.weight}">
                    </div>
                    <div class="form-group">
                        <label>Delivery Type</label>
                        <select id="editDeliverToAddress" required>
                            <option value="false" ${!shipment.deliverToAddress ? 'selected' : ''}>To Office (Free)</option>
                            <option value="true" ${shipment.deliverToAddress ? 'selected' : ''}>To Address (+${deliveryFee} BGN)</option>
                        </select>
                    </div>
                </div>

                <div class="form-group" id="editDestinationOfficeGroup" style="display:${shipment.deliverToAddress ? 'none' : 'block'}">
                    <label>Destination Office</label>
                    <select id="editDestinationOfficeId">
                        ${createSelectOptions(cachedData.offices, 'id', o => `${o.name} - ${o.address}`, shipment.deliveryOfficeId, 'Select destination office...')}
                    </select>
                </div>

                <div class="form-group" id="editAddressGroup" style="display:${shipment.deliverToAddress ? 'block' : 'none'}">
                    <label>Delivery Address</label>
                    <input type="text" id="editDeliveryAddress" value="${shipment.deliveryAddress || ''}" placeholder="Enter delivery address">
                </div>

                <div class="price-preview" id="editPricePreview">
                    <strong>Estimated Price:</strong> <span id="editEstimatedPrice">${formatCurrency(shipment.price)}</span> BGN
                    <small>(Price will be recalculated on save)</small>
                </div>

                <div class="modal-buttons">
                    <button type="submit" class="primary-btn">Save Changes</button>
                    <button type="button" class="btn-cancel" onclick="closeShipmentModal()">Cancel</button>
                </div>
            </form>
        </div>
    `;

    showModal('shipmentModal', content);
    setupEditShipmentForm(shipment);
};

function setupEditShipmentForm(originalShipment) {
    const form = document.getElementById('editShipmentForm');
    const weightInput = document.getElementById('editWeight');
    const deliveryType = document.getElementById('editDeliverToAddress');
    const addressGroup = document.getElementById('editAddressGroup');
    const addressInput = document.getElementById('editDeliveryAddress');
    const officeGroup = document.getElementById('editDestinationOfficeGroup');
    const officeSelect = document.getElementById('editDestinationOfficeId');

    function updateEditPricePreview() {
        const weight = parseFloat(weightInput.value) || 0;
        const toAddress = deliveryType.value === 'true';

        if (!pricingInfo) return;

        const basePrice = parseFloat(pricingInfo.basePrice) || 0;
        const pricePerKg = parseFloat(pricingInfo.pricePerKg) || 0;
        const deliveryFee = toAddress ? (parseFloat(pricingInfo.addressDeliveryFee) || 0) : 0;

        const total = basePrice + (weight * pricePerKg) + deliveryFee;
        document.getElementById('editEstimatedPrice').textContent = total.toFixed(2);
    }

    deliveryType.addEventListener('change', () => {
        const toAddress = deliveryType.value === 'true';
        addressGroup.style.display = toAddress ? 'block' : 'none';
        officeGroup.style.display = toAddress ? 'none' : 'block';
        addressInput.required = toAddress;
        officeSelect.required = !toAddress;
        updateEditPricePreview();
    });

    weightInput.addEventListener('input', updateEditPricePreview);

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const id = document.getElementById('editShipmentId').value;
        const toAddress = deliveryType.value === 'true';

        const shipmentData = {
            senderId: parseInt(document.getElementById('editSenderId').value),
            recipientId: parseInt(document.getElementById('editReceiverId').value),
            weight: parseFloat(weightInput.value)
        };

        if (toAddress) {
            shipmentData.deliveryAddress = addressInput.value;
        } else {
            shipmentData.deliveryOfficeId = parseInt(officeSelect.value);
        }

        const validation = validateDifferent(shipmentData.senderId, shipmentData.recipientId, 'Sender and receiver must be different.');
        if (!validation.isValid) {
            showNotification(validation.message, 'error');
            return;
        }

        try {
            await api.shipments.update(id, shipmentData);
            showNotification('Shipment updated successfully!');
            closeShipmentModal();
            renderAllShipments();
        } catch (error) {
            console.error('Update error:', error);
            showNotification(`Error: ${error.message}`, 'error');
        }
    });
}

// ==========================================
// DELETE SHIPMENT
// ==========================================

window.deleteShipment = async function(id) {
    const shipment = cachedData.shipments.find(s => s.id === id);

    if (!confirm(`Are you sure you want to delete Shipment #${id}?\n\nSender: ${shipment?.senderName || 'N/A'}\nReceiver: ${shipment?.receiverName || 'N/A'}\nStatus: ${shipment?.status || 'N/A'}\n\nThis action cannot be undone.`)) {
        return;
    }

    try {
        await api.shipments.delete(id);
        showNotification(`Shipment #${id} deleted successfully!`);
        renderAllShipments();
    } catch (error) {
        console.error('Delete error:', error);
        showNotification(`Error: ${error.message}`, 'error');
    }
};

window.closeShipmentModal = function() {
    closeModal('shipmentModal');
};

// ==========================================
// COMPANIES CRUD
// ==========================================

async function renderCompanies() {
    const contentArea = getContentArea();
    showLoading(contentArea, 'Loading companies...');

    try {
        const companies = await api.companies.getAll();
        cachedData.companies = companies;

        contentArea.innerHTML = `
            <h3>Companies</h3>
            <button class="primary-btn" id="addCompanyBtn">Add Company</button>
            <div id="companiesTableContainer">
                ${renderCompaniesTable(companies)}
            </div>
            <div id="companyModal" class="modal" style="display:none"></div>
        `;

        document.getElementById('addCompanyBtn').addEventListener('click', () => showCompanyModal());
    } catch (error) {
        console.error('Load companies error:', error);
        showError(contentArea, `Error loading companies: ${error.message}`);
    }
}

function renderCompaniesTable(companies) {
    return renderTable({
        columns: [
            { key: 'id', label: 'ID' },
            { key: 'name', label: 'Name' },
            { key: 'registrationNumber', label: 'Reg. Number', render: c => c.registrationNumber || '-' },
            { key: 'address', label: 'Address', render: c => c.address || '-' },
            { key: 'phone', label: 'Phone', render: c => c.phone || '-' },
            { key: 'email', label: 'Email', render: c => c.email || '-' }
        ],
        data: companies,
        renderActions: c => `
            <button class="btn-sm btn-edit" onclick="editCompany(${c.id})">Edit</button>
            <button class="btn-sm btn-delete" onclick="deleteCompany(${c.id})">Delete</button>
        `,
        emptyMessage: 'No companies found.'
    });
}

window.showCompanyModal = function(company = null) {
    const isEdit = company !== null;
    const content = `
        <div class="modal-content">
            <h4>${isEdit ? 'Edit' : 'Add'} Company</h4>
            <form id="companyForm">
                <input type="hidden" id="companyId" value="${company?.id || ''}">
                <div class="form-group">
                    <label>Company Name *</label>
                    <input type="text" id="companyName" value="${company?.name || ''}" required minlength="2">
                </div>
                <div class="form-group">
                    <label>Registration Number *</label>
                    <input type="text" id="companyRegNumber" value="${company?.registrationNumber || ''}" required>
                </div>
                <div class="form-group">
                    <label>Address</label>
                    <input type="text" id="companyAddress" value="${company?.address || ''}" placeholder="Company address">
                </div>
                <div class="form-group">
                    <label>Phone</label>
                    <input type="tel" id="companyPhone" value="${company?.phone || ''}" placeholder="Phone number">
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" id="companyEmail" value="${company?.email || ''}" placeholder="Email address">
                </div>
                <div class="modal-buttons">
                    <button type="submit" class="primary-btn">${isEdit ? 'Update' : 'Create'}</button>
                    <button type="button" class="btn-cancel" onclick="closeCompanyModal()">Cancel</button>
                </div>
            </form>
        </div>
    `;

    showModal('companyModal', content);
    setupCompanyFormHandler(isEdit);
};

function setupCompanyFormHandler(isEdit) {
    document.getElementById('companyForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('companyId').value;
        const data = {
            name: document.getElementById('companyName').value.trim(),
            registrationNumber: document.getElementById('companyRegNumber').value.trim(),
            address: document.getElementById('companyAddress').value.trim() || null,
            phone: document.getElementById('companyPhone').value.trim() || null,
            email: document.getElementById('companyEmail').value.trim() || null
        };

        try {
            if (isEdit) {
                await api.companies.update(id, data);
                showNotification('Company updated successfully!');
            } else {
                await api.companies.create(data);
                showNotification('Company created successfully!');
            }
            closeCompanyModal();
            renderCompanies();
        } catch (error) {
            showNotification(`Error: ${error.message}`, 'error');
        }
    });
}

window.editCompany = function(id) {
    const company = cachedData.companies.find(c => c.id === id);
    if (company) showCompanyModal(company);
};

window.deleteCompany = async function(id) {
    if (!confirm('Are you sure you want to delete this company?')) return;
    try {
        await api.companies.delete(id);
        showNotification('Company deleted successfully!');
        renderCompanies();
    } catch (error) {
        showNotification(`Error: ${error.message}`, 'error');
    }
};

window.closeCompanyModal = function() {
    closeModal('companyModal');
};

// ==========================================
// OFFICES CRUD
// ==========================================

async function renderOffices() {
    const contentArea = getContentArea();
    showLoading(contentArea, 'Loading offices...');

    try {
        const [offices, companies] = await Promise.all([
            api.offices.getAll(),
            api.companies.getAll()
        ]);

        Object.assign(cachedData, { offices, companies });

        contentArea.innerHTML = `
            <h3>Offices</h3>
            <div class="table-controls">
                <button class="primary-btn" id="addOfficeBtn">Add Office</button>
                <select id="companyFilter" class="filter-select">
                    <option value="">All Companies</option>
                    ${companies.map(c => `<option value="${c.id}">${c.name}</option>`).join('')}
                </select>
                <input type="text" id="officeSearch" placeholder="Search offices..." class="search-input">
            </div>
            <div id="officesTableContainer">
                ${renderOfficesTable(offices)}
            </div>
            <div id="officeModal" class="modal" style="display:none"></div>
        `;

        document.getElementById('addOfficeBtn').addEventListener('click', () => showOfficeModal());
        document.getElementById('companyFilter').addEventListener('change', filterOffices);
        document.getElementById('officeSearch').addEventListener('input', filterOffices);
    } catch (error) {
        console.error('Load offices error:', error);
        showError(contentArea, `Error loading offices: ${error.message}`);
    }
}

async function filterOffices() {
    const companyId = document.getElementById('companyFilter').value;
    const searchTerm = document.getElementById('officeSearch').value.toLowerCase();

    let filtered;

    // If company is selected, use the backend API to filter by company
    if (companyId) {
        try {
            filtered = await api.offices.getByCompany(companyId);
        } catch (error) {
            console.error('Filter error:', error);
            filtered = cachedData.offices.filter(o => o.companyId === parseInt(companyId));
        }
    } else {
        filtered = cachedData.offices;
    }

    // Apply search filter
    if (searchTerm) {
        filtered = filtered.filter(o =>
            (o.name && o.name.toLowerCase().includes(searchTerm)) ||
            (o.address && o.address.toLowerCase().includes(searchTerm)) ||
            (o.city && o.city.toLowerCase().includes(searchTerm)) ||
            (o.companyName && o.companyName.toLowerCase().includes(searchTerm))
        );
    }

    document.getElementById('officesTableContainer').innerHTML = renderOfficesTable(filtered);
}

function renderOfficesTable(offices) {
    return renderTable({
        columns: [
            { key: 'id', label: 'ID' },
            { key: 'name', label: 'Name' },
            { key: 'address', label: 'Address' },
            { key: 'city', label: 'City', render: o => o.city || '-' },
            { key: 'country', label: 'Country', render: o => o.country || '-' },
            { key: 'phone', label: 'Phone', render: o => o.phone || '-' },
            { key: 'companyName', label: 'Company', render: o => o.companyName || 'N/A' }
        ],
        data: offices,
        renderActions: o => `
            <button class="btn-sm btn-edit" onclick="editOffice(${o.id})">Edit</button>
            <button class="btn-sm btn-delete" onclick="deleteOffice(${o.id})">Delete</button>
        `,
        emptyMessage: 'No offices found.'
    });
}

window.showOfficeModal = function(office = null) {
    const isEdit = office !== null;
    const content = `
        <div class="modal-content">
            <h4>${isEdit ? 'Edit' : 'Add'} Office</h4>
            <form id="officeForm">
                <input type="hidden" id="officeId" value="${office?.id || ''}">
                <div class="form-group">
                    <label>Office Name *</label>
                    <input type="text" id="officeName" value="${office?.name || ''}" required>
                </div>
                <div class="form-group">
                    <label>Address *</label>
                    <input type="text" id="officeAddress" value="${office?.address || ''}" required>
                </div>
                <div class="form-group">
                    <label>City *</label>
                    <input type="text" id="officeCity" value="${office?.city || ''}" required placeholder="e.g. Sofia">
                </div>
                <div class="form-group">
                    <label>Country *</label>
                    <input type="text" id="officeCountry" value="${office?.country || 'Bulgaria'}" required placeholder="e.g. Bulgaria">
                </div>
                <div class="form-group">
                    <label>Phone</label>
                    <input type="tel" id="officePhone" value="${office?.phone || ''}" placeholder="Office phone number">
                </div>
                <div class="form-group">
                    <label>Company *</label>
                    <select id="officeCompanyId" required>
                        ${createSelectOptions(cachedData.companies, 'id', 'name', office?.companyId, 'Select company...')}
                    </select>
                </div>
                <div class="modal-buttons">
                    <button type="submit" class="primary-btn">${isEdit ? 'Update' : 'Create'}</button>
                    <button type="button" class="btn-cancel" onclick="closeOfficeModal()">Cancel</button>
                </div>
            </form>
        </div>
    `;

    showModal('officeModal', content);
    setupOfficeFormHandler(isEdit);
};

function setupOfficeFormHandler(isEdit) {
    document.getElementById('officeForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('officeId').value;
        const data = {
            name: document.getElementById('officeName').value.trim(),
            address: document.getElementById('officeAddress').value.trim(),
            city: document.getElementById('officeCity').value.trim(),
            country: document.getElementById('officeCountry').value.trim(),
            phone: document.getElementById('officePhone').value.trim() || null,
            companyId: parseInt(document.getElementById('officeCompanyId').value)
        };

        try {
            if (isEdit) {
                await api.offices.update(id, data);
                showNotification('Office updated successfully!');
            } else {
                await api.offices.create(data);
                showNotification('Office created successfully!');
            }
            closeOfficeModal();
            renderOffices();
        } catch (error) {
            showNotification(`Error: ${error.message}`, 'error');
        }
    });
}

window.editOffice = function(id) {
    const office = cachedData.offices.find(o => o.id === id);
    if (office) showOfficeModal(office);
};

window.deleteOffice = async function(id) {
    if (!confirm('Are you sure you want to delete this office?')) return;
    try {
        await api.offices.delete(id);
        showNotification('Office deleted successfully!');
        renderOffices();
    } catch (error) {
        showNotification(`Error: ${error.message}`, 'error');
    }
};

window.closeOfficeModal = function() {
    closeModal('officeModal');
};

// ==========================================
// EMPLOYEES CRUD
// ==========================================

async function renderEmployees() {
    const contentArea = getContentArea();
    showLoading(contentArea, 'Loading employees...');

    try {
        const [employees, offices] = await Promise.all([
            api.employees.getAll(),
            api.offices.getAll()
        ]);

        Object.assign(cachedData, { employees, offices });

        contentArea.innerHTML = `
            <h3>Employees</h3>
            <button class="primary-btn" id="addEmployeeBtn">Add Employee</button>
            <div id="employeesTableContainer">
                ${renderEmployeesTable(employees)}
            </div>
            <div id="employeeModal" class="modal" style="display:none"></div>
        `;

        document.getElementById('addEmployeeBtn').addEventListener('click', () => showEmployeeModal());
    } catch (error) {
        console.error('Load employees error:', error);
        showError(contentArea, `Error loading employees: ${error.message}`);
    }
}

function renderEmployeesTable(employees) {
    return renderTable({
        columns: [
            { key: 'id', label: 'ID' },
            { key: 'name', label: 'Name' },
            { key: 'employeeType', label: 'Type', render: e => `<span class="type-badge type-${e.employeeType?.toLowerCase()}">${e.employeeType || 'N/A'}</span>` },
            { key: 'officeName', label: 'Office', render: e => e.officeName || 'N/A' }
        ],
        data: employees,
        renderActions: e => `
            <button class="btn-sm btn-edit" onclick="editEmployee(${e.id})">Edit</button>
            <button class="btn-sm btn-delete" onclick="deleteEmployee(${e.id})">Delete</button>
        `,
        emptyMessage: 'No employees found.'
    });
}

window.showEmployeeModal = function(employee = null) {
    const isEdit = employee !== null;
    const content = `
        <div class="modal-content">
            <h4>${isEdit ? 'Edit' : 'Add'} Employee</h4>
            <form id="employeeForm">
                <input type="hidden" id="employeeId" value="${employee?.id || ''}">
                <div class="form-group">
                    <label>Name</label>
                    <input type="text" id="employeeName" value="${employee?.name || ''}" required>
                </div>
                <div class="form-group">
                    <label>Employee Type</label>
                    <select id="employeeType" required>
                        <option value="COURIER" ${employee?.employeeType === 'COURIER' ? 'selected' : ''}>Courier</option>
                        <option value="OFFICE_STAFF" ${employee?.employeeType === 'OFFICE_STAFF' ? 'selected' : ''}>Office Staff</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Office</label>
                    <select id="employeeOfficeId" required>
                        ${createSelectOptions(cachedData.offices, 'id', o => `${o.name} - ${o.address}`, employee?.officeId, 'Select office...')}
                    </select>
                </div>
                ${!isEdit ? `
                <div class="form-group">
                    <label>Username (for login)</label>
                    <input type="text" id="employeeUsername" required minlength="3">
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" id="employeeEmail" required>
                </div>
                <div class="form-group">
                    <label>Password</label>
                    <input type="password" id="employeePassword" required minlength="6">
                </div>
                ` : ''}
                <div class="modal-buttons">
                    <button type="submit" class="primary-btn">${isEdit ? 'Update' : 'Create'}</button>
                    <button type="button" class="btn-cancel" onclick="closeEmployeeModal()">Cancel</button>
                </div>
            </form>
        </div>
    `;

    showModal('employeeModal', content);
    setupEmployeeFormHandler(isEdit, employee);
};

function setupEmployeeFormHandler(isEdit, employee) {
    document.getElementById('employeeForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('employeeId').value;

        try {
            if (isEdit) {
                const officeIdValue = document.getElementById('employeeOfficeId').value;
                const data = {
                    userId: employee.userId,
                    employeeType: document.getElementById('employeeType').value,
                    officeId: officeIdValue ? parseInt(officeIdValue) : null,
                    hireDate: employee.hireDate || new Date().toISOString().split('T')[0],
                    salary: employee.salary || 0
                };
                await api.employees.update(id, data);
                showNotification('Employee updated successfully!');
            } else {
                const username = document.getElementById('employeeUsername').value.trim();
                const email = document.getElementById('employeeEmail').value.trim();
                const password = document.getElementById('employeePassword').value;

                const authResponse = await api.auth.register(username, email, password, 'EMPLOYEE');
                const employees = await api.employees.getAll();
                const newEmployee = employees.find(emp => emp.userId === authResponse.userId);

                if (newEmployee) {
                    const officeIdValue = document.getElementById('employeeOfficeId').value;
                    const updateData = {
                        userId: authResponse.userId,
                        employeeType: document.getElementById('employeeType').value,
                        officeId: officeIdValue ? parseInt(officeIdValue) : null,
                        hireDate: new Date().toISOString().split('T')[0],
                        salary: 0
                    };
                    await api.employees.update(newEmployee.id, updateData);
                }
                showNotification('Employee created successfully!');
            }
            closeEmployeeModal();
            renderEmployees();
        } catch (error) {
            showNotification(`Error: ${error.message}`, 'error');
        }
    });
}

window.editEmployee = function(id) {
    const employee = cachedData.employees.find(e => e.id === id);
    if (employee) showEmployeeModal(employee);
};

window.deleteEmployee = async function(id) {
    if (!confirm('Are you sure you want to delete this employee?')) return;
    try {
        await api.employees.delete(id);
        showNotification('Employee deleted successfully!');
        renderEmployees();
    } catch (error) {
        showNotification(`Error: ${error.message}`, 'error');
    }
};

window.closeEmployeeModal = function() {
    closeModal('employeeModal');
};

// ==========================================
// CUSTOMERS CRUD
// ==========================================

async function renderCustomers() {
    const contentArea = getContentArea();
    showLoading(contentArea, 'Loading customers...');

    try {
        const customers = await api.customers.getAll();
        cachedData.customers = customers;

        contentArea.innerHTML = `
            <h3>Customers</h3>
            <button class="primary-btn" id="addCustomerBtn">Add Customer</button>
            <input type="text" id="customerSearch" placeholder="Search customers..." class="search-input" style="margin-left:10px">
            <div id="customersTableContainer">
                ${renderCustomersTable(customers)}
            </div>
            <div id="customerModal" class="modal" style="display:none"></div>
        `;

        document.getElementById('addCustomerBtn').addEventListener('click', () => showCustomerModal());
        document.getElementById('customerSearch').addEventListener('input', handleCustomerSearch);
    } catch (error) {
        console.error('Load customers error:', error);
        showError(contentArea, `Error loading customers: ${error.message}`);
    }
}

function handleCustomerSearch(e) {
    const searchTerm = e.target.value.toLowerCase();

    // Simple client-side filtering for display purposes
    let filtered = cachedData.customers;

    if (searchTerm) {
        filtered = filtered.filter(c => {
            const name = (c.name || c.username || '').toLowerCase();
            const phone = c.phone || '';
            const email = (c.email || '').toLowerCase();
            const address = (c.address || '').toLowerCase();
            return name.includes(searchTerm) ||
                   phone.includes(searchTerm) ||
                   email.includes(searchTerm) ||
                   address.includes(searchTerm);
        });
    }

    document.getElementById('customersTableContainer').innerHTML = renderCustomersTable(filtered);
}

function renderCustomersTable(customers) {
    return renderTable({
        columns: [
            { key: 'id', label: 'ID' },
            { key: 'name', label: 'Name', render: c => c.name || c.username || '-' },
            { key: 'email', label: 'Email', render: c => c.email || '-' },
            { key: 'phone', label: 'Phone', render: c => c.phone || 'Not provided' },
            { key: 'address', label: 'Address', render: c => c.address || 'Not provided' }
        ],
        data: customers,
        renderActions: c => `
            <button class="btn-sm btn-edit" onclick="editCustomer(${c.id})">Edit</button>
            <button class="btn-sm btn-delete" onclick="deleteCustomer(${c.id})">Delete</button>
            <button class="btn-sm btn-view" onclick="viewCustomerShipments(${c.id})">Shipments</button>
        `,
        emptyMessage: 'No customers found.'
    });
}

window.showCustomerModal = function(customer = null) {
    const isEdit = customer !== null;
    const content = `
        <div class="modal-content">
            <h4>${isEdit ? 'Edit' : 'Add'} Customer</h4>
            <form id="customerForm">
                <input type="hidden" id="customerId" value="${customer?.id || ''}">
                ${!isEdit ? `
                <div class="form-group">
                    <label>Username (for login)</label>
                    <input type="text" id="customerUsername" required minlength="3">
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" id="customerEmail" required>
                </div>
                <div class="form-group">
                    <label>Password</label>
                    <input type="password" id="customerPassword" required minlength="6">
                </div>
                ` : `
                <div class="form-group">
                    <label>Name</label>
                    <input type="text" id="customerName" value="${customer?.name || customer?.username || ''}" readonly disabled>
                    <small>Name cannot be changed</small>
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" id="customerEmail" value="${customer?.email || ''}" readonly disabled>
                    <small>Email cannot be changed</small>
                </div>
                `}
                <div class="form-group">
                    <label>Phone</label>
                    <input type="tel" id="customerPhone" value="${customer?.phone || ''}">
                </div>
                <div class="form-group">
                    <label>Address</label>
                    <input type="text" id="customerAddress" value="${customer?.address || ''}" placeholder="Customer address">
                </div>
                <div class="modal-buttons">
                    <button type="submit" class="primary-btn">${isEdit ? 'Update' : 'Create'}</button>
                    <button type="button" class="btn-cancel" onclick="closeCustomerModal()">Cancel</button>
                </div>
            </form>
        </div>
    `;

    showModal('customerModal', content);
    setupCustomerFormHandler(isEdit, customer);
};

function setupCustomerFormHandler(isEdit, customer) {
    document.getElementById('customerForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('customerId').value;

        try {
            if (!isEdit) {
                const username = document.getElementById('customerUsername').value.trim();
                const email = document.getElementById('customerEmail').value.trim();
                const password = document.getElementById('customerPassword').value;

                const authResponse = await api.auth.register(username, email, password, 'CUSTOMER');
                const customers = await api.customers.getAll();
                const newCustomer = customers.find(c => c.userId === authResponse.userId);

                if (newCustomer) {
                    const phone = document.getElementById('customerPhone').value.trim();
                    const address = document.getElementById('customerAddress').value.trim();

                    if (phone || address) {
                        const updateData = {
                            userId: authResponse.userId,
                            phone: phone || null,
                            address: address || null
                        };
                        await api.customers.update(newCustomer.id, updateData);
                    }
                }
                showNotification('Customer created successfully!');
            } else {
                const data = {
                    userId: customer.userId,
                    phone: document.getElementById('customerPhone').value.trim() || null,
                    address: document.getElementById('customerAddress').value.trim() || null
                };
                await api.customers.update(id, data);
                showNotification('Customer updated successfully!');
            }
            closeCustomerModal();
            renderCustomers();
        } catch (error) {
            showNotification(`Error: ${error.message}`, 'error');
        }
    });
}

window.editCustomer = function(id) {
    const customer = cachedData.customers.find(c => c.id === id);
    if (customer) showCustomerModal(customer);
};

window.deleteCustomer = async function(id) {
    if (!confirm('Are you sure you want to delete this customer?')) return;
    try {
        await api.customers.delete(id);
        showNotification('Customer deleted successfully!');
        renderCustomers();
    } catch (error) {
        showNotification(`Error: ${error.message}`, 'error');
    }
};

window.viewCustomerShipments = async function(customerId) {
    const contentArea = getContentArea();
    const customer = cachedData.customers.find(c => c.id === customerId);

    showLoading(contentArea, 'Loading customer shipments...');

    try {
        const [sent, received] = await Promise.all([
            api.reports.sentByCustomer(customerId),
            api.reports.receivedByCustomer(customerId)
        ]);

        contentArea.innerHTML = `
            <h3>Shipments for ${customer?.name || 'Customer'}</h3>
            <button class="btn-back" onclick="renderCustomers()">Back to Customers</button>

            <h4 style="margin-top:20px">Sent Shipments (${sent.length})</h4>
            ${sent.length > 0 ? renderShipmentSummaryTable(sent, 'sent') : '<p class="no-data">No sent shipments.</p>'}

            <h4 style="margin-top:20px">Received Shipments (${received.length})</h4>
            ${received.length > 0 ? renderShipmentSummaryTable(received, 'received') : '<p class="no-data">No received shipments.</p>'}
        `;
    } catch (error) {
        showError(contentArea, `Error: ${error.message}`);
    }
};

function renderShipmentSummaryTable(shipments, type) {
    const columns = type === 'sent'
        ? [
            { key: 'id', label: 'ID' },
            { key: 'receiverName', label: 'Receiver' },
            { key: 'destinationOfficeName', label: 'Destination' },
            { key: 'weight', label: 'Weight', render: s => `${s.weight} kg` },
            { key: 'price', label: 'Price', render: s => `${formatCurrency(s.price)} BGN` },
            { key: 'status', label: 'Status', render: s => `<span class="status-pill ${getStatusClass(s.status)}">${s.status}</span>` }
        ]
        : [
            { key: 'id', label: 'ID' },
            { key: 'senderName', label: 'Sender' },
            { key: 'originOfficeName', label: 'Origin' },
            { key: 'weight', label: 'Weight', render: s => `${s.weight} kg` },
            { key: 'price', label: 'Price', render: s => `${formatCurrency(s.price)} BGN` },
            { key: 'status', label: 'Status', render: s => `<span class="status-pill ${getStatusClass(s.status)}">${s.status}</span>` }
        ];

    return renderTable({ columns, data: shipments });
}

window.closeCustomerModal = function() {
    closeModal('customerModal');
};

window.renderCustomers = renderCustomers;

// ==========================================
// REPORTS
// ==========================================

async function renderReports() {
    const contentArea = getContentArea();
    const today = new Date();
    const thirtyDaysAgo = new Date(today);
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

    const formatDateInput = (date) => date.toISOString().split('T')[0];

    contentArea.innerHTML = `
        <h3>Reports</h3>

        <div class="report-section">
            <h4>Revenue Report</h4>
            <div class="report-controls">
                <label>From: <input type="date" id="revenueFromDate" value="${formatDateInput(thirtyDaysAgo)}"></label>
                <label>To: <input type="date" id="revenueToDate" value="${formatDateInput(today)}"></label>
                <button class="primary-btn" id="generateRevenueBtn">Generate Revenue Report</button>
            </div>
            <div id="revenueOutput" class="report-output"></div>
        </div>

        <div class="report-section">
            <h4>Quick Reports</h4>
            <div class="report-buttons">
                <button class="btn-report" id="reportAllEmployees">All Employees</button>
                <button class="btn-report" id="reportAllCustomers">All Customers</button>
                <button class="btn-report" id="reportAllShipments">All Shipments</button>
                <button class="btn-report" id="reportPendingShipments">Pending Shipments</button>
            </div>
            <div id="quickReportOutput" class="report-output"></div>
        </div>

        <div class="report-section">
            <h4>Shipments by Employee</h4>
            <div class="report-controls">
                <select id="employeeSelect">
                    <option value="">Loading employees...</option>
                </select>
                <button class="primary-btn" id="reportByEmployeeBtn">Show Shipments</button>
            </div>
            <div id="employeeReportOutput" class="report-output"></div>
        </div>
    `;

    try {
        const employees = await api.employees.getAll();
        document.getElementById('employeeSelect').innerHTML = `
            <option value="">Select employee...</option>
            ${employees.map(e => `<option value="${e.id}">${e.name}</option>`).join('')}
        `;
        cachedData.employees = employees;
    } catch (error) {
        console.error('Error loading employees:', error);
    }

    document.getElementById('generateRevenueBtn').addEventListener('click', generateRevenueReport);
    document.getElementById('reportAllEmployees').addEventListener('click', () => generateQuickReport('employees'));
    document.getElementById('reportAllCustomers').addEventListener('click', () => generateQuickReport('customers'));
    document.getElementById('reportAllShipments').addEventListener('click', () => generateQuickReport('shipments'));
    document.getElementById('reportPendingShipments').addEventListener('click', () => generateQuickReport('pending'));
    document.getElementById('reportByEmployeeBtn').addEventListener('click', generateEmployeeReport);
}

async function generateRevenueReport() {
    const output = document.getElementById('revenueOutput');
    const fromDate = document.getElementById('revenueFromDate').value;
    const toDate = document.getElementById('revenueToDate').value;

    if (!fromDate || !toDate) {
        output.innerHTML = '<div class="error-message">Please select both dates.</div>';
        return;
    }

    showLoading(output, 'Generating report...');

    try {
        const result = await api.reports.revenue(fromDate, toDate);
        output.innerHTML = `
            <div class="revenue-card">
                <h5>Revenue Summary</h5>
                <p><strong>Period:</strong> ${fromDate} to ${toDate}</p>
                <p><strong>Total Revenue:</strong> <span class="revenue-amount">${formatCurrency(result.totalRevenue)} BGN</span></p>
                <p><strong>Delivered Shipments:</strong> ${result.deliveredShipmentsCount || 0}</p>
            </div>
        `;
    } catch (error) {
        showError(output, `Error: ${error.message}`);
    }
}

async function generateQuickReport(type) {
    const output = document.getElementById('quickReportOutput');
    showLoading(output, 'Loading...');

    try {
        let data, title, columns;

        switch (type) {
            case 'employees':
                data = await api.reports.employees();
                title = 'All Employees';
                columns = [
                    { key: 'id', label: 'ID' },
                    { key: 'name', label: 'Name' },
                    { key: 'employeeType', label: 'Type' },
                    { key: 'officeName', label: 'Office', render: e => e.officeName || 'N/A' }
                ];
                break;
            case 'customers':
                data = await api.reports.customers();
                title = 'All Customers';
                columns = [
                    { key: 'id', label: 'ID' },
                    { key: 'name', label: 'Name' },
                    { key: 'phone', label: 'Phone' },
                    { key: 'email', label: 'Email', render: c => c.email || '-' }
                ];
                break;
            case 'shipments':
                data = await api.reports.shipments();
                title = 'All Shipments';
                columns = [
                    { key: 'id', label: 'ID' },
                    { key: 'senderName', label: 'Sender' },
                    { key: 'receiverName', label: 'Receiver' },
                    { key: 'weight', label: 'Weight', render: s => `${s.weight} kg` },
                    { key: 'price', label: 'Price', render: s => `${formatCurrency(s.price)} BGN` },
                    { key: 'status', label: 'Status', render: s => `<span class="status-pill ${getStatusClass(s.status)}">${s.status}</span>` }
                ];
                break;
            case 'pending':
                data = await api.reports.pendingShipments();
                title = 'Pending Shipments';
                columns = [
                    { key: 'id', label: 'ID' },
                    { key: 'senderName', label: 'Sender' },
                    { key: 'receiverName', label: 'Receiver' },
                    { key: 'originOfficeName', label: 'Origin' },
                    { key: 'destinationOfficeName', label: 'Destination' },
                    { key: 'status', label: 'Status', render: s => `<span class="status-pill ${getStatusClass(s.status)}">${s.status}</span>` }
                ];
                break;
        }

        output.innerHTML = `
            <h5>${title} (${data.length} records)</h5>
            ${renderTable({ columns, data, emptyMessage: 'No records found.' })}
        `;
    } catch (error) {
        showError(output, `Error: ${error.message}`);
    }
}

async function generateEmployeeReport() {
    const output = document.getElementById('employeeReportOutput');
    const employeeId = document.getElementById('employeeSelect').value;

    if (!employeeId) {
        output.innerHTML = '<div class="error-message">Please select an employee.</div>';
        return;
    }

    showLoading(output, 'Loading...');

    try {
        const data = await api.reports.shipmentsByEmployee(employeeId);
        const employee = cachedData.employees?.find(e => e.id === parseInt(employeeId));

        const columns = [
            { key: 'id', label: 'ID' },
            { key: 'senderName', label: 'Sender' },
            { key: 'receiverName', label: 'Receiver' },
            { key: 'weight', label: 'Weight', render: s => `${s.weight} kg` },
            { key: 'price', label: 'Price', render: s => `${formatCurrency(s.price)} BGN` },
            { key: 'status', label: 'Status', render: s => `<span class="status-pill ${getStatusClass(s.status)}">${s.status}</span>` },
            { key: 'registeredAt', label: 'Date', render: s => formatDate(s.registeredAt) }
        ];

        output.innerHTML = `
            <h5>Shipments registered by ${employee?.name || 'Employee'} (${data.length} records)</h5>
            ${renderTable({ columns, data, emptyMessage: 'No shipments found for this employee.' })}
        `;
    } catch (error) {
        showError(output, `Error: ${error.message}`);
    }
}

// ==========================================
// SYSTEM CONFIGURATION
// ==========================================

async function renderConfiguration() {
    const contentArea = getContentArea();
    const summaryRow = getSummaryRow();

    summaryRow.innerHTML = '';
    showLoading(contentArea, 'Loading configuration...');

    try {
        const config = await api.pricing.getConfig();

        contentArea.innerHTML = `
            <h3>System Configuration</h3>

            <div class="config-section">
                <h4>Pricing Configuration</h4>
                <p class="config-description">
                    Configure the pricing formula for shipments. Changes take effect immediately for new shipments.
                </p>

                <div class="pricing-config-card">
                    <div class="config-row">
                        <div class="config-item">
                            <label>Base Price (BGN)</label>
                            <div class="config-value">${parseFloat(config.basePrice).toFixed(2)}</div>
                            <small>Applied to every shipment</small>
                        </div>
                        <div class="config-item">
                            <label>Price per Kg (BGN)</label>
                            <div class="config-value">${parseFloat(config.pricePerKg).toFixed(2)}</div>
                            <small>Multiplied by shipment weight</small>
                        </div>
                        <div class="config-item">
                            <label>Address Delivery Fee (BGN)</label>
                            <div class="config-value">${parseFloat(config.addressDeliveryFee).toFixed(2)}</div>
                            <small>Added for home delivery</small>
                        </div>
                    </div>

                    <div class="pricing-formula">
                        <strong>Formula:</strong> Total = ${parseFloat(config.basePrice).toFixed(2)} + (Weight × ${parseFloat(config.pricePerKg).toFixed(2)}) + Delivery Fee
                    </div>

                    <div class="config-meta">
                        <small>Last updated: ${config.updatedAt ? formatDate(config.updatedAt) : 'N/A'}</small>
                    </div>

                    <button class="primary-btn" id="editPricingBtn">Edit Pricing</button>
                </div>
            </div>

            <div id="pricingModal" class="modal" style="display:none"></div>
        `;

        document.getElementById('editPricingBtn').addEventListener('click', () => showPricingModal(config));
    } catch (error) {
        console.error('Load config error:', error);
        showError(contentArea, `Error loading configuration: ${error.message}`);
    }
}

window.showPricingModal = function(config) {
    const content = `
        <div class="modal-content">
            <h4>Edit Pricing Configuration</h4>
            <form id="pricingForm">
                <div class="form-group">
                    <label>Base Price (BGN)</label>
                    <input type="number" id="basePrice" step="0.01" min="0" required
                           value="${parseFloat(config.basePrice).toFixed(2)}">
                    <small>Applied to every shipment</small>
                </div>
                <div class="form-group">
                    <label>Price per Kg (BGN)</label>
                    <input type="number" id="pricePerKg" step="0.01" min="0" required
                           value="${parseFloat(config.pricePerKg).toFixed(2)}">
                    <small>Multiplied by shipment weight</small>
                </div>
                <div class="form-group">
                    <label>Address Delivery Fee (BGN)</label>
                    <input type="number" id="addressDeliveryFee" step="0.01" min="0" required
                           value="${parseFloat(config.addressDeliveryFee).toFixed(2)}">
                    <small>Added for home delivery (office delivery = 0)</small>
                </div>

                <div class="pricing-preview" id="pricingPreview">
                    <strong>Example:</strong> 5kg shipment to address =
                    <span id="examplePrice">${(parseFloat(config.basePrice) + (5 * parseFloat(config.pricePerKg)) + parseFloat(config.addressDeliveryFee)).toFixed(2)}</span> BGN
                </div>

                <div class="modal-buttons">
                    <button type="submit" class="primary-btn">Save Changes</button>
                    <button type="button" class="btn-cancel" onclick="closePricingModal()">Cancel</button>
                </div>
            </form>
        </div>
    `;

    showModal('pricingModal', content);
    setupPricingForm();
};

function setupPricingForm() {
    const form = document.getElementById('pricingForm');
    const basePrice = document.getElementById('basePrice');
    const pricePerKg = document.getElementById('pricePerKg');
    const addressDeliveryFee = document.getElementById('addressDeliveryFee');
    const examplePrice = document.getElementById('examplePrice');

    function updateExample() {
        const base = parseFloat(basePrice.value) || 0;
        const perKg = parseFloat(pricePerKg.value) || 0;
        const fee = parseFloat(addressDeliveryFee.value) || 0;
        const example = base + (5 * perKg) + fee;
        examplePrice.textContent = example.toFixed(2);
    }

    basePrice.addEventListener('input', updateExample);
    pricePerKg.addEventListener('input', updateExample);
    addressDeliveryFee.addEventListener('input', updateExample);

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const data = {
            basePrice: parseFloat(basePrice.value),
            pricePerKg: parseFloat(pricePerKg.value),
            addressDeliveryFee: parseFloat(addressDeliveryFee.value)
        };

        try {
            await api.pricing.updateConfig(data);
            showNotification('Pricing configuration updated successfully!');
            closePricingModal();
            renderConfiguration();

            // Update the cached pricing info
            pricingInfo = await api.pricing.getInfo();
        } catch (error) {
            console.error('Update pricing error:', error);
            showNotification(`Error: ${error.message}`, 'error');
        }
    });
}

window.closePricingModal = function() {
    closeModal('pricingModal');
};
