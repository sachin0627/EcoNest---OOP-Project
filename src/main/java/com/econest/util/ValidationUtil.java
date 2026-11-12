package com.econest.util;

import java.util.regex.Pattern;

/**
 * Centralized Validation Utility for form inputs and business entities.
 * 
 * Demonstrates: Encapsulation of validation rules and method overloading.
 */
public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    // Sri Lankan & international telephone numbers
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+?\\d{1,4}[-\\s]?)?\\(?\\d{1,4}\\)?[-\\s]?\\d{1,4}[-\\s]?\\d{1,9}$"
    );

    private ValidationUtil() {}

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) return false;
        String clean = phone.replaceAll("[\\s-]", "");
        return clean.length() >= 9 && clean.length() <= 15 && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isPositive(double number) {
        return number > 0.0;
    }

    public static boolean isNonNegative(double number) {
        return number >= 0.0;
    }

    public static boolean isPositive(int number) {
        return number > 0;
    }

    public static boolean isNonNegative(int number) {
        return number >= 0;
    }

    public static boolean isValidEcoRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    public static boolean isValidId(String id, String prefix) {
        if (!isNotEmpty(id)) return false;
        return id.trim().toUpperCase().startsWith(prefix.toUpperCase());
    }
}
