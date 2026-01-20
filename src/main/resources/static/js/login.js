/**
 * Logistics Management System - Login Page
 * =========================================
 * Handles user login and registration with JWT authentication
 */

import { api, storeAuth, isAuthenticated, getUser } from './api.js';

// ==========================================
// INIT
// ==========================================

document.addEventListener('DOMContentLoaded', () => {
    // If already logged in, redirect to appropriate dashboard
    if (isAuthenticated()) {
        redirectToDashboard();
        return;
    }

    setupFormToggle();
    setupLoginForm();
    setupSignupForm();
});

/**
 * Redirect to appropriate dashboard based on role
 */
function redirectToDashboard() {
    const user = getUser();
    if (user.role === 'CUSTOMER') {
        window.location.href = '/customer.html';
    } else {
        window.location.href = '/employee.html';
    }
}

// ==========================================
// FORM TOGGLE
// ==========================================

function setupFormToggle() {
    document.getElementById('showSignup').addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('loginForm').classList.remove('active');
        document.getElementById('signupForm').classList.add('active');
        clearErrors();
    });

    document.getElementById('showLogin').addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('signupForm').classList.remove('active');
        document.getElementById('loginForm').classList.add('active');
        clearErrors();
    });
}

function clearErrors() {
    document.getElementById('loginError').style.display = 'none';
    document.getElementById('signupError').style.display = 'none';
    document.getElementById('signupSuccess').style.display = 'none';
}

// ==========================================
// LOGIN FORM
// ==========================================

function setupLoginForm() {
    const form = document.getElementById('loginForm');
    const btn = document.getElementById('loginBtn');
    const errorDiv = document.getElementById('loginError');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const username = document.getElementById('loginUsername').value.trim();
        const password = document.getElementById('loginPassword').value.trim();

        // Validate
        if (!username || !password) {
            showError(errorDiv, 'Please enter both username and password.');
            return;
        }

        // Show loading state
        setLoading(btn, true);
        errorDiv.style.display = 'none';

        try {
            // Call backend API
            const response = await api.auth.login(username, password);

            // Store auth data
            storeAuth(response);

            // Redirect based on role
            redirectToDashboard();

        } catch (error) {
            console.error('Login error:', error);
            showError(errorDiv, error.message || 'Invalid username or password.');
        } finally {
            setLoading(btn, false);
        }
    });
}

// ==========================================
// SIGNUP FORM
// ==========================================

function setupSignupForm() {
    const form = document.getElementById('signupForm');
    const btn = document.getElementById('signupBtn');
    const errorDiv = document.getElementById('signupError');
    const successDiv = document.getElementById('signupSuccess');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const username = document.getElementById('signupUsername').value.trim();
        const email = document.getElementById('signupEmail').value.trim();
        const password = document.getElementById('signupPassword').value.trim();
        const role = document.getElementById('signupRole').value;

        // Validate
        if (!username || !email || !password) {
            showError(errorDiv, 'Please fill in all fields.');
            return;
        }

        if (username.length < 3) {
            showError(errorDiv, 'Username must be at least 3 characters.');
            return;
        }

        if (password.length < 6) {
            showError(errorDiv, 'Password must be at least 6 characters.');
            return;
        }

        // Show loading state
        setLoading(btn, true);
        errorDiv.style.display = 'none';
        successDiv.style.display = 'none';

        try {
            // Call backend API
            const response = await api.auth.register(username, email, password, role);

            // Store auth data
            storeAuth(response);

            // Show success and redirect
            successDiv.textContent = 'Account created successfully! Redirecting...';
            successDiv.style.display = 'block';

            setTimeout(() => {
                redirectToDashboard();
            }, 1500);

        } catch (error) {
            console.error('Registration error:', error);

            // Handle specific error messages
            let errorMessage = error.message || 'Error creating account. Please try again.';

            if (error.data && error.data.validationErrors) {
                const validationErrors = Object.values(error.data.validationErrors).join(', ');
                errorMessage = validationErrors;
            }

            showError(errorDiv, errorMessage);
        } finally {
            setLoading(btn, false);
        }
    });
}

// ==========================================
// HELPERS
// ==========================================

function showError(element, message) {
    element.textContent = message;
    element.style.display = 'block';
}

function setLoading(button, isLoading) {
    const textSpan = button.querySelector('.btn-text');
    const loadingSpan = button.querySelector('.btn-loading');

    if (isLoading) {
        button.disabled = true;
        textSpan.style.display = 'none';
        loadingSpan.style.display = 'inline';
    } else {
        button.disabled = false;
        textSpan.style.display = 'inline';
        loadingSpan.style.display = 'none';
    }
}
