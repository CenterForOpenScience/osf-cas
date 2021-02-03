package io.cos.cas.osf.authentication.exception;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where a user account needs to agree to OSF's terms of service.
 *
 * @author Longze Chen
 * @since 21.0.0
 */
@NoArgsConstructor
public class TermsOfServiceConsentRequiredException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = -7702088330316457626L;

    /**
     * Instantiates a new {@link TermsOfServiceConsentRequiredException}.
     *
     * @param msg the msg
     */
    public TermsOfServiceConsentRequiredException(final String msg) {
        super(msg);
    }
}
