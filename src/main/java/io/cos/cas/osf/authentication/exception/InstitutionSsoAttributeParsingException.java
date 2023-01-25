package io.cos.cas.osf.authentication.exception;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where institution SSO has failed
 * due to attribute normalization or parsing failure.
 *
 * @author Longze Chen
 * @since 23.1.0
 */
@NoArgsConstructor
public class InstitutionSsoAttributeParsingException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = 4319114898092268727L;

    /**
     * Instantiates a new {@link InstitutionSsoAttributeParsingException}.
     *
     * @param msg the msg
     */
    public InstitutionSsoAttributeParsingException(final String msg) {
        super(msg);
    }
}
