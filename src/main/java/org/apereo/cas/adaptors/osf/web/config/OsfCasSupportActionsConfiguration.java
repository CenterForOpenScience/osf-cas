package org.apereo.cas.adaptors.osf.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.osf.web.flow.login.OsfPrincipalFromNonInteractiveCredentialsAction;
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

/**
 * This is {@link OsfCasSupportActionsConfiguration}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Configuration(value = "osfCasSupportActionsConfiguration", proxyBeanMethods = false)
@AutoConfigureBefore(CasSupportActionsConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OsfCasSupportActionsConfiguration extends CasSupportActionsConfiguration {

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

    /**
     * Bean configuration for {@link OsfPrincipalFromNonInteractiveCredentialsAction}.
     *
     * @return the initialized action
     */
    @Bean
    public Action osfNonInteractiveAuthenticationCheckAction() {
        return new OsfPrincipalFromNonInteractiveCredentialsAction(
                initialAuthenticationAttemptWebflowEventResolver.getObject(),
                serviceTicketRequestWebflowEventResolver.getObject(),
                adaptiveAuthenticationPolicy.getObject(),
                centralAuthenticationService.getObject()
        );
    }
}
