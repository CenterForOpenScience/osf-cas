package io.cos.cas.osf.web.flow.config;

import io.cos.cas.osf.web.flow.configurer.OsfCasLoginWebflowConfigurer;
import io.cos.cas.osf.web.flow.configurer.OsfCasLogoutWebFlowConfigurer;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * This is {@link OsfCasWebflowContextConfiguration}.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@Configuration("osfCasWebflowContextConfiguration")
@AutoConfigureBefore(CasWebflowContextConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OsfCasWebflowContextConfiguration extends CasWebflowContextConfiguration {

    private static final int DEFAULT_WEB_FLOW_CONFIGURER_ORDER = 0;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Override
    @Bean
    @Order(DEFAULT_WEB_FLOW_CONFIGURER_ORDER)
    @Lazy(false)
    public CasWebflowConfigurer defaultWebflowConfigurer() {
        OsfCasLoginWebflowConfigurer osfCasLoginWebflowConfigurer
                = new OsfCasLoginWebflowConfigurer(builder(), loginFlowRegistry(), applicationContext, casProperties);
        osfCasLoginWebflowConfigurer.setLogoutFlowDefinitionRegistry(logoutFlowRegistry());
        osfCasLoginWebflowConfigurer.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return osfCasLoginWebflowConfigurer;
    }

    @Override
    @Bean
    @Order(DEFAULT_WEB_FLOW_CONFIGURER_ORDER)
    @Lazy(false)
    public CasWebflowConfigurer defaultLogoutWebflowConfigurer() {
        OsfCasLogoutWebFlowConfigurer osfCasLogoutWebFlowConfigurer
                = new OsfCasLogoutWebFlowConfigurer(builder(), loginFlowRegistry(), applicationContext, casProperties);
        osfCasLogoutWebFlowConfigurer.setLogoutFlowDefinitionRegistry(logoutFlowRegistry());
        osfCasLogoutWebFlowConfigurer.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return osfCasLogoutWebFlowConfigurer;
    }
}
