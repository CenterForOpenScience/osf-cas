package io.cos.cas.osf.authentication.support;

/**
 * This is {@link DelegationProtocol}.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
public enum DelegationProtocol {

    NONE("none"),

    OAUTH_PAC4J("oauth-pac4j"),

    CAS_PAC4J("cas-pac4j"),

    SAML_SHIB("saml-shib");

    private final String id;

    DelegationProtocol(final String id) {
        this.id = id;
    }

    public static DelegationProtocol getType(final String id) throws IllegalArgumentException {
        if (id == null) {
            return null;
        }

        for (final DelegationProtocol type : DelegationProtocol.values()) {
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
