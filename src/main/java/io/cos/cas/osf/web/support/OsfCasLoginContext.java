package io.cos.cas.osf.web.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * This is {@link OsfCasLoginContext}.
 *
 * Stores OSF-specific information about the current web flow. Extends {@link Serializable} so that it can be put into
 * and retrieved from the flow context conveniently.
 *
 * @author Longze Chen
 * @since 20.1.0
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString
@Setter
public class OsfCasLoginContext implements Serializable  {

    private static final long serialVersionUID = 7523144720609509742L;

    private String serviceUrl;

    private String handleErrorName;

    private boolean institutionLogin;

    private String institutionId;

    private boolean orcidRedirect;

    private String orcidLoginUrl;

    public OsfCasLoginContext (
            final String serviceUrl,
            final boolean institutionLogin,
            final String institutionId,
            final boolean orcidRedirect,
            final String orcidLoginUrl
    ) {
        this.serviceUrl = serviceUrl;
        this.handleErrorName = null;
        this.institutionLogin = institutionLogin;
        this.institutionId = institutionId;
        this.orcidRedirect = orcidRedirect;
        this.orcidLoginUrl = orcidLoginUrl;
    }
}
