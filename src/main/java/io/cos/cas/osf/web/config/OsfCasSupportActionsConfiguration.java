package io.cos.cas.osf.web.config;

import io.cos.cas.osf.dao.JpaOsfDao;
import io.cos.cas.osf.web.flow.login.OsfDefaultLoginPreparationAction;
import io.cos.cas.osf.web.flow.login.OsfInstitutionLoginPreparationAction;
import io.cos.cas.osf.web.flow.login.OsfCasPreInitialFlowSetupAction;
import io.cos.cas.osf.web.flow.login.OsfPrincipalFromNonInteractiveCredentialsAction;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link OsfCasSupportActionsConfiguration}.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@Configuration(value = "osfCasSupportActionsConfiguration", proxyBeanMethods = false)
@AutoConfigureBefore(CasSupportActionsConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OsfCasSupportActionsConfiguration extends CasSupportActionsConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private ObjectProvider<AdaptiveAuthenticationPolicy> adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    private ObjectProvider<JpaOsfDao> jpaOsfDao;

    /**
     * Bean configuration for {@link OsfCasPreInitialFlowSetupAction}.
     *
     * @return the initialized action
     */
    @Bean
    public Action osfCasPreInitialFlowSetupAction() {
        return new OsfCasPreInitialFlowSetupAction(casProperties.getAuthn().getOsfUrl());
    }

    /**
     * Bean configuration for {@link OsfPrincipalFromNonInteractiveCredentialsAction}.
     *
     * @return the initialized action
     */
    @Bean
    public Action osfNonInteractiveAuthenticationCheckAction() {
        final Map<String, List<String>> authnDelegationClients = new LinkedHashMap<>();
        authnDelegationClients.put(
                OsfPrincipalFromNonInteractiveCredentialsAction.INSTITUTION_CLIENTS_PARAMETER_NAME,
                casProperties.getAuthn().getOsfPostgres().getInstitutionClients()
        );
        authnDelegationClients.put(
                OsfPrincipalFromNonInteractiveCredentialsAction.NON_INSTITUTION_CLIENTS_PARAMETER_NAME,
                casProperties.getAuthn().getOsfPostgres().getNonInstitutionClients()
        );
        return new OsfPrincipalFromNonInteractiveCredentialsAction(
                initialAuthenticationAttemptWebflowEventResolver.getObject(),
                serviceTicketRequestWebflowEventResolver.getObject(),
                adaptiveAuthenticationPolicy.getObject(),
                centralAuthenticationService.getObject(),
                casProperties.getAuthn().getOsfUrl(),
                casProperties.getAuthn().getOsfApi(),
                authnDelegationClients
        );
    }

    /**
     * Bean configuration for {@link OsfDefaultLoginPreparationAction}.
     *
     * @return the initialized action
     */
    @Bean
    public Action osfDefaultLoginCheckAction() {
        return new OsfDefaultLoginPreparationAction(
                initialAuthenticationAttemptWebflowEventResolver.getObject(),
                serviceTicketRequestWebflowEventResolver.getObject(),
                adaptiveAuthenticationPolicy.getObject()
        );
    }

    /**
     * Bean configuration for {@link OsfInstitutionLoginPreparationAction}.
     *
     * @return the initialized action
     */
    @Bean
    public Action osfInstitutionLoginCheckAction() {
        return new OsfInstitutionLoginPreparationAction(
                initialAuthenticationAttemptWebflowEventResolver.getObject(),
                serviceTicketRequestWebflowEventResolver.getObject(),
                adaptiveAuthenticationPolicy.getObject(),
                jpaOsfDao.getObject(),
                casProperties.getAuthn().getOsfPostgres().getInstitutionClients()
        );
    }
}
