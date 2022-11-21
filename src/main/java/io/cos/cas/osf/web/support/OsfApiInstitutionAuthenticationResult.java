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

    private static final long serialVersionUID = 3971349776123204760L;

    private String username;

    private String institutionId;

    /**
     * Verify that the username comes from one of the three attributes in Shibboleth SSO headers.
     *
     * @param ssoEppn eppn
     * @param ssoMail mail
     * @param ssoMailOther customized attribute for email
     * @return true if username equals to any of the three else false
     */
    public Boolean verifyOsfUsername(final String ssoEppn, final String ssoMail, final String ssoMailOther) {
        if (StringUtils.isBlank(username)) {
            LOGGER.error("[CAS XSLT] Username={} is blank", username);
            return false;
        }
        return username.equalsIgnoreCase(ssoEppn) || username.equalsIgnoreCase(ssoMail) || username.equalsIgnoreCase(ssoMailOther);
    }
}
