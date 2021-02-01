package io.cos.cas.osf.authentication.exception;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where institution SSO has failed.
 *
 * @author Longze Chen
 * @since 21.0.0
 */
@NoArgsConstructor
public class InstitutionSsoFailedException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = 6977786012016534260L;

    /**
     * Instantiates a new {@link InstitutionSsoFailedException}.
     *
     * @param msg the msg
     */
    public InstitutionSsoFailedException(final String msg) {
        super(msg);
    }
}
