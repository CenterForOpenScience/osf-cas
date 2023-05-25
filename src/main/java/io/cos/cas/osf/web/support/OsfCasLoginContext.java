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

    private boolean institutionLogin;

    private String institutionId;

    private String institutionSupportEmail;

    private boolean unsupportedInstitutionLogin;

    private boolean orcidRedirect;

    private String orcidLoginUrl;

    private boolean defaultService;

    /**
     * The default service URL that uses OSF login endpoint with OSF home as destination.
     *
     * e.g. http(s)://[OSF Domain]/login?next=[encoded version of http(s)://[OSF Domain]/]
     */
    private String defaultServiceUrl;
}
