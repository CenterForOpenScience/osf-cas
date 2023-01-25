package io.cos.cas.osf.authentication.support;

/**
 * This is {@link OsfApiPermissionDenied}.
 *
 * @author Longze Chen
 * @since 22.0.1
 */
public enum OsfApiPermissionDenied {

    DEFAULT("PermissionDenied"),

    INSTITUTION_SSO_DUPLICATE_IDENTITY("InstitutionSsoDuplicateIdentity"),

    INSTITUTION_SSO_ACCOUNT_INACTIVE("InstitutionSsoAccountInactive"),

    INSTITUTION_SSO_SELECTIVE_LOGIN_DENIED("InstitutionSsoSelectiveLoginDenied");

    private final String id;

    OsfApiPermissionDenied(final String id) {
        this.id = id;
    }

    public static OsfApiPermissionDenied getType(final String id) throws IllegalArgumentException {
        if (id == null) {
            return null;
        }

        for (final OsfApiPermissionDenied type : OsfApiPermissionDenied.values()) {
            if (id.equals(type.getId())) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching type for id " + id);
    }

    public final String getId() {
        return id;
    }
}
