package io.cos.cas.osf.authentication.support;

import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * This is {@link TotpUtils}.
 *
 * Customized from <a href="https://github.com/parkghost/TOTP-authentication-demo">TOTP Authentication Demo</a>
 *
 * @author Longze Chen
 * @since 20.0.0
 */
public final class TotpUtils {

    private static final int[] DIGITS_POWER = {
            //  0  1   2    3     4      5       6        7         8
            1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000
    };

    private static final int WINDOW = 1;

    private static final int INTERVAL = 30;

    private static final int PASS_CODE_LENGTH = 6;

    private static final String SECRETE_KEY_ALGORITHM = "RAW";

    private static final String CRYPTO = "HmacSHA1";

    private static final int HASH_BUFFER_CAPACITY = 8;

    private static final int HASH_OFFSET_1 = 1;

    private static final int HASH_OFFSET_2 = 2;

    private static final int HASH_OFFSET_3 = 3;

    private static final int BITMASK_15 = 0xf;

    private static final int BITMASK_127 = 0x7f;

    private static final int BITMASK_255 = 0xff;

    private static final int BITWISE_AND_8 = 8;

    private static final int BITWISE_AND_16 = 16;

    private static final int BITWISE_AND_24 = 24;

    /**
     * Checks a Time-based One Time Password Code.
     *
     * @param secret the totp secret
     * @param code the code to verify
     * @return a boolean value indicating if the check passes or fails
     *
     * @throws NoSuchAlgorithmException On no such algorithm found to perform the check.
     * @throws InvalidKeyException On the case when the key is invalid.
     */
    public static boolean checkCode(
            final String secret,
            final long code
    ) throws NoSuchAlgorithmException, InvalidKeyException {
        final Base32 codec = new Base32();
        final byte[] decodedKey = codec.decode(secret);
        final long currentTimeSeconds = System.currentTimeMillis() / 1000;
        final long currentInterval = currentTimeSeconds / INTERVAL;
        int window = WINDOW;
        for (int i = -window; i <= window; ++i) {
            final long hash = generateTotp(decodedKey, currentInterval + i);
            if (hash == code) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method uses the JCE to provide the crypto algorithm. HMAC computes a Hashed Message Authentication Code
     * with the crypto hash algorithm as a parameter.
     *
     * @param keyBytes the bytes to use for the HMAC key
     * @param text the message or text to be authenticated
     * @return the HMAC sha
     */
    private static byte[] hmacSha(final byte[] keyBytes, final byte[] text) {
        try {
            final Mac hmac = Mac.getInstance(CRYPTO);
            final SecretKeySpec macKey = new SecretKeySpec(keyBytes, SECRETE_KEY_ALGORITHM);
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (final GeneralSecurityException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * This method generates a TOTP value for the given set of parameters.
     *
     * @param key the shared secret
     * @param time a value that reflects a time
     * @return digits
     */
    private static int generateTotp(final byte[] key, final long time) {
        final byte[] msg = ByteBuffer.allocate(HASH_BUFFER_CAPACITY).putLong(time).array();
        final byte[] hash = hmacSha(key, msg);
        final int offset = hash[hash.length - 1] & BITMASK_15;
        int binary = ((hash[offset] & BITMASK_127) << BITWISE_AND_24);
        binary = binary | ((hash[offset + HASH_OFFSET_1] & BITMASK_255) << BITWISE_AND_16);
        binary = binary | ((hash[offset + HASH_OFFSET_2] & BITMASK_255) << BITWISE_AND_8);
        binary = binary | (hash[offset + HASH_OFFSET_3] & BITMASK_255);
        return binary % DIGITS_POWER[PASS_CODE_LENGTH];
    }
}
