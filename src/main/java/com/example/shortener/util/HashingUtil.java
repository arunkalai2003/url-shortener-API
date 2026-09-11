package com.example.shortener.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashingUtil {

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    private HashingUtil() {
        throw new IllegalStateException("No instances of " + getClass().getName() + " are allowed");
    }

    /**
     * Computes the SHA-256 hash of the given input string and returns it as a
     * lowercase hexadecimal string.
     *
     * @param input the string to hash, encoded as UTF-8 before hashing
     * @return the 64-character lowercase hexadecimal SHA-256 digest of {@code input}
     * @throws IllegalStateException if the SHA-256 algorithm is unavailable in the
     *                                current JVM (should not occur in practice, since
     *                                SHA-256 is a mandatory algorithm for all Java
     *                                platform implementations)
     */
    public static String sha256(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private static String toHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hex[i * 2] = HEX_DIGITS[v >>> 4];
            hex[i * 2 + 1] = HEX_DIGITS[v & 0x0F];
        }
        return new String(hex);
    }
}
