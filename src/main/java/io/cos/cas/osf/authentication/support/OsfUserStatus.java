package io.cos.cas.osf.authentication.support;

/**
 * This is {@link OsfUserStatus}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
public interface OsfUserStatus {

    String USER_ACTIVE = "ACTIVE";

    String USER_NOT_CONFIRMED_OSF = "NOT_CONFIRMED_OSF";

    String USER_NOT_CONFIRMED_IDP = "NOT_CONFIRMED_IDP";

    String USER_NOT_CONFIRMED_IDP_CREATE = "CREATE";

    String USER_NOT_CLAIMED = "NOT_CLAIMED";

    String USER_MERGED = "MERGED";

    String USER_DISABLED = "DISABLED";

    String USER_STATUS_UNKNOWN = "UNKNOWN";
}
