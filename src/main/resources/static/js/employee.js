/**
 * Logistics Management System - Employee Dashboard
 * =================================================
 * Full backend integration with JWT authentication
 * CRUD operations for all entities and reports
 */

import { api, requireRole, logout, getUser, formatDate, formatCurrency, getStatusClass, showNotification } from './api.js';

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
    // Require EMPLOYEE role
    if (!requireRole('EMPLOYEE')) return;

    currentUser = getUser();
    initUI();
    setupNavigation();
    setupLogout();
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
// NAVIGATION HANDLER
// ==========================================

function handleView(view) {
    switch (view) {
        case 'dashboard': loadDashboard(); break;
        case 'register': renderRegisterShipment(); break;
        case 'all': renderAllShipments(); break;
        case 'companies': renderCompanies(); break;
        case 'offices': renderOffices(); break;
        case 'employees': renderEmployees(); break;
        case 'clients': renderCustomers(); break;
        case 'reports': renderReports(); break;
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
        // Load all data in parallel
        const [shipments, employees, customers] = await Promise.all([
            api.shipments.getAll(),
            api.employees.getAll(),
            api.customers.getAll()
        ]);

        cachedData.shipments = shipments;
        cachedData.employees = employees;
        cachedData.customers = customers;

        // Render summary
        const pending = shipments.filter(s => s.status === 'REGISTERED' || s.status === 'IN_TRANSIT').length;
        const delivered = shipments.filter(s => s.status === 'DELIVERED').length;
        const totalRevenue = shipments.reduce((sum, s) => sum + parseFloat(s.price || 0), 0);

        summaryRow.innerHTML = `
            <div class="sum-card"><h4>Total Shipments</h4><p>${shipments.length}</p></div>
            <div class="sum-card"><h4>Pending</h4><p>${pending}</p></div>
            <div class="sum-card"><h4>Delivered</h4><p>${delivered}</p></div>
            <div class="sum-card"><h4>Revenue (BGN)</h4><p>${totalRevenue.toFixed(2)}</p></div>
        `;

        // Render dashboard content
        contentArea.innerHTML = `
            <h3>Employee Dashboard</h3>
            <p style="color:#465c66;margin-bottom:16px">Welcome back, ${currentUser.username}! Use the sidebar to manage shipments, customers, and view reports.</p>

            <div class="dashboard-stats">
                <div class="stat-section">
                    <h4>Quick Stats</h4>
                    <ul>
                        <li>Employees: ${employees.length}</li>
                        <li>Customers: ${customers.length}</li>
                        <li>Active Shipments: ${pending}</li>
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
        contentArea.innerHTML = `<div class="error-message">Error loading dashboard: ${error.message}</div>`;
    }
}

// ==========================================
// SHIPMENT REGISTRATION
// ==========================================

async function renderRegisterShipment() {
    const contentArea = document.getElementById('contentArea');
    contentArea.innerHTML = '<div class="loading">Loading form...</div>';

    try {
        // Load customers and offices for dropdowns
        const [customers, offices] = await Promise.all([
            api.customers.getAll(),
            api.offices.getAll()
        ]);

        cachedData.customers = customers;
        cachedData.offices = offices;

        contentArea.innerHTML = `
            <h3>Register New Shipment</h3>
            <form id="shipmentForm" class="parcel-form active">
                <div class="form-row">
                    <div class="form-group">
                        <label>Sender (Customer)</label>
                        <select id="senderId" required>
                            <option value="">Select sender...</option>
                            ${customers.map(c => `<option value="${c.id}">${c.name} (${c.phone})</option>`).join('')}
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Receiver (Customer)</label>
                        <select id="receiverId" required>
                            <option value="">Select receiver...</option>
                            ${customers.map(c => `<option value="${c.id}">${c.name} (${c.phone})</option>`).join('')}
                        </select>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Origin Office</label>
                        <select id="originOfficeId" required>
                            <option value="">Select origin office...</option>
                            ${offices.map(o => `<option value="${o.id}">${o.name} - ${o.address}</option>`).join('')}
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Destination Office</label>
                        <select id="destinationOfficeId" required>
                            <option value="">Select destination office...</option>
                            ${offices.map(o => `<option value="${o.id}">${o.name} - ${o.address}</option>`).join('')}
                        </select>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Weight (kg)</label>
                        <input type="number" id="weight" step="0.01" min="0.01" required placeholder="e.g. 2.5">
                    </div>
                    <div class="form-group">
                        <label>Delivery Type</label>
                        <select id="deliverToAddress" required>
                            <option value="false">To Office (Free)</option>
                            <option value="true">To Address (+10.00 BGN)</option>
                        </select>
                    </div>
                </div>

                <div class="form-group" id="addressGroup" style="display:none">
                    <label>Delivery Address</label>
                    <input type="text" id="deliveryAddress" placeholder="Enter delivery address">
                </div>

                <div class="price-preview" id="pricePreview">
                    <strong>Estimated Price:</strong> <span id="estimatedPrice">0.00</span> BGN
                    <small>(Base: 5.00 + Weight x 2.00 + Delivery Fee)</small>
                </div>

                <button type="submit" class="primary-btn">Register Shipment</button>
                <div id="formMessage"></div>
            </form>
        `;

        // Setup form handlers
        setupShipmentForm();

    } catch (error) {
        console.error('Load form error:', error);
        contentArea.innerHTML = `<div class="error-message">Error loading form: ${error.message}</div>`;
    }
}

function setupShipmentForm() {
    const form = document.getElementById('shipmentForm');
    const weightInput = document.getElementById('weight');
    const deliveryType = document.getElementById('deliverToAddress');
    const addressGroup = document.getElementById('addressGroup');
    const addressInput = document.getElementById('deliveryAddress');

    // Show/hide address field based on delivery type
    deliveryType.addEventListener('change', () => {
        const toAddress = deliveryType.value === 'true';
        addressGroup.style.display = toAddress ? 'block' : 'none';
        addressInput.required = toAddress;
        updatePricePreview();
    });

    // Update price preview when weight changes
    weightInput.addEventListener('input', updatePricePreview);

    function updatePricePreview() {
        const weight = parseFloat(weightInput.value) || 0;
        const toAddress = deliveryType.value === 'true';
        const basePrice = 5.00;
        const weightFee = weight * 2.00;
        const deliveryFee = toAddress ? 10.00 : 0;
        const total = basePrice + weightFee + deliveryFee;
        document.getElementById('estimatedPrice').textContent = total.toFixed(2);
    }

    // Form submission
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const messageDiv = document.getElementById('formMessage');

        const shipmentData = {
            senderId: parseInt(document.getElementById('senderId').value),
            receiverId: parseInt(document.getElementById('receiverId').value),
            originOfficeId: parseInt(document.getElementById('originOfficeId').value),
            destinationOfficeId: parseInt(document.getElementById('destinationOfficeId').value),
            weight: parseFloat(document.getElementById('weight').value),
            deliverToAddress: document.getElementById('deliverToAddress').value === 'true'
        };

        if (shipmentData.deliverToAddress) {
            shipmentData.deliveryAddress = document.getElementById('deliveryAddress').value;
        }

        // Validate sender != receiver
        if (shipmentData.senderId === shipmentData.receiverId) {
            messageDiv.innerHTML = '<div class="error-message">Sender and receiver must be different.</div>';
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
    const contentArea = document.getElementById('contentArea');
    contentArea.innerHTML = '<div class="loading">Loading shipments...</div>';

    try {
        const shipments = await api.shipments.getAll();
        cachedData.shipments = shipments;

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
                ${renderShipmentsTable(shipments)}
            </div>
        `;

        // Setup filters
        document.getElementById('shipmentSearch').addEventListener('input', filterShipments);
        document.getElementById('statusFilter').addEventListener('change', filterShipments);

    } catch (error) {
        console.error('Load shipments error:', error);
        contentArea.innerHTML = `<div class="error-message">Error loading shipments: ${error.message}</div>`;
    }
}

function renderShipmentsTable(shipments) {
    if (shipments.length === 0) {
        return '<p class="no-data">No shipments found.</p>';
    }

    return `
        <table class="table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Sender</th>
                    <th>Receiver</th>
                    <th>Origin</th>
                    <th>Destination</th>
                    <th>Weight</th>
                    <th>Price</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${shipments.map(s => `
                    <tr>
                        <td>${s.id}</td>
                        <td>${s.senderName || 'N/A'}</td>
                        <td>${s.receiverName || 'N/A'}</td>
                        <td>${s.originOfficeName || 'N/A'}</td>
                        <td>${s.deliverToAddress ? s.deliveryAddress : (s.destinationOfficeName || 'N/A')}</td>
                        <td>${s.weight} kg</td>
                        <td>${formatCurrency(s.price)} BGN</td>
                        <td><span class="status-pill ${getStatusClass(s.status)}">${s.status}</span></td>
                        <td>
                            <select class="status-select" data-id="${s.id}" ${s.status === 'DELIVERED' || s.status === 'CANCELLED' ? 'disabled' : ''}>
                                <option value="">Change status...</option>
                                <option value="IN_TRANSIT" ${s.status === 'IN_TRANSIT' ? 'disabled' : ''}>In Transit</option>
                                <option value="DELIVERED" ${s.status === 'DELIVERED' ? 'disabled' : ''}>Delivered</option>
                                <option value="CANCELLED" ${s.status === 'CANCELLED' ? 'disabled' : ''}>Cancelled</option>
                            </select>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

function filterShipments() {
    const search = document.getElementById('shipmentSearch').value.toLowerCase();
    const status = document.getElementById('statusFilter').value;

    let filtered = cachedData.shipments.filter(s => {
        const matchesSearch = !search ||
            (s.senderName && s.senderName.toLowerCase().includes(search)) ||
            (s.receiverName && s.receiverName.toLowerCase().includes(search)) ||
            s.id.toString().includes(search);
        const matchesStatus = !status || s.status === status;
        return matchesSearch && matchesStatus;
    });

    document.getElementById('shipmentsTableContainer').innerHTML = renderShipmentsTable(filtered);
    setupStatusChangeHandlers();
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
                renderAllShipments(); // Refresh the table
            } catch (error) {
                console.error('Status update error:', error);
                showNotification(`Error: ${error.message}`, 'error');
                e.target.value = ''; // Reset dropdown
            }
        });
    });
}

// ==========================================
// COMPANIES CRUD
// ==========================================

async function renderCompanies() {
    const contentArea = document.getElementById('contentArea');
    contentArea.innerHTML = '<div class="loading">Loading companies...</div>';

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
        contentArea.innerHTML = `<div class="error-message">Error loading companies: ${error.message}</div>`;
    }
}

function renderCompaniesTable(companies) {
    if (companies.length === 0) {
        return '<p class="no-data">No companies found.</p>';
    }

    return `
        <table class="table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${companies.map(c => `
                    <tr>
                        <td>${c.id}</td>
                        <td>${c.name}</td>
                        <td>
                            <button class="btn-sm btn-edit" onclick="editCompany(${c.id})">Edit</button>
                            <button class="btn-sm btn-delete" onclick="deleteCompany(${c.id})">Delete</button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

window.showCompanyModal = function(company = null) {
    const modal = document.getElementById('companyModal');
    const isEdit = company !== null;

    modal.innerHTML = `
        <div class="modal-content">
            <h4>${isEdit ? 'Edit' : 'Add'} Company</h4>
            <form id="companyForm">
                <input type="hidden" id="companyId" value="${company?.id || ''}">
                <div class="form-group">
                    <label>Company Name</label>
                    <input type="text" id="companyName" value="${company?.name || ''}" required minlength="2">
                </div>
                <div class="modal-buttons">
                    <button type="submit" class="primary-btn">${isEdit ? 'Update' : 'Create'}</button>
                    <button type="button" class="btn-cancel" onclick="closeCompanyModal()">Cancel</button>
                </div>
            </form>
        </div>
    `;

    modal.style.display = 'flex';

    document.getElementById('companyForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('companyId').value;
        const name = document.getElementById('companyName').value.trim();

        try {
            if (isEdit) {
                await api.companies.update(id, { name });
                showNotification('Company updated successfully!');
            } else {
                await api.companies.create({ name });
                showNotification('Company created successfully!');
            }
            closeCompanyModal();
            renderCompanies();
        } catch (error) {
            showNotification(`Error: ${error.message}`, 'error');
        }
    });
};

window.editCompany = async function(id) {
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
    document.getElementById('companyModal').style.display = 'none';
};

// ==========================================
// OFFICES CRUD
// ==========================================

async function renderOffices() {
    const contentArea = document.getElementById('contentArea');
    contentArea.innerHTML = '<div class="loading">Loading offices...</div>';

    try {
        const [offices, companies] = await Promise.all([
            api.offices.getAll(),
            api.companies.getAll()
        ]);

        cachedData.offices = offices;
        cachedData.companies = companies;

        contentArea.innerHTML = `
            <h3>Offices</h3>
            <button class="primary-btn" id="addOfficeBtn">Add Office</button>
            <div id="officesTableContainer">
                ${renderOfficesTable(offices)}
            </div>
            <div id="officeModal" class="modal" style="display:none"></div>
        `;

        document.getElementById('addOfficeBtn').addEventListener('click', () => showOfficeModal());

    } catch (error) {
        console.error('Load offices error:', error);
        contentArea.innerHTML = `<div class="error-message">Error loading offices: ${error.message}</div>`;
    }
}

function renderOfficesTable(offices) {
    if (offices.length === 0) {
        return '<p class="no-data">No offices found.</p>';
    }

    return `
        <table class="table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Address</th>
                    <th>Company</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${offices.map(o => `
                    <tr>
                        <td>${o.id}</td>
                        <td>${o.name}</td>
                        <td>${o.address}</td>
                        <td>${o.companyName || 'N/A'}</td>
                        <td>
                            <button class="btn-sm btn-edit" onclick="editOffice(${o.id})">Edit</button>
                            <button class="btn-sm btn-delete" onclick="deleteOffice(${o.id})">Delete</button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

window.showOfficeModal = function(office = null) {
    const modal = document.getElementById('officeModal');
    const isEdit = office !== null;

    modal.innerHTML = `
        <div class="modal-content">
            <h4>${isEdit ? 'Edit' : 'Add'} Office</h4>
            <form id="officeForm">
                <input type="hidden" id="officeId" value="${office?.id || ''}">
                <div class="form-group">
                    <label>Office Name</label>
                    <input type="text" id="officeName" value="${office?.name || ''}" required>
                </div>
                <div class="form-group">
                    <label>Address</label>
                    <input type="text" id="officeAddress" value="${office?.address || ''}" required>
                </div>
                <div class="form-group">
                    <label>Company</label>
                    <select id="officeCompanyId" required>
                        <option value="">Select company...</option>
                        ${cachedData.companies.map(c =>
                            `<option value="${c.id}" ${office?.companyId === c.id ? 'selected' : ''}>${c.name}</option>`
                        ).join('')}
                    </select>
                </div>
                <div class="modal-buttons">
                    <button type="submit" class="primary-btn">${isEdit ? 'Update' : 'Create'}</button>
                    <button type="button" class="btn-cancel" onclick="closeOfficeModal()">Cancel</button>
                </div>
            </form>
        </div>
    `;

    modal.style.display = 'flex';

    document.getElementById('officeForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('officeId').value;
        const data = {
            name: document.getElementById('officeName').value.trim(),
            address: document.getElementById('officeAddress').value.trim(),
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
};

window.editOffice = async function(id) {
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
    document.getElementById('officeModal').style.display = 'none';
};

// ==========================================
// EMPLOYEES CRUD
// ==========================================

async function renderEmployees() {
    const contentArea = document.getElementById('contentArea');
    contentArea.innerHTML = '<div class="loading">Loading employees...</div>';

    try {
        const [employees, offices] = await Promise.all([
            api.employees.getAll(),
            api.offices.getAll()
        ]);

        cachedData.employees = employees;
        cachedData.offices = offices;

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
        contentArea.innerHTML = `<div class="error-message">Error loading employees: ${error.message}</div>`;
    }
}

function renderEmployeesTable(employees) {
    if (employees.length === 0) {
        return '<p class="no-data">No employees found.</p>';
    }

    return `
        <table class="table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Office</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${employees.map(e => `
                    <tr>
                        <td>${e.id}</td>
                        <td>${e.name}</td>
                        <td><span class="type-badge type-${e.employeeType?.toLowerCase()}">${e.employeeType || 'N/A'}</span></td>
                        <td>${e.officeName || 'N/A'}</td>
                        <td>
                            <button class="btn-sm btn-edit" onclick="editEmployee(${e.id})">Edit</button>
                            <button class="btn-sm btn-delete" onclick="deleteEmployee(${e.id})">Delete</button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

window.showEmployeeModal = function(employee = null) {
    const modal = document.getElementById('employeeModal');
    const isEdit = employee !== null;

    modal.innerHTML = `
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
                        <option value="">Select office...</option>
                        ${cachedData.offices.map(o =>
                            `<option value="${o.id}" ${employee?.officeId === o.id ? 'selected' : ''}>${o.name} - ${o.address}</option>`
                        ).join('')}
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

    modal.style.display = 'flex';

    document.getElementById('employeeForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('employeeId').value;
        const data = {
            name: document.getElementById('employeeName').value.trim(),
            employeeType: document.getElementById('employeeType').value,
            officeId: parseInt(document.getElementById('employeeOfficeId').value)
        };

        if (!isEdit) {
            data.username = document.getElementById('employeeUsername').value.trim();
            data.email = document.getElementById('employeeEmail').value.trim();
            data.password = document.getElementById('employeePassword').value;
        }

        try {
            if (isEdit) {
                await api.employees.update(id, data);
                showNotification('Employee updated successfully!');
            } else {
                await api.employees.create(data);
                showNotification('Employee created successfully!');
            }
            closeEmployeeModal();
            renderEmployees();
        } catch (error) {
            showNotification(`Error: ${error.message}`, 'error');
        }
    });
};

window.editEmployee = async function(id) {
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
    document.getElementById('employeeModal').style.display = 'none';
};

// ==========================================
// CUSTOMERS CRUD
// ==========================================

async function renderCustomers() {
    const contentArea = document.getElementById('contentArea');
    contentArea.innerHTML = '<div class="loading">Loading customers...</div>';

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
        document.getElementById('customerSearch').addEventListener('input', (e) => {
            const search = e.target.value.toLowerCase();
            const filtered = cachedData.customers.filter(c =>
                c.name.toLowerCase().includes(search) ||
                c.phone.includes(search) ||
                (c.email && c.email.toLowerCase().includes(search))
            );
            document.getElementById('customersTableContainer').innerHTML = renderCustomersTable(filtered);
        });

    } catch (error) {
        console.error('Load customers error:', error);
        contentArea.innerHTML = `<div class="error-message">Error loading customers: ${error.message}</div>`;
    }
}

function renderCustomersTable(customers) {
    if (customers.length === 0) {
        return '<p class="no-data">No customers found.</p>';
    }

    return `
        <table class="table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Phone</th>
                    <th>Email</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${customers.map(c => `
                    <tr>
                        <td>${c.id}</td>
                        <td>${c.name}</td>
                        <td>${c.phone}</td>
                        <td>${c.email || '-'}</td>
                        <td>
                            <button class="btn-sm btn-edit" onclick="editCustomer(${c.id})">Edit</button>
                            <button class="btn-sm btn-delete" onclick="deleteCustomer(${c.id})">Delete</button>
                            <button class="btn-sm btn-view" onclick="viewCustomerShipments(${c.id})">Shipments</button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

window.showCustomerModal = function(customer = null) {
    const modal = document.getElementById('customerModal');
    const isEdit = customer !== null;

    modal.innerHTML = `
        <div class="modal-content">
            <h4>${isEdit ? 'Edit' : 'Add'} Customer</h4>
            <form id="customerForm">
                <input type="hidden" id="customerId" value="${customer?.id || ''}">
                <div class="form-group">
                    <label>Name</label>
                    <input type="text" id="customerName" value="${customer?.name || ''}" required>
                </div>
                <div class="form-group">
                    <label>Phone</label>
                    <input type="tel" id="customerPhone" value="${customer?.phone || ''}" required>
                </div>
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
                    <label>Email</label>
                    <input type="email" id="customerEmail" value="${customer?.email || ''}">
                </div>
                `}
                <div class="modal-buttons">
                    <button type="submit" class="primary-btn">${isEdit ? 'Update' : 'Create'}</button>
                    <button type="button" class="btn-cancel" onclick="closeCustomerModal()">Cancel</button>
                </div>
            </form>
        </div>
    `;

    modal.style.display = 'flex';

    document.getElementById('customerForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('customerId').value;
        const data = {
            name: document.getElementById('customerName').value.trim(),
            phone: document.getElementById('customerPhone').value.trim()
        };

        if (!isEdit) {
            data.username = document.getElementById('customerUsername').value.trim();
            data.email = document.getElementById('customerEmail').value.trim();
            data.password = document.getElementById('customerPassword').value;
        } else {
            data.email = document.getElementById('customerEmail').value.trim();
        }

        try {
            if (isEdit) {
                await api.customers.update(id, data);
                showNotification('Customer updated successfully!');
            } else {
                await api.customers.create(data);
                showNotification('Customer created successfully!');
            }
            closeCustomerModal();
            renderCustomers();
        } catch (error) {
            showNotification(`Error: ${error.message}`, 'error');
        }
    });
};

window.editCustomer = async function(id) {
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
    const contentArea = document.getElementById('contentArea');
    const customer = cachedData.customers.find(c => c.id === customerId);

    contentArea.innerHTML = '<div class="loading">Loading customer shipments...</div>';

    try {
        const [sent, received] = await Promise.all([
            api.reports.sentByCustomer(customerId),
            api.reports.receivedByCustomer(customerId)
        ]);

        contentArea.innerHTML = `
            <h3>Shipments for ${customer?.name || 'Customer'}</h3>
            <button class="btn-back" onclick="renderCustomers()">Back to Customers</button>

            <h4 style="margin-top:20px">Sent Shipments (${sent.length})</h4>
            ${sent.length > 0 ? `
                <table class="table">
                    <thead><tr><th>ID</th><th>Receiver</th><th>Destination</th><th>Weight</th><th>Price</th><th>Status</th></tr></thead>
                    <tbody>
                        ${sent.map(s => `
                            <tr>
                                <td>${s.id}</td>
                                <td>${s.receiverName}</td>
                                <td>${s.destinationOfficeName}</td>
                                <td>${s.weight} kg</td>
                                <td>${formatCurrency(s.price)} BGN</td>
                                <td><span class="status-pill ${getStatusClass(s.status)}">${s.status}</span></td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            ` : '<p class="no-data">No sent shipments.</p>'}

            <h4 style="margin-top:20px">Received Shipments (${received.length})</h4>
            ${received.length > 0 ? `
                <table class="table">
                    <thead><tr><th>ID</th><th>Sender</th><th>Origin</th><th>Weight</th><th>Price</th><th>Status</th></tr></thead>
                    <tbody>
                        ${received.map(s => `
                            <tr>
                                <td>${s.id}</td>
                                <td>${s.senderName}</td>
                                <td>${s.originOfficeName}</td>
                                <td>${s.weight} kg</td>
                                <td>${formatCurrency(s.price)} BGN</td>
                                <td><span class="status-pill ${getStatusClass(s.status)}">${s.status}</span></td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            ` : '<p class="no-data">No received shipments.</p>'}
        `;
    } catch (error) {
        contentArea.innerHTML = `<div class="error-message">Error: ${error.message}</div>`;
    }
};

window.closeCustomerModal = function() {
    document.getElementById('customerModal').style.display = 'none';
};

// Make renderCustomers available globally for back button
window.renderCustomers = renderCustomers;

// ==========================================
// REPORTS
// ==========================================

async function renderReports() {
    const contentArea = document.getElementById('contentArea');

    // Set default dates (last 30 days)
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

    // Load employees for dropdown
    try {
        const employees = await api.employees.getAll();
        const employeeSelect = document.getElementById('employeeSelect');
        employeeSelect.innerHTML = `
            <option value="">Select employee...</option>
            ${employees.map(e => `<option value="${e.id}">${e.name}</option>`).join('')}
        `;
    } catch (error) {
        console.error('Error loading employees:', error);
    }

    // Setup event handlers
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

    output.innerHTML = '<div class="loading">Generating report...</div>';

    try {
        const result = await api.reports.revenue(fromDate, toDate);
        output.innerHTML = `
            <div class="revenue-card">
                <h5>Revenue Summary</h5>
                <p><strong>Period:</strong> ${fromDate} to ${toDate}</p>
                <p><strong>Total Revenue:</strong> <span class="revenue-amount">${formatCurrency(result.totalRevenue)} BGN</span></p>
                <p><strong>Number of Shipments:</strong> ${result.shipmentCount || 'N/A'}</p>
            </div>
        `;
    } catch (error) {
        output.innerHTML = `<div class="error-message">Error: ${error.message}</div>`;
    }
}

async function generateQuickReport(type) {
    const output = document.getElementById('quickReportOutput');
    output.innerHTML = '<div class="loading">Loading...</div>';

    try {
        let data;
        let title;
        let tableHtml;

        switch (type) {
            case 'employees':
                data = await api.reports.employees();
                title = 'All Employees';
                tableHtml = `
                    <table class="table">
                        <thead><tr><th>ID</th><th>Name</th><th>Type</th><th>Office</th></tr></thead>
                        <tbody>
                            ${data.map(e => `<tr><td>${e.id}</td><td>${e.name}</td><td>${e.employeeType}</td><td>${e.officeName || 'N/A'}</td></tr>`).join('')}
                        </tbody>
                    </table>
                `;
                break;

            case 'customers':
                data = await api.reports.customers();
                title = 'All Customers';
                tableHtml = `
                    <table class="table">
                        <thead><tr><th>ID</th><th>Name</th><th>Phone</th><th>Email</th></tr></thead>
                        <tbody>
                            ${data.map(c => `<tr><td>${c.id}</td><td>${c.name}</td><td>${c.phone}</td><td>${c.email || '-'}</td></tr>`).join('')}
                        </tbody>
                    </table>
                `;
                break;

            case 'shipments':
                data = await api.reports.shipments();
                title = 'All Shipments';
                tableHtml = `
                    <table class="table">
                        <thead><tr><th>ID</th><th>Sender</th><th>Receiver</th><th>Weight</th><th>Price</th><th>Status</th></tr></thead>
                        <tbody>
                            ${data.map(s => `
                                <tr>
                                    <td>${s.id}</td>
                                    <td>${s.senderName}</td>
                                    <td>${s.receiverName}</td>
                                    <td>${s.weight} kg</td>
                                    <td>${formatCurrency(s.price)} BGN</td>
                                    <td><span class="status-pill ${getStatusClass(s.status)}">${s.status}</span></td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                `;
                break;

            case 'pending':
                data = await api.reports.pendingShipments();
                title = 'Pending Shipments';
                tableHtml = `
                    <table class="table">
                        <thead><tr><th>ID</th><th>Sender</th><th>Receiver</th><th>Origin</th><th>Destination</th><th>Status</th></tr></thead>
                        <tbody>
                            ${data.map(s => `
                                <tr>
                                    <td>${s.id}</td>
                                    <td>${s.senderName}</td>
                                    <td>${s.receiverName}</td>
                                    <td>${s.originOfficeName}</td>
                                    <td>${s.destinationOfficeName}</td>
                                    <td><span class="status-pill ${getStatusClass(s.status)}">${s.status}</span></td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                `;
                break;
        }

        output.innerHTML = `
            <h5>${title} (${data.length} records)</h5>
            ${data.length > 0 ? tableHtml : '<p class="no-data">No records found.</p>'}
        `;

    } catch (error) {
        output.innerHTML = `<div class="error-message">Error: ${error.message}</div>`;
    }
}

async function generateEmployeeReport() {
    const output = document.getElementById('employeeReportOutput');
    const employeeId = document.getElementById('employeeSelect').value;

    if (!employeeId) {
        output.innerHTML = '<div class="error-message">Please select an employee.</div>';
        return;
    }

    output.innerHTML = '<div class="loading">Loading...</div>';

    try {
        const data = await api.reports.shipmentsByEmployee(employeeId);
        const employee = cachedData.employees?.find(e => e.id === parseInt(employeeId));

        output.innerHTML = `
            <h5>Shipments registered by ${employee?.name || 'Employee'} (${data.length} records)</h5>
            ${data.length > 0 ? `
                <table class="table">
                    <thead><tr><th>ID</th><th>Sender</th><th>Receiver</th><th>Weight</th><th>Price</th><th>Status</th><th>Date</th></tr></thead>
                    <tbody>
                        ${data.map(s => `
                            <tr>
                                <td>${s.id}</td>
                                <td>${s.senderName}</td>
                                <td>${s.receiverName}</td>
                                <td>${s.weight} kg</td>
                                <td>${formatCurrency(s.price)} BGN</td>
                                <td><span class="status-pill ${getStatusClass(s.status)}">${s.status}</span></td>
                                <td>${formatDate(s.registeredAt)}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            ` : '<p class="no-data">No shipments found for this employee.</p>'}
        `;

    } catch (error) {
        output.innerHTML = `<div class="error-message">Error: ${error.message}</div>`;
    }
}
