package org.apereo.cas.web.flow;

import org.apereo.cas.adaptors.osf.web.flow.OsfCasWebflowConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionSet;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link DelegatedAuthenticationWebflowConfigurer}.
 *
 * The configurer is responsible for adjusting the CAS webflow context for pac4j integration. OSF CAS customizes it by
 * intercepting transition of the success event: it redirects the web flow to the non-interactive authentication check
 * state with action {@link org.apereo.cas.adaptors.osf.web.flow.login.OsfPrincipalFromNonInteractiveCredentialsAction}.
 *
 * @author Misagh Moayyed
 * @author Longze Chen
 * @since 4.2
 */
public class DelegatedAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String DECISION_STATE_CHECK_DELEGATED_AUTHN_FAILURE = "checkDelegatedAuthnFailureDecision";

    private static final String ACTION_ID_DELEGATED_AUTHN_CLIENT_LOGOUT = "delegatedAuthenticationClientLogoutAction";

    public DelegatedAuthenticationWebflowConfigurer(
            final FlowBuilderServices flowBuilderServices,
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            final FlowDefinitionRegistry logoutFlowDefinitionRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties
    ) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            createClientActionActionState(flow);
            createStopWebflowViewState(flow);
            createSaml2ClientLogoutAction();
        }
    }

    protected void createSaml2ClientLogoutAction() {
        final Flow logoutFlow = getLogoutFlow();
        final TransitionableState state = getState(logoutFlow, CasWebflowConstants.STATE_ID_TERMINATE_SESSION);
        state.getEntryActionList().add(createEvaluateAction(ACTION_ID_DELEGATED_AUTHN_CLIENT_LOGOUT));
    }

    protected void createClientActionActionState(final Flow flow) {
        final ActionState actionState = createActionState(
                flow,
                CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
        );

        final TransitionSet transitionSet = actionState.getTransitionSet();
        // OSF CAS customization: transit to the non-interactive authentication check action upon success
        transitionSet.add(createTransition(
                CasWebflowConstants.TRANSITION_ID_SUCCESS,
                OsfCasWebflowConstants.STATE_ID_OSF_NON_INTERACTIVE_AUTHENTICATION_CHECK
        ));

        final String currentStartState = getStartState(flow).getId();
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, currentStartState));

        transitionSet.add(createTransition(
                CasWebflowConstants.TRANSITION_ID_RESUME,
                CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET
        ));
        transitionSet.add(createTransition(
                CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                DECISION_STATE_CHECK_DELEGATED_AUTHN_FAILURE
        ));
        transitionSet.add(
                createTransition(CasWebflowConstants.TRANSITION_ID_STOP,
                        CasWebflowConstants.STATE_ID_STOP_WEBFLOW
                ));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_WARN, CasWebflowConstants.STATE_ID_WARN));
        setStartState(flow, actionState);
    }

    protected void createStopWebflowViewState(final Flow flow) {
        createDecisionState(
                flow,
                DECISION_STATE_CHECK_DELEGATED_AUTHN_FAILURE,
                "flowScope.unauthorizedRedirectUrl != null",
                CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK,
                CasWebflowConstants.STATE_ID_STOP_WEBFLOW
        );

        final ViewState state = createViewState(
                flow,
                CasWebflowConstants.STATE_ID_STOP_WEBFLOW,
                CasWebflowConstants.STATE_ID_PAC4J_STOP_WEBFLOW
        );
        state.getEntryActionList().add(new AbstractAction() {
            @Override
            protected Event doExecute(final RequestContext requestContext) {
                final HttpServletRequest request
                        = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                final HttpServletResponse response
                        = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
                Optional<ModelAndView> mv
                        = DelegatedClientAuthenticationAction.hasDelegationRequestFailed(request, response.getStatus());
                mv.ifPresent(
                        modelAndView ->
                                modelAndView.getModel().forEach((k, v) -> requestContext.getFlowScope().put(k, v))
                );
                return null;
            }
        });
    }
}
