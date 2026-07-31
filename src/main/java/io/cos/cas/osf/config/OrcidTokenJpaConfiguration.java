package io.cos.cas.osf.config;

import io.cos.cas.osf.dao.JpaOsfOrcidTokenDao;
import io.cos.cas.osf.dao.OsfOrcidTokenDao;
import io.cos.cas.osf.util.crypto.OrcidTokenCipherExecutor;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.List;

/**
 * This is {@link OrcidTokenJpaConfiguration}.
 *
 * Configures a second, writable JPA persistence unit dedicated to the {@code osf_orcid_oauth_token} table. This is
 * intentionally separate from {@link JpaOsfDaoConfiguration}, which is read-only at the driver level
 * ({@code cas.authn.osf-postgres.jpa.url} is opened with {@code readOnly=true&readOnlyMode=always}) and cannot be
 * used to persist new state. Instead, this context reuses the connection settings of CAS's own writable ticket
 * registry database ({@code cas.ticket.registry.jpa.*}, {@code ddl-auto=update}), so Hibernate creates the new table
 * automatically on startup with no separate migration required.
 *
 * @author Longze Chen
 * @since 26.1.0
 */
@Configuration("orcidTokenJpaConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OrcidTokenJpaConfiguration {

    private static final List<String> ORCID_TOKEN_MODEL_PACKAGES_TO_SCAN = List.of("io.cos.cas.osf.orcidtoken");

    @Autowired
    @Qualifier("jpaBeanFactory")
    private ObjectProvider<JpaBeanFactory> jpaBeanFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void initializeOrcidTokenCipher() {
        OrcidTokenCipherExecutor.initialize(casProperties.getAuthn().getOsfOrcidRevocation().getTokenEncryptionKey());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean orcidTokenEntityManagerFactory() {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        final JpaBeanFactory factory = jpaBeanFactory.getObject();
        final JpaConfigurationContext ctx = new JpaConfigurationContext(
                factory.newJpaVendorAdapter(casProperties.getJdbc()),
                "orcidTokenContext",
                ORCID_TOKEN_MODEL_PACKAGES_TO_SCAN,
                orcidTokenDataSource());
        return factory.newEntityManagerFactoryBean(ctx, casProperties.getTicket().getRegistry().getJpa());
    }

    @Bean
    public PlatformTransactionManager orcidTokenTransactionManager(
            @Qualifier("orcidTokenEntityManagerFactory") final EntityManagerFactory emf
    ) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @Bean
    public DataSource orcidTokenDataSource() {
        return JpaBeans.newDataSource(casProperties.getTicket().getRegistry().getJpa());
    }

    @ConditionalOnMissingBean(name = "osfOrcidTokenDao")
    @Bean
    public OsfOrcidTokenDao osfOrcidTokenDao() {
        return new JpaOsfOrcidTokenDao();
    }
}
