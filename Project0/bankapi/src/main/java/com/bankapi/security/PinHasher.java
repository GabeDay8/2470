package com.bankapi.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Turns a PIN into something safe to store, and checks a PIN against a
 * stored hash. Never stores or logs the raw PIN anywhere.
 *
 * How it works: a random 16-byte "salt" is generated per account so two
 * people with the same PIN don't end up with the same stored hash. The PIN
 * is combined with the salt and run through SHA-256. Both the salt and the
 * resulting hash are stored together (Base64-encoded, separated by ":"),
 * so matches() can redo the same process on login and compare results -
 * it never has to reverse the hash, because SHA-256 can't be reversed.
 */
public final class PinHasher {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PinHasher() {
    }

    public static String hash(String pin) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = digest(pin, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean matches(String pin, String storedHash) {
        String[] parts = storedHash.split(":", 2);
        if (parts.length != 2) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
        byte[] actualHash = digest(pin, salt);
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    private static byte[] digest(String pin, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(pin.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available on this JVM", e);
        }
    }
}