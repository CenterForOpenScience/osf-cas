package io.cos.cas.osf.web.flow.configurer;

import io.cos.cas.osf.web.flow.support.OsfCasWebflowConstants;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.DefaultLogoutWebflowConfigurer;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionList;
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
}
