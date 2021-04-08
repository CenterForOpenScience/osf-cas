package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.support.oauth.services.OAuth20ServiceRegistry;

import lombok.val;

import io.cos.cas.oauth.support.OsfCasOAuth20Utils;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasOAuth20ServicesConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Longze Chen
 * @since 6.1.0
 */
@Configuration("casOAuth20ServicesConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuth20ServicesConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory> webApplicationServiceFactory;

    @Bean
    public Service oauthCallbackService() {
        // OSF CAS customization: there is no functionality change to this configuration class (as of 6.2.8). However, it must overlaid
        // so that the Spring configuration process can use the overlaid interface constant `OAuth20Constants.BASE_OAUTH20_URL` correctly.
        // This is similar to the issue in `CasOAuth20Configuration`, where interface constants must either 1) be directly used instead of
        // via a built-in helper method or 2) be used via a customized helper method.
        val oAuthCallbackUrl = OsfCasOAuth20Utils.casOAuthCallbackUrlDefinition(casProperties.getServer().getPrefix());
        return webApplicationServiceFactory.getObject().createService(oAuthCallbackUrl);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oauthServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer oauthServiceRegistryExecutionPlanConfigurer() {

        // TODO: Currently, "login to authorize" is not supported for ORCiD, thus only add institution clients. Fortunately, unlike
        //       oldCAS, newCAS supports this feature to be implemented using JPA OSF data access model without OSF side changes.
        final List<String> allowedProviders = new ArrayList<>(casProperties.getAuthn().getOsfPostgres().getInstitutionClients());
        final DefaultRegisteredServiceDelegatedAuthenticationPolicy delegatedAuthenticationPolicy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        delegatedAuthenticationPolicy.setAllowedProviders(allowedProviders);
        delegatedAuthenticationPolicy.setPermitUndefined(false);
        final DefaultRegisteredServiceAccessStrategy accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        accessStrategy.setDelegatedAuthenticationPolicy(delegatedAuthenticationPolicy);

        // OSF CAS customization: re-use oldCAS service ID which is fixed instead of random, use a new user-friendly service name that
        // informs users that this is the login to authorize, temporarily disable ORCiD login via access strategy due to the aforementioned
        // oldCAS limitation, and return allowed attributes via release strategy
        return plan -> {
            val service = new RegexRegisteredService();
            service.setId(983450982340993434L);
            service.setEvaluationOrder(Ordered.HIGHEST_PRECEDENCE);
            service.setName("OSF Authorization");
            service.setDescription("OAuth Authentication Callback Request URL");
            service.setServiceId(oauthCallbackService().getId());
            service.setAccessStrategy(accessStrategy);
            service.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(new ArrayList<>()));
            plan.registerServiceRegistry(new OAuth20ServiceRegistry(applicationContext, service));
        };
    }
}
