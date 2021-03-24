package io.cos.cas.osf.web.flow.configurer;

import io.cos.cas.osf.web.flow.support.OsfCasWebflowConstants;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.DefaultLogoutWebflowConfigurer;
import org.apereo.cas.web.flow.logout.TerminateSessionAction;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionList;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link OsfCasLogoutWebFlowConfigurer}.
 *
 * @author Longze Chen
 * @since 21.0.0
 */
public class OsfCasLogoutWebFlowConfigurer extends DefaultLogoutWebflowConfigurer {

    public OsfCasLogoutWebFlowConfigurer(
            final FlowBuilderServices flowBuilderServices,
            final FlowDefinitionRegistry flowDefinitionRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties
    ) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLogoutFlow();
        if (flow != null) {
            createInitialFlowActions(flow);
            createInstitutionLogoutRedirectAction(flow);
        }
        super.doInitialize();
    }

    /**
     * Create initial flow actions similar to {@link OsfCasLoginWebflowConfigurer#createInitialFlowActions(Flow)}.
     *
     * @param flow the flow
     */
    protected void createInitialFlowActions(final Flow flow) {
        final ActionList startActionList = flow.getStartActionList();
        startActionList.add(createEvaluateAction(OsfCasWebflowConstants.ACTION_ID_OSF_PRE_INITIAL_FLOW_SETUP));
    }

    @Override
    protected ActionState createTerminateSessionActionState(final Flow flow) {
        final ActionState actionState = createActionState(
                flow,
                CasWebflowConstants.STATE_ID_TERMINATE_SESSION,
                CasWebflowConstants.ACTION_ID_TERMINATE_SESSION
        );
        createTransitionForState(
                actionState,
                CasWebflowConstants.TRANSITION_ID_WARN,
                CasWebflowConstants.STATE_ID_CONFIRM_LOGOUT_VIEW
        );
        createTransitionForState(
                actionState,
                OsfCasWebflowConstants.TRANSITION_ID_INSTITUTION_LOGOUT,
                OsfCasWebflowConstants.STATE_ID_INSTITUTION_LOGOUT_REDIRECT
        );
        createStateDefaultTransition(actionState, CasWebflowConstants.STATE_ID_DO_LOGOUT);
        return actionState;
    }

    /**
     * Create an redirect action to support customized institution logout.
     *
     * @param flow the flow
     */
    protected void createInstitutionLogoutRedirectAction(final Flow flow) {
        final String viewId = "flowScope." + TerminateSessionAction.PARAMETER_INSTITUTION_LOGOUT_ENDPOINT;
        createEndState(flow, OsfCasWebflowConstants.STATE_ID_INSTITUTION_LOGOUT_REDIRECT, viewId, true);
    }
}
