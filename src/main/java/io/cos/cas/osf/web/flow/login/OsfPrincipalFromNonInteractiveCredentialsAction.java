package io.cos.cas.osf.web.flow.login;

import io.cos.cas.osf.authentication.credential.OsfPostgresCredential;
import io.cos.cas.osf.authentication.support.DelegationProtocol;
import io.cos.cas.osf.configuration.model.OsfApiAuthenticationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

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
 * @since 20.0.0
 */
@Slf4j
@Setter
@Getter
public class OsfPrincipalFromNonInteractiveCredentialsAction extends AbstractNonInteractiveCredentialsAction {

    private static final String USERNAME_PARAMETER_NAME = "username";

    private static final String VERIFICATION_KEY_PARAMETER_NAME = "verification_key";

    private static final String AUTHENTICATION_EXCEPTION = "authnError";

    public static final String INSTITUTION_CLIENTS_PARAMETER_NAME = "institutionClients";

    public static final String NON_INSTITUTION_CLIENTS_PARAMETER_NAME = "nonInstitutionClients";

    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    @NotNull
    private String osfApiInstnAuthnEndpoint;

    @NotNull
    private String osfApiInstnAuthnJwtSecret;

    @NotNull
    private String osfApiInstnAuthnJweSecret;

    @NotNull
    private String osfApiInstnAuthnXslLocation;

    private Map<String, List<String>> authnDelegationClients;

    public OsfPrincipalFromNonInteractiveCredentialsAction(
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            final CentralAuthenticationService centralAuthenticationService,
            final OsfApiAuthenticationProperties osfApiAuthenticationProperties,
            final Map<String, List<String>> authnDelegationClients
    ) {
        super(
                initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy
        );
        this.centralAuthenticationService = centralAuthenticationService;
        this.authnDelegationClients = authnDelegationClients;
        this.osfApiInstnAuthnEndpoint = osfApiAuthenticationProperties.getInstnAuthnEndpoint();
        this.osfApiInstnAuthnJwtSecret = osfApiAuthenticationProperties.getInstnAuthnJwtSecret();
        this.osfApiInstnAuthnJweSecret = osfApiAuthenticationProperties.getInstnAuthnJweSecret();
        this.osfApiInstnAuthnXslLocation = osfApiAuthenticationProperties.getInstnAuthnXslLocation();
    }

    @SneakyThrows
    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {

        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        // Check if credential already exists from delegated authentication
        final Credential credential = WebUtils.getCredential(context);
        if (credential != null) {
            LOGGER.debug("Existing credential found in context of type [{}]", credential.getClass());
            if (credential instanceof ClientCredential) {
                final String clientName = ((ClientCredential) credential).getClientName();
                if (authnDelegationClients.get(NON_INSTITUTION_CLIENTS_PARAMETER_NAME).contains(clientName)) {
                    LOGGER.debug(
                            "Valid non-institution authn delegation client [{}] found with principal [{}]",
                            clientName,
                            credential.getId()
                    );
                    return credential;
                }
                if (authnDelegationClients.get(INSTITUTION_CLIENTS_PARAMETER_NAME).contains(clientName)) {
                    LOGGER.debug(
                            "Valid institution authn delegation client [{}] found with principal [{}]",
                            clientName,
                            credential.getId()
                    );
                    final OsfPostgresCredential osfPostgresCredential = new OsfPostgresCredential();
                    osfPostgresCredential.setUsername(credential.getId());
                    osfPostgresCredential.setInstitutionId(((ClientCredential) credential).getClientName());
                    osfPostgresCredential.setRemotePrincipal(true);
                    osfPostgresCredential.setDelegationProtocol(DelegationProtocol.CAS_PAC4J);
                    return osfPostgresCredential;
                }
                LOGGER.debug("Unsupported delegation client [{}]", clientName);
                return null;
            }
            LOGGER.debug("Unsupported delegation credential [{}]", credential.getClass().getSimpleName());
            return null;
        }

        LOGGER.debug("No valid credential found in the request context.");
        final OsfPostgresCredential osfPostgresCredential = new OsfPostgresCredential();
        final String username = request.getParameter(USERNAME_PARAMETER_NAME);
        final String verificationKey = request.getParameter(VERIFICATION_KEY_PARAMETER_NAME);
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(verificationKey)) {
            osfPostgresCredential.setUsername(username);
            osfPostgresCredential.setVerificationKey(verificationKey);
            osfPostgresCredential.setRememberMe(true);
            LOGGER.debug("User [{}] found in request w/ verificationKey", username);
            return osfPostgresCredential;
        }
        LOGGER.debug("No username or verification key found in the request parameters.");
        return null;
    }

    @Override
    protected Event doPreExecute(final RequestContext context) throws Exception {
        return super.doPreExecute(context);
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return super.doExecute(requestContext);
    }

    /**
     * On error.
     *
     * Super class {@link AbstractNonInteractiveCredentialsAction} always appends an error message to the message
     * context when returning error, of which the default one is{@link javax.security.auth.login.FailedLoginException}.
     * This leads to an extra error message being displayed along with the actual error message when exception happens.
     * Thus, must override the {@link AbstractNonInteractiveCredentialsAction#onError(RequestContext)} with no-op.
     *
     * @param context the context
     */
    @Override
    protected void onError(final RequestContext context) {}
}
