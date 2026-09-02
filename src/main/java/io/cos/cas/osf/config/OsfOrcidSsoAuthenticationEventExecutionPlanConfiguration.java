package io.cos.cas.osf.config;

import io.cos.cas.osf.authentication.handler.support.OsfOrcidSsoAuthenticationHandler;
import io.cos.cas.osf.configuration.model.OsfOrcidSsoAuthenticationProperties;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("osfOrcidSsoAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OsfOrcidSsoAuthenticationEventExecutionPlanConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @ConditionalOnMissingBean(name = "jsonPrincipalFactory")
    @Bean
    public PrincipalFactory jsonPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "osfOrcidSsoAuthenticationHandler")
    @Bean
    public AuthenticationHandler osfOrcidSsoAuthenticationHandler() {
        OsfOrcidSsoAuthenticationProperties jsonProps = casProperties.getAuthn().getOsfOrcidSso();
        return new OsfOrcidSsoAuthenticationHandler(
                jsonProps.getName(),
                servicesManager.getObject(),
                jsonPrincipalFactory(),
                jsonProps.getOrder()
        );
    }

    @ConditionalOnMissingBean(name = "osfOrcidSsoAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer OsfOrcidSsoAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            if (casProperties.getAuthn().getOsfOrcidSso().isEnabled()) {
                plan.registerAuthenticationHandlerWithPrincipalResolver(
                        osfOrcidSsoAuthenticationHandler(),
                        defaultPrincipalResolver.getObject()
                );
            }
        };
    }
}
