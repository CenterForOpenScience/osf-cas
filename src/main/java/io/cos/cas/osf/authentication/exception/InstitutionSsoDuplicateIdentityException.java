package io.cos.cas.osf.authentication.exception;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where institution SSO has failed
 * due to duplicate SSO identity.
 *
 * @author Longze Chen
 * @since 23.1.0
 */
@NoArgsConstructor
public class InstitutionSsoDuplicateIdentityException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = 1412743002614665584L;

    /**
     * Instantiates a new {@link InstitutionSsoDuplicateIdentityException}.
     *
     * @param msg the msg
     */
    public InstitutionSsoDuplicateIdentityException(final String msg) {
        super(msg);
    }
}
