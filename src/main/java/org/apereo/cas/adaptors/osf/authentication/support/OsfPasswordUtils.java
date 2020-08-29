package org.apereo.cas.adaptors.osf.authentication.support;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * This is {@link OsfPasswordUtils}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@NoArgsConstructor
@Slf4j
public final class OsfPasswordUtils {

    private static String sha256HashPassword(final String password) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] sha256HashedPassword = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            final StringBuilder builder = new StringBuilder();
            for (final byte b : sha256HashedPassword) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (final Exception e) {
            LOGGER.error("CAS encountered a problem when sha256-hashing the password: [{}]", e.toString());
            return null;
        }
    }

    private static String updateBCryptHashIdentifier(final String passwordHash) {
        try {
            if (passwordHash.charAt(2) != '$') {
                final StringBuilder builder = new StringBuilder(passwordHash);
                builder.setCharAt(2, 'a');
                return builder.toString();
            }
            return passwordHash;
        } catch (final Exception e) {
            LOGGER.error("CAS encountered a problem when updating password hash identifier: [{}]", e.toString());
            return null;
        }
    }

    public static boolean verifyPassword(final String plainTextPassword, final String userPasswordHash) {
        String password, passwordHash;
        try {
            if (userPasswordHash.startsWith("bcrypt$")) {
                passwordHash = userPasswordHash.split("bcrypt\\$")[1];
                password = plainTextPassword;
            } else if(userPasswordHash.startsWith("bcrypt_sha256$")) {
                passwordHash = userPasswordHash.split("bcrypt_sha256\\$")[1];
                password = sha256HashPassword(plainTextPassword);
            } else {
                return false;
            }
            passwordHash = updateBCryptHashIdentifier(passwordHash);
            return password != null && passwordHash != null && BCrypt.checkpw(password, passwordHash);
        } catch (final Exception e) {
            LOGGER.error("CAS encountered a problem when verifying the password: [{}]", e.toString());
            return false;
        }
    }

    public static boolean isUnusablePassword(final String passwordHash) {
        return passwordHash == null || passwordHash.startsWith("!");
    }

    public static boolean checkPasswordPrefix(final String passwordHash) {
        return passwordHash != null
                && (passwordHash.startsWith("bcrypt$") || passwordHash.startsWith("bcrypt_sha256$"));
    }
}
