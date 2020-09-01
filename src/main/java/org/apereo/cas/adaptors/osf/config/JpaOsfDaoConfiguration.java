package org.apereo.cas.adaptors.osf.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.osf.models.AbstractOsfModel;
import org.apereo.cas.adaptors.osf.daos.JpaOsfDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

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

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link JpaOsfDaoConfiguration}.
 *
 * Longze Chen
 * @since 6.2.1
 */
@Configuration("jpaOsfDaoConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JpaOsfDaoConfiguration {

    @Autowired
    @Qualifier("jpaBeanFactory")
    private ObjectProvider<JpaBeanFactory> jpaBeanFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public List<String> jpaOsfDaoModelPackagesToScan() {
        final Reflections reflections =
                new Reflections(new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(CentralAuthenticationService.NAMESPACE))
                        .setScanners(new SubTypesScanner(false)));
        final Set<Class<? extends AbstractOsfModel>> subTypes = reflections.getSubTypesOf(AbstractOsfModel.class);
        return subTypes.stream().map(t -> t.getPackage().getName()).collect(Collectors.<String>toList());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean jpaOsfDaoEntityManagerFactory() {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        final JpaBeanFactory factory = jpaBeanFactory.getObject();
        final JpaConfigurationContext ctx = new JpaConfigurationContext(
                jpaBeanFactory.getObject().newJpaVendorAdapter(casProperties.getJdbc()),
                "osfDaoContext",
                jpaOsfDaoModelPackagesToScan(),
                jpaOsfDaoDataSource());
        return factory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getOsfPostgres().getJpa());
    }

    @Bean
    public PlatformTransactionManager jpaOsfDaoTransactionManager(@Qualifier("jpaOsfDaoEntityManagerFactory") final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @Bean
    public DataSource jpaOsfDaoDataSource() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getOsfPostgres().getJpa());
    }

    @ConditionalOnMissingBean(name = "jpaOsfDao")
    @Bean
    public JpaOsfDao jpaOsfDao() {
        return new JpaOsfDao();
    }
}
