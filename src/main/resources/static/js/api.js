/**
 * Logistics Management System - API Utility
 * ==========================================
 * Centralized API calls with JWT authentication
 *
 * Usage:
 *   import { api, isAuthenticated, logout, getUser } from './api.js';
 *   const shipments = await api.get('/api/shipments');
 */

const API_BASE = '';  // Same origin

// ==========================================
// AUTH HELPERS
// ==========================================

/**
 * Get stored JWT token
 */
export function getToken() {
    return localStorage.getItem('jwt_token');
}

/**
 * Get stored user data
 */
export function getUser() {
    const userData = localStorage.getItem('user_data');
    return userData ? JSON.parse(userData) : null;
}

/**
 * Check if user is authenticated
 */
export function isAuthenticated() {
    const token = getToken();
    const user = getUser();
    return token && user;
}

/**
 * Check if current user has a specific role
 */
export function hasRole(role) {
    const user = getUser();
    return user && user.role === role;
}

/**
 * Store auth data after login/register
 */
export function storeAuth(authResponse) {
    localStorage.setItem('jwt_token', authResponse.token);
    localStorage.setItem('user_data', JSON.stringify({
        userId: authResponse.userId,
        username: authResponse.username,
        email: authResponse.email,
        role: authResponse.role
    }));
}

/**
 * Clear auth data and redirect to login
 */
export function logout() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_data');
    window.location.href = '/login.html';
}

/**
 * Require authentication - redirect to login if not authenticated
 */
export function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = '/login.html';
        return false;
    }
    return true;
}

/**
 * Require specific role - redirect if not authorized
 */
export function requireRole(role) {
    if (!requireAuth()) return false;
    if (!hasRole(role)) {
        alert('You do not have permission to access this page');
        logout();
        return false;
    }
    return true;
}

// ==========================================
// API REQUEST HELPERS
// ==========================================

/**
 * Make an authenticated API request
 * @param {string} endpoint - API endpoint (e.g., '/api/shipments')
 * @param {object} options - Fetch options
 * @returns {Promise<any>} - Response data
 */
async function request(endpoint, options = {}) {
    const token = getToken();

    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    // Add Authorization header if token exists
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers
    };

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, config);

        // Handle 401 Unauthorized - token expired or invalid
        if (response.status === 401) {
            logout();
            throw new Error('Session expired. Please login again.');
        }

        // Handle 403 Forbidden
        if (response.status === 403) {
            throw new Error('You do not have permission to perform this action.');
        }

        // Handle 204 No Content
        if (response.status === 204) {
            return null;
        }

        // Parse JSON response
        const data = await response.json();

        // Handle error responses
        if (!response.ok) {
            const errorMessage = data.message || data.error || 'An error occurred';
            const error = new Error(errorMessage);
            error.status = response.status;
            error.data = data;
            throw error;
        }

        return data;
    } catch (error) {
        // Re-throw if it's already a handled error
        if (error.status) {
            throw error;
        }
        // Network error or other issue
        console.error('API Request failed:', error);
        throw new Error('Network error. Please check your connection.');
    }
}

// ==========================================
// API METHODS
// ==========================================

export const api = {
    /**
     * GET request
     */
    get: (endpoint) => request(endpoint, { method: 'GET' }),

    /**
     * POST request
     */
    post: (endpoint, data) => request(endpoint, {
        method: 'POST',
        body: JSON.stringify(data)
    }),

    /**
     * PUT request
     */
    put: (endpoint, data) => request(endpoint, {
        method: 'PUT',
        body: JSON.stringify(data)
    }),

    /**
     * PATCH request
     */
    patch: (endpoint, data) => request(endpoint, {
        method: 'PATCH',
        body: JSON.stringify(data)
    }),

    /**
     * DELETE request
     */
    delete: (endpoint) => request(endpoint, { method: 'DELETE' }),

    // ==========================================
    // AUTH ENDPOINTS
    // ==========================================

    auth: {
        login: (username, password) =>
            request('/api/auth/login', {
                method: 'POST',
                body: JSON.stringify({ username, password })
            }),

        register: (username, email, password, role) =>
            request('/api/auth/register', {
                method: 'POST',
                body: JSON.stringify({ username, email, password, role })
            })
    },

    // ==========================================
    // COMPANY ENDPOINTS
    // ==========================================

    companies: {
        getAll: () => api.get('/api/companies'),
        getById: (id) => api.get(`/api/companies/${id}`),
        create: (data) => api.post('/api/companies', data),
        update: (id, data) => api.put(`/api/companies/${id}`, data),
        delete: (id) => api.delete(`/api/companies/${id}`)
    },

    // ==========================================
    // OFFICE ENDPOINTS
    // ==========================================

    offices: {
        getAll: () => api.get('/api/offices'),
        getById: (id) => api.get(`/api/offices/${id}`),
        getByCompany: (companyId) => api.get(`/api/offices/company/${companyId}`),
        create: (data) => api.post('/api/offices', data),
        update: (id, data) => api.put(`/api/offices/${id}`, data),
        delete: (id) => api.delete(`/api/offices/${id}`)
    },

    // ==========================================
    // EMPLOYEE ENDPOINTS
    // ==========================================

    employees: {
        getAll: () => api.get('/api/employees'),
        getById: (id) => api.get(`/api/employees/${id}`),
        create: (data) => api.post('/api/employees', data),
        update: (id, data) => api.put(`/api/employees/${id}`, data),
        delete: (id) => api.delete(`/api/employees/${id}`)
    },

    // ==========================================
    // CUSTOMER ENDPOINTS
    // ==========================================

    customers: {
        getAll: () => api.get('/api/customers'),
        getById: (id) => api.get(`/api/customers/${id}`),
        create: (data) => api.post('/api/customers', data),
        update: (id, data) => api.put(`/api/customers/${id}`, data),
        delete: (id) => api.delete(`/api/customers/${id}`)
    },

    // ==========================================
    // SHIPMENT ENDPOINTS
    // ==========================================

    shipments: {
        getAll: () => api.get('/api/shipments'),
        getById: (id) => api.get(`/api/shipments/${id}`),
        create: (data) => api.post('/api/shipments', data),
        update: (id, data) => api.put(`/api/shipments/${id}`, data),
        updateStatus: (id, status) => api.patch(`/api/shipments/${id}/status`, { status }),
        delete: (id) => api.delete(`/api/shipments/${id}`)
    },

    // ==========================================
    // REPORT ENDPOINTS
    // ==========================================

    reports: {
        employees: () => api.get('/api/reports/employees'),
        customers: () => api.get('/api/reports/customers'),
        shipments: () => api.get('/api/reports/shipments'),
        shipmentsByEmployee: (employeeId) => api.get(`/api/reports/shipments/employee/${employeeId}`),
        pendingShipments: () => api.get('/api/reports/shipments/pending'),
        sentByCustomer: (customerId) => api.get(`/api/reports/shipments/customer/${customerId}/sent`),
        receivedByCustomer: (customerId) => api.get(`/api/reports/shipments/customer/${customerId}/received`),
        revenue: (startDate, endDate) => api.get(`/api/reports/revenue?startDate=${startDate}&endDate=${endDate}`),
        dashboardMetrics: () => api.get('/api/reports/dashboard'),
        customerMetrics: () => api.get('/api/reports/customer-metrics')
    },

    // ==========================================
    // PRICING ENDPOINTS
    // ==========================================

    pricing: {
        getInfo: () => api.get('/api/pricing'),
        getConfig: () => api.get('/api/pricing/config'),
        updateConfig: (data) => api.put('/api/pricing/config', data)
    }
};

// ==========================================
// UI HELPERS
// ==========================================

/**
 * Show loading state
 */
export function showLoading(element) {
    if (element) {
        element.innerHTML = '<div class="loading">Loading...</div>';
    }
}

/**
 * Show error message
 */
export function showError(element, message) {
    if (element) {
        element.innerHTML = `<div class="error-message">${message}</div>`;
    }
}

/**
 * Format date for display
 */
export function formatDate(dateString) {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Format currency
 */
export function formatCurrency(amount) {
    if (amount === null || amount === undefined) return '-';
    return parseFloat(amount).toFixed(2);
}

/**
 * Get status display class
 */
export function getStatusClass(status) {
    const statusMap = {
        'REGISTERED': 'status-registered',
        'IN_TRANSIT': 'status-transit',
        'DELIVERED': 'status-delivered',
        'CANCELLED': 'status-cancelled'
    };
    return statusMap[status] || 'status-default';
}
