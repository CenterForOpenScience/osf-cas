package org.apereo.cas.adaptors.osf.web.flow.login;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.osf.authentication.credential.OsfCredential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link OsfPrincipalFromNonInteractiveCredentialsAction}.
 *
 * Extends {@link AbstractNonInteractiveCredentialsAction} to check if there is any non-interactive authentication
 * available. If found and valid, create the credential and return success to authenticate and create ticket granting
 * ticket. Otherwise, return error and go to the login page.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Slf4j
@Setter
@Getter
public class OsfPrincipalFromNonInteractiveCredentialsAction extends AbstractNonInteractiveCredentialsAction {

    public OsfPrincipalFromNonInteractiveCredentialsAction(
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            final CentralAuthenticationService centralAuthenticationService
    ) {
        super(
                initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy
        );
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        LOGGER.debug("#### OSF non-interactive authn action: constructCredentialsFromRequest() ...");
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final OsfCredential osfCredential = new OsfCredential();
        final String username = request.getParameter("username");
        final String verification_key = request.getParameter("verification_key");
        if (username != null && !username.isEmpty() && verification_key != null && !verification_key.isEmpty()) {
            osfCredential.setUsername(username);
            osfCredential.setVerificationKey(verification_key);
            LOGGER.debug("User [{}] found in request w/ one-time verification key [{}]", username, verification_key);
            return osfCredential;
        }
        LOGGER.debug("No user with verification key found in request");
        return null;
    }

    @Override
    protected Event doPreExecute(final RequestContext context) throws Exception {
        LOGGER.debug("#### OSF non-interactive authn action: doPreExecute() ...");
        return super.doPreExecute(context);
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        LOGGER.debug("#### OSF non-interactive authn action: doExecute() ...");
        return super.doExecute(requestContext);
    }
}
