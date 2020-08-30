package org.apereo.cas.adaptors.osf.web.flow.configurer;

import org.apereo.cas.adaptors.osf.authentication.credential.OsfPostgresCredential;
import org.apereo.cas.adaptors.osf.authentication.exceptions.AccountNotConfirmedIdpException;
import org.apereo.cas.adaptors.osf.authentication.exceptions.AccountNotConfirmedOsfException;
import org.apereo.cas.adaptors.osf.authentication.exceptions.InvalidUserStatusException;
import org.apereo.cas.adaptors.osf.authentication.exceptions.InvalidVerificationKeyException;
import org.apereo.cas.adaptors.osf.authentication.exceptions.OneTimePasswordRequiredException;
import org.apereo.cas.adaptors.osf.web.flow.OsfCasWebflowConstants;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.DefaultLoginWebflowConfigurer;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.History;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import java.util.List;

/**
 * This is {@link OsfCasLoginWebflowConfigurer}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Slf4j
public class OsfCasLoginWebflowConfigurer extends DefaultLoginWebflowConfigurer {

    /**
     * Instantiates a new OSF customized login web flow configurer {@link OsfCasLoginWebflowConfigurer} expanding the
     * the default login web flow configurer {@link DefaultLoginWebflowConfigurer}.
     *
     * @param flowBuilderServices    the flow builder services
     * @param flowDefinitionRegistry the flow definition registry
     * @param applicationContext     the application context
     * @param casProperties          the cas properties
     */
    public OsfCasLoginWebflowConfigurer(
            final FlowBuilderServices flowBuilderServices,
            final FlowDefinitionRegistry flowDefinitionRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties
    ) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void createDefaultViewStates(final Flow flow) {
        super.createDefaultViewStates(flow);
        // Create OSF customized view states
        createOsfCasViewStates(flow);
    }

    @Override
    protected void createLoginFormView(final Flow flow) {
        List<String> propertiesToBind = CollectionUtils.wrapList("username", "password", "source");
        BinderConfiguration binder = createStateBinderConfiguration(propertiesToBind);
        casProperties.getView().getCustomLoginFormFields()
                .forEach((field, props) -> {
                    String fieldName = String.format("customFields[%s]", field);
                    binder.addBinding(
                            new BinderConfiguration.Binding(fieldName, props.getConverter(), props.isRequired())
                    );
                });
        ViewState state = createViewState(
                flow,
                CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM,
                "casLoginView",
                binder
        );
        state.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_RENDER_LOGIN_FORM));
        // Bind OSF customized credential {@link OsfPostgresCredential}
        createStateModelBinding(state, CasWebflowConstants.VAR_ID_CREDENTIAL, OsfPostgresCredential.class);
        Transition transition = createTransitionForState(
                state,
                CasWebflowConstants.TRANSITION_ID_SUBMIT,
                CasWebflowConstants.STATE_ID_REAL_SUBMIT
        );
        MutableAttributeMap<Object> attributes = transition.getAttributes();
        attributes.put("bind", Boolean.TRUE);
        attributes.put("validate", Boolean.TRUE);
        attributes.put("history", History.INVALIDATE);
    }

    @Override
    protected void createRememberMeAuthnWebflowConfig(final Flow flow) {
        if (casProperties.getTicket().getTgt().getRememberMe().isEnabled()) {
            // Bind OSF customized credential {@link OsfPostgresCredential}
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, OsfPostgresCredential.class);
            ViewState state = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
            BinderConfiguration cfg = getViewStateBinderConfiguration(state);
            cfg.addBinding(new BinderConfiguration.Binding("rememberMe", null, false));
        } else {
            // Bind OSF customized credential {@link OsfPostgresCredential}
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, OsfPostgresCredential.class);
        }
    }

    @Override
    protected void createDefaultActionStates(final Flow flow) {
        super.createDefaultActionStates(flow);
        // Create the customized non-interactive authentication check action
        createOsfNonInteractiveAuthenticationCheckAction(flow);
    }

    @Override
    protected void createHandleAuthenticationFailureAction(final Flow flow) {
        ActionState handler = createActionState(
                flow,
                CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE,
                CasWebflowConstants.ACTION_ID_AUTHENTICATION_EXCEPTION_HANDLER
        );

        // Built-in transitions
        createTransitionForState(
                handler,
                AccountDisabledException.class.getSimpleName(),
                CasWebflowConstants.VIEW_ID_ACCOUNT_DISABLED
        );
        createTransitionForState(
                handler,
                AccountLockedException.class.getSimpleName(),
                CasWebflowConstants.VIEW_ID_ACCOUNT_LOCKED
        );
        createTransitionForState(
                handler,
                AccountPasswordMustChangeException.class.getSimpleName(),
                CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD
        );
        createTransitionForState(
                handler,
                CredentialExpiredException.class.getSimpleName(),
                CasWebflowConstants.VIEW_ID_EXPIRED_PASSWORD
        );
        createTransitionForState(
                handler,
                InvalidLoginLocationException.class.getSimpleName(),
                CasWebflowConstants.VIEW_ID_INVALID_WORKSTATION
        );
        createTransitionForState(
                handler,
                InvalidLoginTimeException.class.getSimpleName(),
                CasWebflowConstants.VIEW_ID_INVALID_AUTHENTICATION_HOURS
        );
        createTransitionForState(
                handler,
                FailedLoginException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM
        );
        createTransitionForState(
                handler,
                AccountNotFoundException.class.getSimpleName(),
                CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM
        );
        createTransitionForState(
                handler,
                UnauthorizedServiceForPrincipalException.class.getSimpleName(),
                CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM
        );
        createTransitionForState(
                handler,
                PrincipalException.class.getSimpleName(),
                CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM
        );
        createTransitionForState(
                handler,
                UnsatisfiedAuthenticationPolicyException.class.getSimpleName(),
                CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM
        );
        createTransitionForState(
                handler,
                UnauthorizedAuthenticationException.class.getSimpleName(),
                CasWebflowConstants.VIEW_ID_AUTHENTICATION_BLOCKED
        );
        createTransitionForState(
                handler,
                CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK,
                CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK
        );

        // OSF customized transitions
        createTransitionForState(
                handler,
                AccountNotConfirmedIdpException.class.getSimpleName(),
                OsfCasWebflowConstants.VIEW_ID_ACCOUNT_NOT_CONFIRMED_IDP
        );
        createTransitionForState(
                handler,
                AccountNotConfirmedOsfException.class.getSimpleName(),
                OsfCasWebflowConstants.VIEW_ID_ACCOUNT_NOT_CONFIRMED_OSF
        );
        createTransitionForState(
                handler,
                InvalidUserStatusException.class.getSimpleName(),
                OsfCasWebflowConstants.VIEW_ID_INVALID_USER_STATUS
        );
        createTransitionForState(
                handler,
                InvalidVerificationKeyException.class.getSimpleName(),
                OsfCasWebflowConstants.VIEW_ID_INVALID_VERIFICATION_KEY
        );
        createTransitionForState(
                handler,
                OneTimePasswordRequiredException.class.getSimpleName(),
                OsfCasWebflowConstants.VIEW_ID_ONE_TIME_PASSWORD_REQUIRED
        );

        // The default transition
        createStateDefaultTransition(handler, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
    }

    @Override
    protected void createServiceAuthorizationCheckAction(final Flow flow) {
        ActionState serviceAuthorizationCheck = createActionState(
                flow,
                CasWebflowConstants.STATE_ID_SERVICE_AUTHZ_CHECK, "serviceAuthorizationCheck"
        );
        // Service authorization check action now transits to the OSF non-interactive action. For the action see
        // {@link org.apereo.cas.adaptors.osf.web.flow.login.OsfPrincipalFromNonInteractiveCredentialsAction} and for
        // its transition see {@link #createOsfNonInteractiveAuthenticationCheckAction}
        createStateDefaultTransition(
                serviceAuthorizationCheck,
                OsfCasWebflowConstants.STATE_ID_OSF_NON_INTERACTIVE_AUTHENTICATION_CHECK
        );
    }

    /**
     * Create the customized non-interactive authentication check action for OSF CAS.
     *
     * @param flow the flow
     */
    private void createOsfNonInteractiveAuthenticationCheckAction(final Flow flow) {
        ActionState action = createActionState(
                flow,
                OsfCasWebflowConstants.STATE_ID_OSF_NON_INTERACTIVE_AUTHENTICATION_CHECK,
                OsfCasWebflowConstants.ACTION_ID_OSF_NON_INTERACTIVE_AUTHENTICATION_CHECK
        );
        createTransitionForState(
                action,
                CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET
        );
        createTransitionForState(
                action,
                CasWebflowConstants.TRANSITION_ID_ERROR,
                CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM
        );
        createTransitionForState(
                action,
                CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE
        );
    }

    /**
     * Create extra view states for OSF CAS.
     *
     * @param flow the flow
     */
    private void createOsfCasViewStates(final Flow flow) {
        createViewState(
                flow,
                OsfCasWebflowConstants.VIEW_ID_ACCOUNT_NOT_CONFIRMED_IDP,
                OsfCasWebflowConstants.VIEW_ID_ACCOUNT_NOT_CONFIRMED_IDP
        );
        createViewState(
                flow,
                OsfCasWebflowConstants.VIEW_ID_ACCOUNT_NOT_CONFIRMED_OSF,
                OsfCasWebflowConstants.VIEW_ID_ACCOUNT_NOT_CONFIRMED_OSF
        );
        createViewState(
                flow,
                OsfCasWebflowConstants.VIEW_ID_INVALID_USER_STATUS,
                OsfCasWebflowConstants.VIEW_ID_INVALID_USER_STATUS
        );
        createViewState(
                flow,
                OsfCasWebflowConstants.VIEW_ID_INVALID_VERIFICATION_KEY,
                OsfCasWebflowConstants.VIEW_ID_INVALID_VERIFICATION_KEY
        );
        createViewState(
                flow,
                OsfCasWebflowConstants.VIEW_ID_ONE_TIME_PASSWORD_REQUIRED,
                OsfCasWebflowConstants.VIEW_ID_ONE_TIME_PASSWORD_REQUIRED
        );
    }
}
