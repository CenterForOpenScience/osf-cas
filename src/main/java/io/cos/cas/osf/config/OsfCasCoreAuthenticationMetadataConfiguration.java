package io.cos.cas.osf.config;

import io.cos.cas.osf.authentication.metadata.OsfOrcidSsoAuthenticationMetaDataPopulator;
import io.cos.cas.osf.authentication.metadata.OsfPostgresAuthenticationMetaDataPopulator;

import lombok.extern.slf4j.Slf4j;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link OsfCasCoreAuthenticationMetadataConfiguration}.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@Configuration("osfCasCoreAuthenticationMetadataConfiguration")
@AutoConfigureBefore(CasCoreAuthenticationMetadataConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class OsfCasCoreAuthenticationMetadataConfiguration extends CasCoreAuthenticationMetadataConfiguration {

    @Bean
    public AuthenticationMetaDataPopulator osfPostgresAuthenticationMetaDataPopulator() {
        return new OsfPostgresAuthenticationMetaDataPopulator();
    }

    @Bean
    public AuthenticationMetaDataPopulator osfOrcidSsoAuthenticationMetaDataPopulator() {
        return new OsfOrcidSsoAuthenticationMetaDataPopulator();
    }

    @Bean
    public AuthenticationEventExecutionPlanConfigurer casCoreAuthenticationMetadataAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationMetadataPopulator(successfulHandlerMetaDataPopulator());
            plan.registerAuthenticationMetadataPopulator(rememberMeAuthenticationMetaDataPopulator());

            // Register OsfPostgresAuthenticationMetaDataPopulator
            plan.registerAuthenticationMetadataPopulator(osfPostgresAuthenticationMetaDataPopulator());
            LOGGER.info(
                    "Register [{}] to metadata authentication event execution plan",
                    OsfPostgresAuthenticationMetaDataPopulator.class.getSimpleName()
            );

            // Register OsfOrcidSsoAuthenticationMetaDataPopulator
            plan.registerAuthenticationMetadataPopulator(osfOrcidSsoAuthenticationMetaDataPopulator());
            LOGGER.info(
                    "Register [{}] to metadata authentication event execution plan",
                    OsfOrcidSsoAuthenticationMetaDataPopulator.class.getSimpleName()
            );

            plan.registerAuthenticationMetadataPopulator(authenticationCredentialTypeMetaDataPopulator());
            plan.registerAuthenticationMetadataPopulator(authenticationDateMetaDataPopulator());
            plan.registerAuthenticationMetadataPopulator(credentialCustomFieldsAttributeMetaDataPopulator());
        };
    }
}
