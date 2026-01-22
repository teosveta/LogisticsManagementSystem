/**
 * Logistics Management System - Login Page
 * Handles user login and registration with JWT authentication
 * Uses modular architecture following SOLID principles
 */

import { api, storeAuth, isAuthenticated } from './api.js';
import { validateLoginForm, validateSignupForm, showValidationError, clearValidationErrors } from './validation.js';
import { redirectToDashboard } from './navigation.js';

// ==========================================
// INIT
// ==========================================

document.addEventListener('DOMContentLoaded', () => {
    if (isAuthenticated()) {
        redirectToDashboard();
        return;
    }

    setupFormToggle();
    setupLoginForm();
    setupSignupForm();
});

// ==========================================
// FORM TOGGLE
// ==========================================

function setupFormToggle() {
    const showSignupLink = document.getElementById('showSignup');
    const showLoginLink = document.getElementById('showLogin');
    const loginForm = document.getElementById('loginForm');
    const signupForm = document.getElementById('signupForm');

    showSignupLink.addEventListener('click', (e) => {
        e.preventDefault();
        loginForm.classList.remove('active');
        signupForm.classList.add('active');
        clearAllErrors();
    });

    showLoginLink.addEventListener('click', (e) => {
        e.preventDefault();
        signupForm.classList.remove('active');
        loginForm.classList.add('active');
        clearAllErrors();
    });
}

function clearAllErrors() {
    const errorDivs = ['loginError', 'signupError', 'signupSuccess'];
    errorDivs.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = 'none';
    });
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

        const validation = validateLoginForm(username, password);
        if (!validation.isValid) {
            showValidationError(errorDiv, validation.message);
            return;
        }

        setButtonLoading(btn, true);
        clearValidationErrors(errorDiv);

        try {
            const response = await api.auth.login(username, password);
            storeAuth(response);
            redirectToDashboard();
        } catch (error) {
            console.error('Login error:', error);
            showValidationError(errorDiv, error.message || 'Invalid username or password.');
        } finally {
            setButtonLoading(btn, false);
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

        const validation = validateSignupForm(username, email, password);
        if (!validation.isValid) {
            showValidationError(errorDiv, validation.message);
            return;
        }

        setButtonLoading(btn, true);
        clearValidationErrors(errorDiv);
        successDiv.style.display = 'none';

        try {
            const response = await api.auth.register(username, email, password, role);
            storeAuth(response);

            successDiv.textContent = 'Account created successfully! Redirecting...';
            successDiv.style.display = 'block';

            setTimeout(() => {
                redirectToDashboard();
            }, 1500);
        } catch (error) {
            console.error('Registration error:', error);

            let errorMessage = error.message || 'Error creating account. Please try again.';
            if (error.data && error.data.validationErrors) {
                errorMessage = Object.values(error.data.validationErrors).join(', ');
            }

            showValidationError(errorDiv, errorMessage);
        } finally {
            setButtonLoading(btn, false);
        }
    });
}

// ==========================================
// UI HELPERS
// ==========================================

function setButtonLoading(button, isLoading) {
    const textSpan = button.querySelector('.btn-text');
    const loadingSpan = button.querySelector('.btn-loading');

    if (isLoading) {
        button.disabled = true;
        if (textSpan) textSpan.style.display = 'none';
        if (loadingSpan) loadingSpan.style.display = 'inline';
    } else {
        button.disabled = false;
        if (textSpan) textSpan.style.display = 'inline';
        if (loadingSpan) loadingSpan.style.display = 'none';
    }
}
