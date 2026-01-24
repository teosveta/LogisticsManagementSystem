/**
 * Validation Module - Form and input validation utilities
 * Follows Single Responsibility Principle - handles only validation logic
 */

// ==========================================
// VALIDATION RESULTS
// ==========================================

/**
 * Create a validation result object
 */
function createResult(isValid, message = null) {
    return { isValid, message };
}

// ==========================================
// BASIC VALIDATORS
// ==========================================

/**
 * Check if value is not empty
 */
export function validateRequired(value, fieldName = 'Field') {
    const trimmed = String(value || '').trim();
    if (!trimmed) {
        return createResult(false, `${fieldName} is required.`);
    }
    return createResult(true);
}

/**
 * Check minimum length
 */
export function validateMinLength(value, minLength, fieldName = 'Field') {
    const trimmed = String(value || '').trim();
    if (trimmed.length < minLength) {
        return createResult(false, `${fieldName} must be at least ${minLength} characters.`);
    }
    return createResult(true);
}

/**
 * Check maximum length
 */
export function validateMaxLength(value, maxLength, fieldName = 'Field') {
    const trimmed = String(value || '').trim();
    if (trimmed.length > maxLength) {
        return createResult(false, `${fieldName} must be at most ${maxLength} characters.`);
    }
    return createResult(true);
}

/**
 * Validate email format
 */
export function validateEmail(email) {
    const trimmed = String(email || '').trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(trimmed)) {
        return createResult(false, 'Please enter a valid email address.');
    }
    return createResult(true);
}

/**
 * Validate phone number format
 * Only allows: digits, +, spaces, hyphens, parentheses
 * Rejects any letters or special characters
 */
export function validatePhone(phone) {
    if (!phone) return createResult(true); // Optional field
    const trimmed = String(phone).trim();
    if (!trimmed) return createResult(true);

    // Check for any letters - reject immediately
    if (/[a-zA-Z]/.test(trimmed)) {
        return createResult(false, 'Phone number cannot contain letters.');
    }

    // Only allow digits and common phone characters: + - ( ) space
    const phoneRegex = /^[0-9+\s()\-]{7,20}$/;
    if (!phoneRegex.test(trimmed)) {
        return createResult(false, 'Phone must contain only numbers and valid characters (+, -, spaces, parentheses).');
    }
    return createResult(true);
}

/**
 * Validate positive number
 */
export function validatePositiveNumber(value, fieldName = 'Value') {
    const num = parseFloat(value);
    if (isNaN(num) || num <= 0) {
        return createResult(false, `${fieldName} must be a positive number.`);
    }
    return createResult(true);
}

/**
 * Validate two values are different
 */
export function validateDifferent(value1, value2, message = 'Values must be different.') {
    if (value1 === value2) {
        return createResult(false, message);
    }
    return createResult(true);
}

/**
 * Validate selection is made
 */
export function validateSelection(value, fieldName = 'Selection') {
    if (!value || value === '') {
        return createResult(false, `Please select a ${fieldName}.`);
    }
    return createResult(true);
}

// ==========================================
// COMPOUND VALIDATORS
// ==========================================

/**
 * Validate username (3+ chars, alphanumeric)
 */
export function validateUsername(username) {
    const required = validateRequired(username, 'Username');
    if (!required.isValid) return required;

    const minLength = validateMinLength(username, 3, 'Username');
    if (!minLength.isValid) return minLength;

    return createResult(true);
}

/**
 * Validate password (6+ chars)
 */
export function validatePassword(password) {
    const required = validateRequired(password, 'Password');
    if (!required.isValid) return required;

    const minLength = validateMinLength(password, 6, 'Password');
    if (!minLength.isValid) return minLength;

    return createResult(true);
}

// ==========================================
// FORM VALIDATION
// ==========================================

/**
 * Validate multiple fields at once
 * @param {Array} validations - Array of {field, validator, args} objects
 * @returns {Object} - {isValid: boolean, errors: string[]}
 */
export function validateForm(validations) {
    const errors = [];

    for (const validation of validations) {
        const result = validation.validator(...validation.args);
        if (!result.isValid) {
            errors.push(result.message);
        }
    }

    return {
        isValid: errors.length === 0,
        errors
    };
}

/**
 * Show validation error on form
 */
export function showValidationError(errorElement, message) {
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }
}

/**
 * Clear validation errors
 */
export function clearValidationErrors(errorElement) {
    if (errorElement) {
        errorElement.textContent = '';
        errorElement.style.display = 'none';
    }
}

// ==========================================
// SHIPMENT-SPECIFIC VALIDATORS
// ==========================================

/**
 * Validate shipment form data
 */
export function validateShipmentForm(data) {
    const errors = [];

    // Sender validation
    if (!data.senderId) {
        errors.push('Please select a sender.');
    }

    // Recipient validation
    if (!data.recipientId) {
        errors.push('Please select a receiver.');
    }

    // Sender != Recipient
    if (data.senderId && data.recipientId && data.senderId === data.recipientId) {
        errors.push('Sender and receiver must be different.');
    }

    // Weight validation - must be between 0.01 and 10000 kg
    if (!data.weight || data.weight <= 0) {
        errors.push('Weight must be greater than 0.');
    } else if (data.weight < 0.01) {
        errors.push('Weight must be at least 0.01 kg.');
    } else if (data.weight > 10000) {
        errors.push('Weight cannot exceed 10000 kg.');
    }

    // Delivery destination validation
    if (data.deliverToAddress && !data.deliveryAddress) {
        errors.push('Please enter a delivery address.');
    }

    if (!data.deliverToAddress && !data.deliveryOfficeId) {
        errors.push('Please select a destination office.');
    }

    return {
        isValid: errors.length === 0,
        errors
    };
}

// ==========================================
// COMPANY-SPECIFIC VALIDATORS
// ==========================================

/**
 * Validate company form data
 */
export function validateCompanyForm(data) {
    const errors = [];

    const nameResult = validateMinLength(data.name, 2, 'Company name');
    if (!nameResult.isValid) errors.push(nameResult.message);

    if (!data.registrationNumber || !data.registrationNumber.trim()) {
        errors.push('Registration number is required.');
    }

    if (data.email) {
        const emailResult = validateEmail(data.email);
        if (!emailResult.isValid) errors.push(emailResult.message);
    }

    if (data.phone) {
        const phoneResult = validatePhone(data.phone);
        if (!phoneResult.isValid) errors.push(phoneResult.message);
    }

    return {
        isValid: errors.length === 0,
        errors
    };
}

// ==========================================
// CUSTOMER-SPECIFIC VALIDATORS
// ==========================================

/**
 * Validate customer registration form
 */
export function validateCustomerRegistration(data) {
    const errors = [];

    const usernameResult = validateUsername(data.username);
    if (!usernameResult.isValid) errors.push(usernameResult.message);

    const emailResult = validateEmail(data.email);
    if (!emailResult.isValid) errors.push(emailResult.message);

    const passwordResult = validatePassword(data.password);
    if (!passwordResult.isValid) errors.push(passwordResult.message);

    return {
        isValid: errors.length === 0,
        errors
    };
}

// ==========================================
// LOGIN VALIDATORS
// ==========================================

/**
 * Validate login form data
 */
export function validateLoginForm(username, password) {
    const errors = [];

    if (!username || !username.trim()) {
        errors.push('Please enter your username.');
    }

    if (!password || !password.trim()) {
        errors.push('Please enter your password.');
    }

    return {
        isValid: errors.length === 0,
        errors,
        message: errors.join(' ')
    };
}

/**
 * Validate signup form data
 */
export function validateSignupForm(username, email, password) {
    const errors = [];

    const usernameResult = validateUsername(username);
    if (!usernameResult.isValid) errors.push(usernameResult.message);

    const emailResult = validateEmail(email);
    if (!emailResult.isValid) errors.push(emailResult.message);

    const passwordResult = validatePassword(password);
    if (!passwordResult.isValid) errors.push(passwordResult.message);

    return {
        isValid: errors.length === 0,
        errors,
        message: errors.join(' ')
    };
}
