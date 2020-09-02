package io.cos.cas.osf.authentication.support;

import io.cos.cas.osf.model.OsfUser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

import javax.security.auth.login.FailedLoginException;
import java.util.Map;

/**
 * This is {@link OsfUserUtils}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Slf4j
public final class OsfUserUtils {

    private static boolean isCreatedByExternalIdp(final JsonObject externalIdentity) throws FailedLoginException {
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

    public static String verifyUserStatus(final OsfUser osfUser) {
        if (osfUser.isActive()) {
            LOGGER.info(
                    "User status check success: username=[{}], status=[{}]",
                    osfUser.getUsername(),
                    OsfUserStatus.USER_ACTIVE
            );
            return OsfUserStatus.USER_ACTIVE;
        } else {
            if (!osfUser.isRegistered() && !osfUser.isConfirmed()) {
                if (OsfPasswordUtils.isUnusablePassword(osfUser.getPassword())) {
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
                } else if (OsfPasswordUtils.checkPasswordPrefix(osfUser.getPassword())) {
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
