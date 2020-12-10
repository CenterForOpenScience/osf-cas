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

    /**
     * The encoded service URL provided by the "service=" query param in the request URL.
     *
     * This attribute is deprecated and should be removed since 1) ThymeLeaf handles URL building elegantly in the template and 2) both of
     * the flow parameters "service.originalUrl" and "originalUrl" stores the current service information.
     */
    private String encodedServiceUrl;

    private String handleErrorName;

    private boolean institutionLogin;

    private String institutionId;

    private boolean orcidRedirect;

    private String orcidLoginUrl;

    private boolean defaultService;

    /**
     * The default service URL that uses OSF login endpoint with OSF home as destination.
     *
     * e.g. http(s)://[OSF Domain]/login?next=[encoded version of http(s)://[OSF Domain]/]
     */
    private String defaultServiceUrl;

    public OsfCasLoginContext (
            final String encodedServiceUrl,
            final boolean institutionLogin,
            final String institutionId,
            final boolean orcidRedirect,
            final String orcidLoginUrl,
            final boolean defaultService,
            final String defaultServiceUrl
    ) {
        this.encodedServiceUrl = encodedServiceUrl;
        this.handleErrorName = null;
        this.institutionLogin = institutionLogin;
        this.institutionId = institutionId;
        this.orcidRedirect = orcidRedirect;
        this.orcidLoginUrl = orcidLoginUrl;
        this.defaultService = defaultService;
        this.defaultServiceUrl = defaultServiceUrl;
    }
}
