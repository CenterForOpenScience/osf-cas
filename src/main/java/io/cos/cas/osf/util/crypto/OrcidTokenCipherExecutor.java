package io.cos.cas.osf.util.crypto;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * This is {@link OrcidTokenCipherExecutor}.
 *
 * Encrypts / decrypts the ORCID access and refresh tokens stored in {@code osf_orcid_oauth_token} at rest, using
 * AES/GCM with a key derived from {@code cas.authn.osf-orcid-revocation.token-encryption-key}. JPA
 * {@link javax.persistence.AttributeConverter} instances are instantiated directly by the persistence provider
 * (not by Spring), so the raw configured secret is stashed here as a static field once, during
 * {@code OrcidTokenJpaConfiguration} bean initialization, and read from here by
 * {@link io.cos.cas.osf.orcidtoken.OsfOrcidTokenCryptoConverter}.
 *
 * @author Longze Chen
 * @since 26.1.0
 */
@Slf4j
public final class OrcidTokenCipherExecutor {

    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";

    private static final String KEY_ALGORITHM = "AES";

    private static final int GCM_IV_LENGTH_IN_BYTES = 12;

    private static final int GCM_TAG_LENGTH_IN_BITS = 128;

    private static volatile SecretKeySpec secretKeySpec;

    private OrcidTokenCipherExecutor() {
    }

    /**
     * Initialize the static encryption key from the configured shared secret. Safe to call more than once (e.g. on
     * context refresh); the last value wins.
     *
     * @param rawKey the configured {@code cas.authn.osf-orcid-revocation.token-encryption-key}
     */
    public static void initialize(final String rawKey) {
        if (StringUtils.isBlank(rawKey)) {
            LOGGER.warn("ORCID token encryption key is not configured; ORCID token storage will fail until it is set.");
            secretKeySpec = null;
            return;
        }
        try {
            final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            final byte[] normalizedKey = sha256.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            secretKeySpec = new SecretKeySpec(normalizedKey, KEY_ALGORITHM);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to initialize ORCID token cipher: " + e.getMessage(), e);
        }
    }

    /**
     * Encrypt a plaintext value, returning a Base64 string of {@code iv || ciphertext || tag}.
     *
     * @param plainText the value to encrypt
     * @return the encrypted value, or {@code null} if the input is {@code null}
     */
    public static String encrypt(final String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            final byte[] iv = new byte[GCM_IV_LENGTH_IN_BYTES];
            new SecureRandom().nextBytes(iv);
            final Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, requireKey(), new GCMParameterSpec(GCM_TAG_LENGTH_IN_BITS, iv));
            final byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            final byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to encrypt ORCID token: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt a value previously produced by {@link #encrypt(String)}.
     *
     * @param encoded the Base64-encoded {@code iv || ciphertext || tag}
     * @return the decrypted plaintext, or {@code null} if the input is {@code null}
     */
    public static String decrypt(final String encoded) {
        if (encoded == null) {
            return null;
        }
        try {
            final byte[] combined = Base64.getDecoder().decode(encoded);
            final byte[] iv = new byte[GCM_IV_LENGTH_IN_BYTES];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            final byte[] cipherText = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);
            final Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, requireKey(), new GCMParameterSpec(GCM_TAG_LENGTH_IN_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to decrypt ORCID token: " + e.getMessage(), e);
        }
    }

    private static SecretKeySpec requireKey() {
        final SecretKeySpec key = secretKeySpec;
        if (key == null) {
            throw new IllegalStateException(
                    "ORCID token cipher is not initialized; check cas.authn.osf-orcid-revocation.token-encryption-key"
            );
        }
        return key;
    }
}
