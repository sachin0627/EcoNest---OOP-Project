package com.econest.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for hashing and verifying passwords securely using SHA-256 with cryptographic salt.
 * 
 * Demonstrates: Encapsulation of security algorithms and data protection.
 */
public class PasswordUtil {
    private static final String DEFAULT_SALT = "econest_secure_salt_2026";

    private PasswordUtil() {}

    /**
     * Generates a cryptographically random salt string.
     * @return base64 encoded salt
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a plain-text password using SHA-256 and the provided salt.
     * @param password plain password
     * @param salt salt string
     * @return hexadecimal SHA-256 digest
     */
    public static String hashPassword(String password, String salt) {
        if (password == null) password = "";
        if (salt == null || salt.isEmpty()) salt = DEFAULT_SALT;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String input = password + salt;
            byte[] hashedBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 hashing algorithm not available!", e);
        }
    }

    /**
     * Verifies if a plain password matches the hashed password.
     * @param plainPassword input password
     * @param salt salt stored with user
     * @param expectedHash stored password hash
     * @return true if password matches
     */
    public static boolean verifyPassword(String plainPassword, String salt, String expectedHash) {
        if (plainPassword == null || expectedHash == null) return false;
        String calculated = hashPassword(plainPassword, salt);
        return calculated.equalsIgnoreCase(expectedHash);
    }
}
