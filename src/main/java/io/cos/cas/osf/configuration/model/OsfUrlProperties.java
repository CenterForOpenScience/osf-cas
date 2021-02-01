package io.cos.cas.osf.configuration.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link OsfUrlProperties}.
 *
 * CASv4.1.x-based oldCAS uses Java Server Pages (JSP) as the template engine with Spring Framework. Settings defined
 * in .properties files can be accessed directly in JSP. However, this is no longer the case for CASv6.2.x-based newCAS
 * which uses Thymeleaf as the template engine with Spring Framework. Thus, server-specific (i.e. production, test and
 * staging servers) URLs need to be manually put into the flow context for the templates to access.
 *
 * @author Longze Chen
 * @since 21.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
public class OsfUrlProperties implements Serializable {

    private static final long serialVersionUID = 5799818150523709901L;

    /**
     * OSF home page URL.
     */
    private String home;

    /**
     * OSF dashboard page URL (must-be-signed-in).
     */
    private String dashboard;

    /**
     * OSF sign-up page URL.
     */
    private String register;

    /**
     * OSF login endpoint with "?next=".
     */
    private String loginWithNext;

    /**
     * OSF logout endpoint URL.
     */
    private String logout;

    /**
     * OSF resend-confirmation page URL.
     */
    private String resendConfirmation;

    /**
     * OSF forgot-password page URL
     */
    private String forgotPassword;

    /**
     * Build the default service URL using OSF login endpoint with OSF home page as destination.
     */
    public String constructDefaultServiceUrl() {
        return loginWithNext + URLEncoder.encode(home, StandardCharsets.UTF_8);
    }
}
