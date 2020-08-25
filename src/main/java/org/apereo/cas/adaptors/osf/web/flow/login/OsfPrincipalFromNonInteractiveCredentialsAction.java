package org.apereo.cas.adaptors.osf.web.flow.login;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.osf.authentication.credential.OsfCredential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

/**
 * This is {@link OsfPrincipalFromNonInteractiveCredentialsAction}.
 *
 * Extends {@link AbstractNonInteractiveCredentialsAction} to check if there is any non-interactive authentication
 * available. In the case of authentication delegation, if credential with matching client found, simply use that
 * credential and return the success event (i.e. to authenticate and create the ticket granting ticket). In the case
 * of "username / verification key" login, if both found in requests parameters, create the OSF credential and return
 * success. Otherwise, return the error event and go to the login page for default "username / password" login.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Slf4j
@Setter
@Getter
public class OsfPrincipalFromNonInteractiveCredentialsAction extends AbstractNonInteractiveCredentialsAction {

    private static final List<String> AUTHN_DELEGATION_CLIENT_LIST = new ArrayList<>(List.of("oldcas", "orcid"));

    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

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
        this.centralAuthenticationService = centralAuthenticationService;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        LOGGER.debug("#### OSF non-interactive authn action: constructCredentialsFromRequest() ...");
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        // Check if credential already exists from delegated authentication
        final Credential credential = WebUtils.getCredential(context);
        if (credential != null) {
            LOGGER.debug(
                    "#### OSF non-interactive authn action: existing credential found of type [{}]",
                    credential.getClass()
            );
            if (credential instanceof ClientCredential) {
                final String clientName = ((ClientCredential) credential).getClientName();
                LOGGER.debug("#### OSF non-interactive authn action: clientName=[{}]", clientName);
                if (AUTHN_DELEGATION_CLIENT_LIST.contains(clientName)) {
                    LOGGER.debug("#### OSF non-interactive authn action: valid authn delegation client found, reuse credential");
                    final String principalId = credential.getId();
                    return credential;
                }
                LOGGER.debug("#### OSF non-interactive authn action: unsupported client [{}]", clientName);
                return null;
            }
            LOGGER.debug("#### OSF non-interactive authn action: unsupported credential [{}]", credential.getClass());
            return null;
        }

        LOGGER.debug("#### OSF non-interactive authn action: credential not found");
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
