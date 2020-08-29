package org.apereo.cas.adaptors.osf.config;

import org.apereo.cas.adaptors.osf.authentication.handler.support.OsfJsonAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.osf.OsfJsonAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * This is {@link OsfJsonAuthenticationEventExecutionPlanConfiguration}.
 *
 * Longze Chen
 * @since 6.2.1
 */
@Configuration("osfJsonAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class OsfJsonAuthenticationEventExecutionPlanConfiguration {

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

    @ConditionalOnMissingBean(name = "osfJsonAuthenticationHandler")
    @Bean
    public AuthenticationHandler osfJsonAuthenticationHandler() {
        OsfJsonAuthenticationProperties jsonProps = casProperties.getAuthn().getOsfJson();
        OsfJsonAuthenticationHandler authenticationHandler = new OsfJsonAuthenticationHandler(
                jsonProps.getName(),
                servicesManager.getObject(),
                jsonPrincipalFactory(),
                jsonProps.getOrder(),
                jsonProps.getLocation()
        );
        authenticationHandler.setPasswordEncoder(
                PasswordEncoderUtils.newPasswordEncoder(jsonProps.getPasswordEncoder(),applicationContext)
        );
        if (jsonProps.getPasswordPolicy().isEnabled()) {
            authenticationHandler.setPasswordPolicyConfiguration(
                    new PasswordPolicyContext(jsonProps.getPasswordPolicy())
            );
        }
        authenticationHandler.setPrincipalNameTransformer(
                PrincipalNameTransformerUtils.newPrincipalNameTransformer(jsonProps.getPrincipalTransformation())
        );
        return authenticationHandler;
    }

    @ConditionalOnMissingBean(name = "osfJsonAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer osfJsonAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            Resource file = casProperties.getAuthn().getOsfJson().getLocation();
            boolean isEnabled = casProperties.getAuthn().getOsfJson().isEnabled();
            if (isEnabled && file != null) {
                LOGGER.debug("Added JSON resource authentication handler for the target file [{}]", file.getFilename());
                plan.registerAuthenticationHandlerWithPrincipalResolver(
                        osfJsonAuthenticationHandler(),
                        defaultPrincipalResolver.getObject()
                );
            }
        };
    }
}
