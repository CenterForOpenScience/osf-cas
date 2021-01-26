package io.cos.cas.osf.web.flow.configurer;

import io.cos.cas.osf.authentication.credential.OsfPostgresCredential;
import io.cos.cas.osf.authentication.exception.AccountNotConfirmedIdpException;
import io.cos.cas.osf.authentication.exception.AccountNotConfirmedOsfException;
import io.cos.cas.osf.authentication.exception.InstitutionSsoFailedException;
import io.cos.cas.osf.authentication.exception.InvalidOneTimePasswordException;
import io.cos.cas.osf.authentication.exception.InvalidUserStatusException;
import io.cos.cas.osf.authentication.exception.InvalidVerificationKeyException;
import io.cos.cas.osf.authentication.exception.OneTimePasswordRequiredException;
import io.cos.cas.osf.authentication.exception.TermsOfServiceConsentRequiredException;
import io.cos.cas.osf.web.flow.support.OsfCasWebflowConstants;

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

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionList;
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
 * @since 20.0.0
 */
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
    protected void createInitialFlowActions(final Flow flow) {
        final ActionList startActionList = flow.getStartActionList();
        startActionList.add(createEvaluateAction(OsfCasWebflowConstants.ACTION_ID_OSF_PRE_INITIAL_FLOW_SETUP));
        super.createInitialFlowActions(flow);
    }

    @Override
    protected void createDefaultViewStates(final Flow flow) {
        super.createDefaultViewStates(flow);
        // Create OSF customized view states
        createTwoFactorLoginFormView(flow);
        createTermsOfServiceConsentLoginFormView(flow);
        createInstitutionLoginView(flow);
        createOrcidLoginAutoRedirectView(flow);
        createDefaultServiceLoginAutoRedirectView(flow);
        createOsfCasAuthenticationExceptionViewStates(flow);
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
        // Create the customized interactive login check actions
        createOsfDefaultLoginCheckAction(flow);
        createOsfInstitutionLoginCheckAction(flow);
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
        createTransitionForState(
                handler,
                InvalidOneTimePasswordException.class.getSimpleName(),
                OsfCasWebflowConstants.VIEW_ID_ONE_TIME_PASSWORD_REQUIRED
        );
        createTransitionForState(
                handler,
                TermsOfServiceConsentRequiredException.class.getSimpleName(),
                OsfCasWebflowConstants.VIEW_ID_TERMS_OF_SERVICE_CONSENT_REQUIRED
        );
        createTransitionForState(
                handler,
                InstitutionSsoFailedException.class.getSimpleName(),
                OsfCasWebflowConstants.VIEW_ID_INSTITUTION_SSO_FAILED
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
        // {@link io.cos.cas.osf.web.flow.login.OsfPrincipalFromNonInteractiveCredentialsAction} and for
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
                OsfCasWebflowConstants.STATE_ID_OSF_DEFAULT_LOGIN_CHECK
        );
        createTransitionForState(
                action,
                CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE
        );
    }

    /**
     * Create the default login check / preparation action for OSF CAS.
     *
     * @param flow the flow
     */
    private void createOsfDefaultLoginCheckAction(final Flow flow) {
        ActionState action = createActionState(
                flow,
                OsfCasWebflowConstants.STATE_ID_OSF_DEFAULT_LOGIN_CHECK,
                OsfCasWebflowConstants.ACTION_ID_OSF_DEFAULT_LOGIN_CHECK
        );
        createTransitionForState(
                action,
                OsfCasWebflowConstants.TRANSITION_ID_USERNAME_PASSWORD_LOGIN,
                CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM
        );
        createTransitionForState(
                action,
                OsfCasWebflowConstants.TRANSITION_ID_INSTITUTION_LOGIN,
                OsfCasWebflowConstants.STATE_ID_OSF_INSTITUTION_LOGIN_CHECK
        );
        createTransitionForState(
                action,
                OsfCasWebflowConstants.TRANSITION_ID_ORCID_LOGIN_AUTO_REDIRECT,
                OsfCasWebflowConstants.VIEW_ID_ORCID_LOGIN_AUTO_REDIRECT
        );
        createTransitionForState(
                action,
                OsfCasWebflowConstants.TRANSITION_ID_DEFAULT_SERVICE_LOGIN_AUTO_REDIRECT,
                OsfCasWebflowConstants.VIEW_ID_DEFAULT_SERVICE_LOGIN_AUTO_REDIRECT
        );
        createTransitionForState(
                action,
                CasWebflowConstants.TRANSITION_ID_ERROR,
                CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM
        );
    }

    /**
     * Create the institution login check / preparation action for OSF CAS.
     *
     * @param flow the flow
     */
    private void createOsfInstitutionLoginCheckAction(final Flow flow) {
        ActionState action = createActionState(
                flow,
                OsfCasWebflowConstants.STATE_ID_OSF_INSTITUTION_LOGIN_CHECK,
                OsfCasWebflowConstants.ACTION_ID_OSF_INSTITUTION_LOGIN_CHECK
        );
        createTransitionForState(
                action,
                CasWebflowConstants.TRANSITION_ID_ERROR,
                OsfCasWebflowConstants.VIEW_ID_INSTITUTION_SSO_FAILED
        );
        createTransitionForState(
                action,
                CasWebflowConstants.TRANSITION_ID_SUCCESS,
                OsfCasWebflowConstants.VIEW_ID_INSTITUTION_SSO_INIT
        );
    }

    /**
     * Create extra authentication exception view states for OSF CAS.
     *
     * @param flow the flow
     */
    private void createOsfCasAuthenticationExceptionViewStates(final Flow flow) {
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
                OsfCasWebflowConstants.VIEW_ID_INSTITUTION_SSO_FAILED,
                OsfCasWebflowConstants.VIEW_ID_INSTITUTION_SSO_FAILED
        );
    }

    /**
     * Create the customized two-factor authentication form submission view state for OSF CAS.
     *
     * @param flow the flow
     */
    private void createTwoFactorLoginFormView(final Flow flow) {
        List<String> propertiesToBind = CollectionUtils.wrapList("oneTimePassword", "source");
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
                OsfCasWebflowConstants.VIEW_ID_ONE_TIME_PASSWORD_REQUIRED,
                OsfCasWebflowConstants.VIEW_ID_ONE_TIME_PASSWORD_REQUIRED,
                binder
        );
        state.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_RENDER_LOGIN_FORM));
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

    /**
     * Create the customized terms of service consent form submission view state for OSF CAS.
     *
     * @param flow the flow
     */
    private void createTermsOfServiceConsentLoginFormView(final Flow flow) {
        List<String> propertiesToBind = CollectionUtils.wrapList("termsOfServiceChecked", "source");
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
                OsfCasWebflowConstants.VIEW_ID_TERMS_OF_SERVICE_CONSENT_REQUIRED,
                OsfCasWebflowConstants.VIEW_ID_TERMS_OF_SERVICE_CONSENT_REQUIRED,
                binder
        );
        state.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_RENDER_LOGIN_FORM));
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

    /**
     * Create the ORCiD login auto-redirect view to support the OSF feature "sign-up via ORCiD".
     *
     * @param flow the flow
     */
    protected void createOrcidLoginAutoRedirectView(final Flow flow) {
        createViewState(
            flow,
            OsfCasWebflowConstants.VIEW_ID_ORCID_LOGIN_AUTO_REDIRECT,
            OsfCasWebflowConstants.VIEW_ID_ORCID_LOGIN_AUTO_REDIRECT
        );
    }

    /**
     * Create the ORCiD login auto-redirect view to support the OSF feature "sign-up via ORCiD".
     *
     * @param flow the flow
     */
    protected void createDefaultServiceLoginAutoRedirectView(final Flow flow) {
        createViewState(
                flow,
                OsfCasWebflowConstants.VIEW_ID_DEFAULT_SERVICE_LOGIN_AUTO_REDIRECT,
                OsfCasWebflowConstants.VIEW_ID_DEFAULT_SERVICE_LOGIN_AUTO_REDIRECT
        );
    }

    /**
     * Create the institution SSO init view state to support the OSF feature "sign-in via institutions".
     *
     * @param flow the flow
     */
    protected void createInstitutionLoginView(final Flow flow) {
        createViewState(
                flow,
                OsfCasWebflowConstants.VIEW_ID_INSTITUTION_SSO_INIT,
                OsfCasWebflowConstants.VIEW_ID_INSTITUTION_SSO_INIT
        );
    }
}
