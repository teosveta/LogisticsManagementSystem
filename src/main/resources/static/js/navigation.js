/**
 * Navigation Module - Shared navigation and layout setup
 * Follows Single Responsibility Principle - handles only navigation logic
 */

import { logout, getUser } from './api.js';

// ==========================================
// UI INITIALIZATION
// ==========================================

/**
 * Initialize user interface with current user data
 */
export function initUI(user) {
    const userNameEl = document.getElementById('userName');
    const welcomeNameEl = document.getElementById('welcomeName');

    if (userNameEl) {
        userNameEl.textContent = user.username;
    }
    if (welcomeNameEl) {
        welcomeNameEl.textContent = user.username;
    }
}

// ==========================================
// NAVIGATION SETUP
// ==========================================

/**
 * Setup sidebar navigation with view handler
 * @param {Function} viewHandler - Function to handle view changes
 */
export function setupNavigation(viewHandler) {
    const navButtons = document.querySelectorAll('.nav-btn');

    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            // Update active state
            navButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            // Call view handler with view name
            const view = btn.dataset.view;
            if (view && viewHandler) {
                viewHandler(view);
            }
        });
    });
}

/**
 * Setup mobile menu toggle
 */
export function setupMobileMenu() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');

    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', () => {
            sidebar.classList.toggle('open');
        });

        // Close sidebar when clicking outside on mobile
        document.addEventListener('click', (e) => {
            if (sidebar.classList.contains('open') &&
                !sidebar.contains(e.target) &&
                !menuToggle.contains(e.target)) {
                sidebar.classList.remove('open');
            }
        });
    }
}

/**
 * Setup logout button
 */
export function setupLogout() {
    const logoutBtn = document.getElementById('logoutBtn');

    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            logout();
        });
    }
}

/**
 * Initialize all navigation components
 */
export function initNavigation(viewHandler) {
    const user = getUser();

    if (user) {
        initUI(user);
    }

    setupNavigation(viewHandler);
    setupMobileMenu();
    setupLogout();
}

// ==========================================
// REDIRECT HELPERS
// ==========================================

/**
 * Redirect to dashboard based on user role
 */
export function redirectToDashboard() {
    const user = getUser();

    if (!user) {
        window.location.href = '/login.html';
        return;
    }

    if (user.role === 'CUSTOMER') {
        window.location.href = '/customer.html';
    } else {
        window.location.href = '/employee.html';
    }
}

/**
 * Redirect to login page
 */
export function redirectToLogin() {
    window.location.href = '/login.html';
}

// ==========================================
// VIEW STATE MANAGEMENT
// ==========================================

/**
 * Get content area element
 */
export function getContentArea() {
    return document.getElementById('contentArea');
}

/**
 * Get summary row element
 */
export function getSummaryRow() {
    return document.getElementById('summaryRow');
}

/**
 * Set active navigation button by view name
 */
export function setActiveNav(viewName) {
    const navButtons = document.querySelectorAll('.nav-btn');

    navButtons.forEach(btn => {
        if (btn.dataset.view === viewName) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}
