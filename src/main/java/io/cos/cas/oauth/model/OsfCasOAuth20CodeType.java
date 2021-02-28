package io.cos.cas.oauth.model;

/**
 * This is {@link OsfCasOAuth20CodeType}.
 *
 * @author Longze Chen
 * @since 21.x.0
 */
public enum OsfCasOAuth20CodeType {

    /** Authorization code that allows generating a refresh token along with the access token. */
    OFFLINE(0),

    /** Authorization code that only allows generating an access token. */
    ONLINE(1),

    /** Access token that is generated from an OSF Personal Access Token. */
    PERSONAL(2),

    /** Access token or refresh token generated from the OAuth 2.0 authorization. */
    VANILLA(3);

    /** The value of the code / token enumeration. */
    private final int value;

    /**
     * Constructs a new {@link OsfCasOAuth20CodeType}.
     *
     * @param newValue the value representing the code / token type.
     */
    OsfCasOAuth20CodeType(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}
