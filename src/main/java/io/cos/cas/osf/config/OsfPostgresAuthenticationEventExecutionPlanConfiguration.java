package io.cos.cas.osf.config;

import io.cos.cas.osf.authentication.handler.support.OsfPostgresAuthenticationHandler;
import io.cos.cas.osf.configuration.model.OsfPostgresAuthenticationProperties;
import io.cos.cas.osf.dao.JpaOsfDao;

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

/**
 * This is {@link OsfPostgresAuthenticationEventExecutionPlanConfiguration}.
 *
 * Longze Chen
 * @since 20.0.0
 */
@Configuration("osfPostgresAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OsfPostgresAuthenticationEventExecutionPlanConfiguration {

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

    @Autowired
    private ObjectProvider<JpaOsfDao> jpaOsfDao;

    @ConditionalOnMissingBean(name = "jsonPrincipalFactory")
    @Bean
    public PrincipalFactory jsonPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "osfPostgresAuthenticationHandler")
    @Bean
    public AuthenticationHandler osfPostgresAuthenticationHandler() {
        OsfPostgresAuthenticationProperties jsonProps = casProperties.getAuthn().getOsfPostgres();
        return new OsfPostgresAuthenticationHandler(
                jsonProps.getName(),
                servicesManager.getObject(),
                jsonPrincipalFactory(),
                jsonProps.getOrder(),
                jpaOsfDao.getObject()
        );
    }

    @ConditionalOnMissingBean(name = "osfPostgresAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer osfPostgresAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            if (casProperties.getAuthn().getOsfPostgres().isEnabled()) {
                plan.registerAuthenticationHandlerWithPrincipalResolver(
                        osfPostgresAuthenticationHandler(),
                        defaultPrincipalResolver.getObject()
                );
            }
        };
    }
}
