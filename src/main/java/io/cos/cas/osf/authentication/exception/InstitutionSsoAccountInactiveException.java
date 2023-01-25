package io.cos.cas.osf.authentication.exception;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where institution SSO has failed
 * due to the OSF account is not active or not eligible for activation.
 *
 * @author Longze Chen
 * @since 23.1.0
 */
@NoArgsConstructor
public class InstitutionSsoAccountInactiveException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = -430454081442388569L;

    /**
     * Instantiates a new {@link InstitutionSsoAccountInactiveException}.
     *
     * @param msg the msg
     */
    public InstitutionSsoAccountInactiveException(final String msg) {
        super(msg);
    }
}
