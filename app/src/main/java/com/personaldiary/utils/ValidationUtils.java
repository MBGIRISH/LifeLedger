package com.personaldiary.utils;

import android.util.Patterns;

public final class ValidationUtils {

    private ValidationUtils() {}

    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static String getPasswordError(String password) {
        if (password == null || password.isEmpty()) {
            return "Please enter your password";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain an uppercase letter";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain a number";
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            return "Password must contain a special character";
        }
        return null;
    }

    public static boolean isValidPassword(String password) {
        return getPasswordError(password) == null;
    }
}
