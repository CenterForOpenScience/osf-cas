package io.cos.cas.osf.web.config;

import io.cos.cas.osf.dao.OsfOrcidTokenDao;
import io.cos.cas.osf.web.rest.OrcidTokenRevocationController;

import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link OrcidTokenRevocationWebConfiguration}.
 *
 * Registers {@link OrcidTokenRevocationController} as a Spring MVC controller bean; CAS discovers {@code @Controller}
 * beans regardless of how they were registered (auto-config bean method here, not classpath component-scan).
 *
 * @author Longze Chen
 * @since 26.1.0
 */
@Configuration("orcidTokenRevocationWebConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OrcidTokenRevocationWebConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ObjectProvider<OsfOrcidTokenDao> osfOrcidTokenDao;

    @ConditionalOnMissingBean(name = "orcidTokenRevocationController")
    @Bean
    public OrcidTokenRevocationController orcidTokenRevocationController() {
        return new OrcidTokenRevocationController(
                osfOrcidTokenDao.getObject(),
                casProperties.getAuthn().getOsfOrcidRevocation().getSharedSecret(),
                casProperties.getAuthn().getOsfOrcidRevocation().getRevokeUrl(),
                casProperties.getAuthn().getPac4j().getOrcid().getId(),
                casProperties.getAuthn().getPac4j().getOrcid().getSecret()
        );
    }
}
