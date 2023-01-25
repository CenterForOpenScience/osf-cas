package io.cos.cas.osf.authentication.exception;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where institution SSO has failed
 * due to missing required attributes from IdP.
 *
 * @author Longze Chen
 * @since 23.1.0
 */
@NoArgsConstructor
public class InstitutionSsoAttributeMissingException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = 1412743002614665584L;

    /**
     * Instantiates a new {@link InstitutionSsoAttributeMissingException}.
     *
     * @param msg the msg
     */
    public InstitutionSsoAttributeMissingException(final String msg) {
        super(msg);
    }
}
