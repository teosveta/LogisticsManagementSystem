/**
 * UI Module - Reusable UI components and rendering utilities
 * Follows Single Responsibility Principle - handles only UI rendering
 */

import { formatCurrency, getStatusClass, formatDate } from './api.js';

// ==========================================
// LOADING & ERROR STATES
// ==========================================

/**
 * Show loading spinner in element
 */
export function showLoading(element, message = 'Loading...') {
    if (element) {
        element.innerHTML = `<div class="loading">${message}</div>`;
    }
}

/**
 * Show error message in element
 */
export function showError(element, message) {
    if (element) {
        element.innerHTML = `<div class="error-message">${message}</div>`;
    }
}

/**
 * Show success message in element
 */
export function showSuccess(element, message) {
    if (element) {
        element.innerHTML = `<div class="success-message">${message}</div>`;
    }
}

/**
 * Show notification toast message
 * @param {string} message - Message to display
 * @param {string} type - Type of notification ('success' or 'error')
 */
export function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    document.body.appendChild(notification);

    setTimeout(() => {
        notification.classList.add('fade-out');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// ==========================================
// MODAL UTILITIES
// ==========================================

/**
 * Show modal with content
 */
export function showModal(modalId, content) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.innerHTML = content;
        modal.style.display = 'flex';
    }
}

/**
 * Close modal by id
 */
export function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

/**
 * Create modal wrapper HTML
 */
export function createModalWrapper(title, formId, formContent, isEdit = false) {
    return `
        <div class="modal-content">
            <h4>${title}</h4>
            <form id="${formId}">
                ${formContent}
                <div class="modal-buttons">
                    <button type="submit" class="primary-btn">${isEdit ? 'Update' : 'Create'}</button>
                    <button type="button" class="btn-cancel" data-close-modal>Cancel</button>
                </div>
            </form>
        </div>
    `;
}

// ==========================================
// TABLE RENDERING
// ==========================================

/**
 * Render a generic table with configuration
 * @param {Object} config - Table configuration
 * @param {Array} config.columns - Column definitions [{key, label, render?}]
 * @param {Array} config.data - Data rows
 * @param {Function} config.renderActions - Optional function to render action buttons
 * @param {string} config.emptyMessage - Message when no data
 */
export function renderTable(config) {
    const { columns, data, renderActions, emptyMessage = 'No data found.' } = config;

    if (!data || data.length === 0) {
        return `<p class="no-data">${emptyMessage}</p>`;
    }

    const headerRow = columns.map(col => `<th>${col.label}</th>`).join('');
    const actionsHeader = renderActions ? '<th>Actions</th>' : '';

    const bodyRows = data.map(row => {
        const cells = columns.map(col => {
            const value = col.render ? col.render(row) : (row[col.key] ?? '-');
            return `<td>${value}</td>`;
        }).join('');
        const actionsCell = renderActions ? `<td>${renderActions(row)}</td>` : '';
        return `<tr>${cells}${actionsCell}</tr>`;
    }).join('');

    return `
        <table class="table">
            <thead><tr>${headerRow}${actionsHeader}</tr></thead>
            <tbody>${bodyRows}</tbody>
        </table>
    `;
}

/**
 * Render shipments table - specialized for shipments data
 */
export function renderShipmentsTable(shipments, options = {}) {
    const { showStatusSelect = false, showActions = false, emptyMessage = 'No shipments found.' } = options;

    if (shipments.length === 0) {
        return `<p class="no-data">${emptyMessage}</p>`;
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
                    ${showStatusSelect || showActions ? '<th>Actions</th>' : ''}
                </tr>
            </thead>
            <tbody>
                ${shipments.map(s => `
                    <tr>
                        <td><a href="#" class="shipment-link" data-id="${s.id}">#${s.id}</a></td>
                        <td>${s.senderName || 'N/A'}</td>
                        <td>${s.receiverName || 'N/A'}</td>
                        <td>${s.originOfficeName || 'N/A'}</td>
                        <td>${s.deliverToAddress ? s.deliveryAddress : (s.destinationOfficeName || 'N/A')}</td>
                        <td>${s.weight} kg</td>
                        <td>${formatCurrency(s.price)} BGN</td>
                        <td><span class="status-pill ${getStatusClass(s.status)}">${s.status}</span></td>
                        ${showStatusSelect || showActions ? `
                            <td class="actions-cell">
                                ${showStatusSelect ? `
                                    <select class="status-select" data-id="${s.id}" ${s.status === 'DELIVERED' || s.status === 'CANCELLED' ? 'disabled' : ''}>
                                        <option value="">Status...</option>
                                        <option value="IN_TRANSIT" ${s.status === 'IN_TRANSIT' ? 'disabled' : ''}>In Transit</option>
                                        <option value="DELIVERED" ${s.status === 'DELIVERED' ? 'disabled' : ''}>Delivered</option>
                                        <option value="CANCELLED" ${s.status === 'CANCELLED' ? 'disabled' : ''}>Cancelled</option>
                                    </select>
                                ` : ''}
                                ${showActions ? `
                                    <button class="btn-sm btn-view" onclick="viewShipmentDetails(${s.id})">View</button>
                                    <button class="btn-sm btn-edit" onclick="editShipment(${s.id})" ${s.status === 'DELIVERED' || s.status === 'CANCELLED' ? 'disabled' : ''}>Edit</button>
                                    <button class="btn-sm btn-delete" onclick="deleteShipment(${s.id})">Delete</button>
                                ` : ''}
                            </td>
                        ` : ''}
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

/**
 * Render customer shipments table (for customer dashboard)
 */
export function renderCustomerShipmentsTable(shipments, type) {
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

// ==========================================
// FORM UTILITIES
// ==========================================

/**
 * Create a form group HTML
 */
export function createFormGroup(config) {
    const {
        id,
        label,
        type = 'text',
        value = '',
        required = false,
        placeholder = '',
        options = null,
        minlength = null,
        readonly = false,
        hint = null
    } = config;

    const requiredAttr = required ? 'required' : '';
    const minlengthAttr = minlength ? `minlength="${minlength}"` : '';
    const readonlyAttr = readonly ? 'readonly disabled' : '';
    const placeholderAttr = placeholder ? `placeholder="${placeholder}"` : '';

    let input;
    if (type === 'select' && options) {
        input = `
            <select id="${id}" ${requiredAttr}>
                ${options.map(opt =>
                    `<option value="${opt.value}" ${opt.selected ? 'selected' : ''}>${opt.label}</option>`
                ).join('')}
            </select>
        `;
    } else {
        input = `<input type="${type}" id="${id}" value="${value}" ${requiredAttr} ${minlengthAttr} ${readonlyAttr} ${placeholderAttr}>`;
    }

    return `
        <div class="form-group">
            <label>${label}${required ? ' *' : ''}</label>
            ${input}
            ${hint ? `<small>${hint}</small>` : ''}
        </div>
    `;
}

/**
 * Create select options from array of objects
 */
export function createSelectOptions(items, valueKey, labelFn, selectedValue = null, placeholder = 'Select...') {
    const options = [`<option value="">${placeholder}</option>`];
    items.forEach(item => {
        const value = item[valueKey];
        const label = typeof labelFn === 'function' ? labelFn(item) : item[labelFn];
        const selected = selectedValue === value ? 'selected' : '';
        options.push(`<option value="${value}" ${selected}>${label}</option>`);
    });
    return options.join('');
}

// ==========================================
// SUMMARY CARDS
// ==========================================

/**
 * Render summary cards row
 */
export function renderSummaryCards(cards) {
    return cards.map(card =>
        `<div class="sum-card"><h4>${card.title}</h4><p>${card.value}</p></div>`
    ).join('');
}

// ==========================================
// FILTER CONTROLS
// ==========================================

/**
 * Create filter controls HTML
 */
export function createFilterControls(config) {
    const { searchId, searchPlaceholder, filters = [] } = config;

    const filterSelects = filters.map(filter => `
        <select id="${filter.id}">
            <option value="">${filter.placeholder}</option>
            ${filter.options.map(opt => `<option value="${opt.value}">${opt.label}</option>`).join('')}
        </select>
    `).join('');

    return `
        <div class="table-controls">
            <input type="text" id="${searchId}" placeholder="${searchPlaceholder}" class="search-input">
            ${filterSelects}
        </div>
    `;
}

// ==========================================
// STATUS UTILITIES
// ==========================================

/**
 * Render status pill
 */
export function renderStatusPill(status) {
    return `<span class="status-pill ${getStatusClass(status)}">${status}</span>`;
}

/**
 * Render employee type badge
 */
export function renderTypeBadge(type) {
    return `<span class="type-badge type-${type?.toLowerCase()}">${type || 'N/A'}</span>`;
}
