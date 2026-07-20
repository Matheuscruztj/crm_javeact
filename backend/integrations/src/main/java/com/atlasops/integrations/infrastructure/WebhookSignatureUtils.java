package com.atlasops.integrations.infrastructure;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility for HMAC-SHA256 webhook signature generation and verification.
 * Provides tamper-detection for outbound webhook payloads.
 *
 * <p>Validates: P1.9 — HMAC-SHA256 webhook signature
 */
public final class WebhookSignatureUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private WebhookSignatureUtils() {
        // static utility class
    }

    /**
     * Signs a payload with the given secret using HmacSHA256.
     *
     * @param payload the raw string payload to sign
     * @param secret  the shared secret
     * @return lowercase hex-encoded HMAC-SHA256 signature
     */
    public static String sign(String payload, String secret) {
        if (payload == null || secret == null) {
            throw new IllegalArgumentException("Payload and secret must not be null");
        }
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(keySpec);
            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return toHex(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC-SHA256 signature", e);
        }
    }

    /**
     * Verifies a webhook payload against the provided signature using constant-time comparison.
     *
     * @param payload   the raw string payload
     * @param secret    the shared secret
     * @param signature the hex-encoded signature to verify
     * @return true if the signature matches
     */
    public static boolean verify(String payload, String secret, String signature) {
        if (payload == null || secret == null || signature == null) {
            return false;
        }
        String expected = sign(payload, secret);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
