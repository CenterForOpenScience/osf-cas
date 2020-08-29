package org.apereo.cas.adaptors.osf.authentication.support;

import org.apereo.cas.adaptors.osf.models.OsfUser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.security.auth.login.FailedLoginException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

/**
 * This is {@link AuthenticationUtils}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Slf4j
@UtilityClass
public class AuthenticationUtils {

    private String sha256HashPassword(final String password) {
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

    private String updateBCryptHashIdentifier(final String passwordHash) {
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

    private boolean isUnusablePassword(final String passwordHash) {
        return passwordHash == null || passwordHash.startsWith("!");
    }

    private boolean checkPasswordPrefix(final String passwordHash) {
        return passwordHash != null
                && (passwordHash.startsWith("bcrypt$") || passwordHash.startsWith("bcrypt_sha256$"));
    }

    private boolean isCreatedByExternalIdp(final JsonObject externalIdentity) throws FailedLoginException {
        for (final Map.Entry<String, JsonElement> provider : externalIdentity.entrySet()) {
            try {
                for (final Map.Entry<String, JsonElement> identity : provider.getValue().getAsJsonObject().entrySet()) {
                    if (!identity.getValue().isJsonPrimitive()) {
                        throw new FailedLoginException();
                    }
                    if (OsfUserStatus.USER_NOT_CONFIRMED_IDP_CREATE.equals(identity.getValue().getAsString())) {
                        LOGGER.warn(
                                "New but unconfirmed OSF user created via external IdP: [{} -> {}]",
                                identity.getKey(),
                                identity.getValue().toString()
                        );
                        return true;
                    }
                }
            } catch (final IllegalStateException e) {
                throw new FailedLoginException();
            }
        }
        return false;
    }

    public boolean verifyPassword(final String plainTextPassword, final String userPasswordHash) {
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

    public String verifyUserStatus(final OsfUser osfUser) {
        if (osfUser.isActive()) {
            LOGGER.info(
                    "User status check success: username=[{}], status=[{}]",
                    osfUser.getUsername(),
                    OsfUserStatus.USER_ACTIVE
            );
            return OsfUserStatus.USER_ACTIVE;
        } else {
            if (!osfUser.isRegistered() && !osfUser.isConfirmed()) {
                if (isUnusablePassword(osfUser.getPassword())) {
                    try {
                        if (isCreatedByExternalIdp(osfUser.getExternalIdentity())) {
                            LOGGER.warn(
                                    "User status check warning: username=[{}], status=[{}]",
                                    osfUser.getUsername(),
                                    OsfUserStatus.USER_NOT_CONFIRMED_IDP
                            );
                            return OsfUserStatus.USER_NOT_CONFIRMED_IDP;
                        }
                    } catch (final FailedLoginException e) {
                        LOGGER.error(
                                "User status check failure: username=[{}], status=[{}]",
                                osfUser.getUsername(),
                                OsfUserStatus.USER_STATUS_UNKNOWN
                        );
                        return OsfUserStatus.USER_STATUS_UNKNOWN;
                    }
                    LOGGER.warn(
                            "User status check warning: username=[{}], status=[{}]",
                            osfUser.getUsername(),
                            OsfUserStatus.USER_NOT_CLAIMED
                    );
                    return OsfUserStatus.USER_NOT_CLAIMED;
                } else if (checkPasswordPrefix(osfUser.getPassword())) {
                    LOGGER.warn(
                            "User status check warning: username=[{}], status=[{}]",
                            osfUser.getUsername(),
                            OsfUserStatus.USER_NOT_CONFIRMED_OSF
                    );
                    return OsfUserStatus.USER_NOT_CONFIRMED_OSF;
                }
            }
            if (osfUser.isMerged()) {
                LOGGER.error(
                        "User status check failure: username=[{}], status=[{}]",
                        osfUser.getUsername(),
                        OsfUserStatus.USER_MERGED
                );
                return OsfUserStatus.USER_MERGED;
            }
            if (osfUser.isDisabled()) {
                LOGGER.warn(
                        "User status check warning: username=[{}], status=[{}]",
                        osfUser.getUsername(),
                        OsfUserStatus.USER_DISABLED
                );
                return OsfUserStatus.USER_DISABLED;
            }
            LOGGER.error(
                    "User status check failure: username=[{}], status=[{}]",
                    osfUser.getUsername(),
                    OsfUserStatus.USER_STATUS_UNKNOWN
            );
            return OsfUserStatus.USER_STATUS_UNKNOWN;
        }
    }
}
