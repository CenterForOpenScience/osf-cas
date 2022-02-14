package io.cos.cas.osf.authentication.support;

/**
 * This is {@link OsfApiPermissionDenied}.
 *
 * @author Longze Chen
 * @since 22.0.0
 */
public enum OsfApiPermissionDenied {

    DEFAULT("PermissionDenied"),

    INSTITUTION_SELECTIVE_SSO_FAILURE("InstitutionSsoSelectiveNotAllowed");

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
