package io.cos.cas.oauth.services;

import io.cos.cas.osf.dao.JpaOsfDao;
import io.cos.cas.osf.model.OsfOAuth20App;

import lombok.extern.slf4j.Slf4j;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractServiceRegistry;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

import javax.validation.constraints.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link OsfPostgresServiceRegistry}, the customized service registry OSF developer apps.
 *
 * OSF developer apps are stored in the OSF database and has its own mode {@link OsfOAuth20App} in CAS. The method {@link #load()} uses the
 * data access object {@link #jpaOsfDao} to retrieve all active developer apps and then transform them into {@link OAuthRegisteredService}
 * objects with extra settings applied.
 *
 * Here is the documentation on how to inject custom service registries into the CAS implementation (i.e. managed by and accessed via
 * service managers.): https://apereo.github.io/cas/6.2.x/services/Custom-Service-Management.html.
 *
 * Here is the configuration class: {@link io.cos.cas.oauth.config.OsfPostgresServiceRegistryConfiguration}.
 *
 * @author Longze Chen
 * @since 21.x.0
 */
@Slf4j
public class OsfPostgresServiceRegistry extends AbstractServiceRegistry {

    private static final int HEX_RADIX = 16;

    private static final int EVALUATION_ORDER = Ordered.LOWEST_PRECEDENCE;

    @NotNull
    private final JpaOsfDao jpaOsfDao;

    @NotNull
    private final CasConfigurationProperties casProperties;

    private Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<>();

    /**
     * Create a new {@link OsfPostgresServiceRegistry} object.
     *
     * This constructor is used during bean configuration. See {@link io.cos.cas.oauth.config.OsfPostgresServiceRegistryConfiguration}.
     *
     * @param applicationContext the configurable application context
     * @param serviceRegistryListeners a collection of service registry listeners
     * @param casProperties the CAS configuration properties
     * @param jpaOsfDao the JPA OSF data access object
     */
    public OsfPostgresServiceRegistry(
            final ConfigurableApplicationContext applicationContext,
            final Collection<ServiceRegistryListener> serviceRegistryListeners,
            final CasConfigurationProperties casProperties,
            final JpaOsfDao jpaOsfDao
    ) {
        super(applicationContext, serviceRegistryListeners);
        this.casProperties = casProperties;
        this.jpaOsfDao = jpaOsfDao;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        LOGGER.error("Service registry [{}] is read-only.", this.getClass().getName());
        return false;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        LOGGER.error("Service registry [{}] is read-only.", this.getClass().getName());
        return null;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return serviceMap.get(id);
    }

    @Override
    public long size() {
        return this.serviceMap.size();
    }

    @Override
    public Collection<RegisteredService> load() {
        final List<OsfOAuth20App> osfOAuth20Apps =jpaOsfDao.findAllOsfOAuth20Apps();
        final Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<>();
        for (final OsfOAuth20App app: osfOAuth20Apps) {
            final OAuthRegisteredService registeredService = new OAuthRegisteredService();
            registeredService.setId(new BigInteger(app.getObjectId(), HEX_RADIX).longValue());
            registeredService.setName(app.getName());
            registeredService.setDescription(app.getDescription());
            registeredService.setServiceId(app.getCallbackUrl());
            registeredService.setEvaluationOrder(EVALUATION_ORDER);
            registeredService.setClientId(app.getClientId());
            registeredService.setClientSecret(app.getClientSecret());
            registeredService.setBypassApprovalPrompt(Boolean.FALSE);
            registeredService.setGenerateRefreshToken(Boolean.TRUE);
            registeredService.setSupportedGrantTypes(getSupportedGrantTypes());
            registeredService.setSupportedResponseTypes(getSupportedResponseTypes());
            registeredService.setAttributeReleasePolicy(getAttributeReleasePolicy());
            registeredService.setAccessStrategy(getAccessStrategy());
            serviceMap.put(registeredService.getId(), registeredService);
        }
        this.serviceMap = serviceMap;
        return new ArrayList<>(this.serviceMap.values());
    }

    private HashSet<String> getSupportedGrantTypes() {
        return new HashSet<>(Arrays.asList(OAuth20GrantTypes.AUTHORIZATION_CODE.getType(), OAuth20GrantTypes.REFRESH_TOKEN.getType()));
    }

    private HashSet<String> getSupportedResponseTypes() {
        return new HashSet<>(Collections.singletonList(OAuth20ResponseTypes.CODE.getType()));
    }

    private ReturnAllowedAttributeReleasePolicy getAttributeReleasePolicy() {
        return new ReturnAllowedAttributeReleasePolicy(new ArrayList<>());
    }

    private DefaultRegisteredServiceAccessStrategy getAccessStrategy() {
        final DefaultRegisteredServiceAccessStrategy accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        final DefaultRegisteredServiceDelegatedAuthenticationPolicy delegatedAuthenticationPolicy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        final List<String> allowedProviders = new ArrayList<>();
        allowedProviders.addAll(casProperties.getAuthn().getOsfPostgres().getInstitutionClients());
        allowedProviders.addAll(casProperties.getAuthn().getOsfPostgres().getNonInstitutionClients());
        delegatedAuthenticationPolicy.setAllowedProviders(allowedProviders);
        delegatedAuthenticationPolicy.setPermitUndefined(false);
        accessStrategy.setDelegatedAuthenticationPolicy(delegatedAuthenticationPolicy);
        return accessStrategy;
    }
}
