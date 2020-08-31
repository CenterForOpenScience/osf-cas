package org.apereo.cas.adaptors.osf.web.flow.config;

import org.apereo.cas.adaptors.osf.authentication.exceptions.AccountNotConfirmedIdpException;
import org.apereo.cas.adaptors.osf.authentication.exceptions.AccountNotConfirmedOsfException;
import org.apereo.cas.adaptors.osf.authentication.exceptions.InvalidOneTimePasswordException;
import org.apereo.cas.adaptors.osf.authentication.exceptions.InvalidPasswordException;
import org.apereo.cas.adaptors.osf.authentication.exceptions.InvalidUserStatusException;
import org.apereo.cas.adaptors.osf.authentication.exceptions.InvalidVerificationKeyException;
import org.apereo.cas.adaptors.osf.authentication.exceptions.OneTimePasswordRequiredException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * This is {@link OsfCasCoreWebflowConfiguration}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Configuration("osfCasCoreWebflowConfiguration")
@AutoConfigureBefore(CasCoreWebflowConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class OsfCasCoreWebflowConfiguration extends CasCoreWebflowConfiguration {

    @Override
    @Bean
    public Set<Class<? extends Throwable>> handledAuthenticationExceptions() {
        Set<Class<? extends Throwable>> errors = super.handledAuthenticationExceptions();
        // OSF CAS Customization: Add newly created OSF-specific authentication exceptions to the handled error list,
        //                        which is required to enable all authentication exception handlers and respective
        //                        handling actions to handle these exceptions.
        errors.add(AccountNotConfirmedIdpException.class);
        errors.add(AccountNotConfirmedOsfException.class);
        errors.add(InvalidOneTimePasswordException.class);
        errors.add(InvalidPasswordException.class);
        errors.add(InvalidUserStatusException.class);
        errors.add(InvalidVerificationKeyException.class);
        errors.add(OneTimePasswordRequiredException.class);
        return errors;
    }
}
