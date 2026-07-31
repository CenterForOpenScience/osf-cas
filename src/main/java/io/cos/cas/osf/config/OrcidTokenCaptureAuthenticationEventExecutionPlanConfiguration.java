package io.cos.cas.osf.config;

import io.cos.cas.osf.authentication.postprocessor.OrcidTokenCaptureAuthenticationPostProcessor;
import io.cos.cas.osf.dao.OsfOrcidTokenDao;

import lombok.extern.slf4j.Slf4j;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link OrcidTokenCaptureAuthenticationEventExecutionPlanConfiguration}.
 *
 * Registers {@link OrcidTokenCaptureAuthenticationPostProcessor} into the authentication event execution plan, the
 * same way {@code OsfPostgresAuthenticationEventExecutionPlanConfiguration} registers its handler.
 *
 * @author Longze Chen
 * @since 26.1.0
 */
@Configuration("orcidTokenCaptureAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class OrcidTokenCaptureAuthenticationEventExecutionPlanConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ObjectProvider<OsfOrcidTokenDao> osfOrcidTokenDao;

    @ConditionalOnMissingBean(name = "orcidTokenCaptureAuthenticationPostProcessor")
    @Bean
    public AuthenticationPostProcessor orcidTokenCaptureAuthenticationPostProcessor() {
        return new OrcidTokenCaptureAuthenticationPostProcessor(
                casProperties.getAuthn().getPac4j().getOrcid().getClientName(),
                casProperties.getAuthn().getPac4j().getOrcid().getId(),
                casProperties.getAuthn().getPac4j().getOrcid().getSecret(),
                casProperties.getAuthn().getOsfOrcidRevocation().getRevokeUrl(),
                osfOrcidTokenDao.getObject()
        );
    }

    @ConditionalOnMissingBean(name = "orcidTokenCaptureAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer orcidTokenCaptureAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            LOGGER.debug(
                    "Register [{}] to the authentication event execution plan",
                    OrcidTokenCaptureAuthenticationPostProcessor.class.getSimpleName()
            );
            plan.registerAuthenticationPostProcessor(orcidTokenCaptureAuthenticationPostProcessor());
        };
    }
}
