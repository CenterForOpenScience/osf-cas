package io.cos.cas.oauth.config;

import io.cos.cas.oauth.services.OsfPostgresServiceRegistry;
import io.cos.cas.osf.dao.JpaOsfDao;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * This is {@link OsfPostgresServiceRegistryConfiguration}, the configuration class for {@link OsfPostgresServiceRegistry}.
 *
 * @author Longze Chen
 * @since 21.1.0
 */
@Configuration("osfPostgresServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OsfPostgresServiceRegistryConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ObjectProvider<JpaOsfDao> jpaOsfDao;

    @Bean
    @ConditionalOnMissingBean(name = "osfPostgresServiceRegistry")
    public OsfPostgresServiceRegistry osfPostgresServiceRegistry() {
        return new OsfPostgresServiceRegistry(
              applicationContext,
              serviceRegistryListeners.getObject(),
              casProperties,
              jpaOsfDao.getObject()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "osfPostgresServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer osfPostgresServiceRegistryExecutionPlanConfigurer() {
        return plan -> plan.registerServiceRegistry(osfPostgresServiceRegistry());
    }
}
