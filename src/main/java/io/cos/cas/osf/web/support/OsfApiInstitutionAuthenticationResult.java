package io.cos.cas.osf.web.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * This is {@link OsfApiInstitutionAuthenticationResult}.
 *
 * @author Longze Chen
 * @since 21.0.0
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString
@Setter
@Slf4j
public class OsfApiInstitutionAuthenticationResult implements Serializable {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = 3971349776123204760L;

    /**
     * The object ID of an OSF Institution.
     */
    private String institutionId;

    /**
     * The user's institutional email.
     */
    private String ssoEmail;

    /**
     * The user's institutional identity.
     */
    private String ssoIdentity;

    /**
     * Verify that the SSO email comes from one of the three attributes in Shibboleth SSO headers.
     * From OSF API's perspective, the email provided by SSO is stored in {@link #ssoEmail} which doesn't have to be
     * the {@code username} f a candidate OSF user. From CAS's perspective, this {@link #ssoEmail} comes from three
     * SSO attributes provided by Shibboleth's authn request: {@code eppn}, {@code mail} and {@code mailOther}.
     * Since {@link #ssoEmail} may have already been deduplicated after successful OSF API request, this method does
     * substring check instead of equality check.
     *
     * @param eppn the eppn attribute
     * @param mail the mail attribute
     * @param mailOther the customized mail attribute
     * @return {@code true} if {@link #ssoEmail} equals to any of the three email attributes; otherwise return {@code false}
     */
    public Boolean verifyOsfSsoEmail(final String eppn, final String mail, final String mailOther) {
        if (StringUtils.isBlank(ssoEmail)) {
            LOGGER.error("[CAS XSLT] SSO Email cannot be blank!");
            return false;
        }
        boolean isPartOfEppn = Boolean.FALSE;
        boolean isPartOfMail = Boolean.FALSE;
        boolean isPartOfMailOther = Boolean.FALSE;
        if (!StringUtils.isBlank(eppn)) {
            isPartOfEppn =  eppn.toLowerCase().contains(ssoEmail.toLowerCase());
        }
        if (!StringUtils.isBlank(mail)) {
            isPartOfMail = mail.toLowerCase().contains(ssoEmail.toLowerCase());
        }
        if (!StringUtils.isBlank(mailOther)) {
            isPartOfMailOther = mailOther.toLowerCase().contains(ssoEmail.toLowerCase());
        }
        return isPartOfEppn || isPartOfMail || isPartOfMailOther;
    }
}
