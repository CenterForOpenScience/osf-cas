package io.cos.cas.osf.authentication.exception;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where institution SSO has failed
 * due to OSF API not supporting multiple SSO emails.
 *
 * @author Longze Chen
 * @since 25.0.0
 */
@NoArgsConstructor
public class InstitutionSsoMultipleEmailsNotSupportedException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = -7703550523317297865L;

    /**
     * Instantiates a new {@link InstitutionSsoMultipleEmailsNotSupportedException}.
     *
     * @param msg the msg
     */
    public InstitutionSsoMultipleEmailsNotSupportedException(final String msg) {
        super(msg);
    }
}
