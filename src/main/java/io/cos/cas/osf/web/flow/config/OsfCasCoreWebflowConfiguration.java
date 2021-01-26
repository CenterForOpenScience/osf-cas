package io.cos.cas.osf.web.flow.config;

import io.cos.cas.osf.authentication.exception.AccountNotConfirmedIdpException;
import io.cos.cas.osf.authentication.exception.AccountNotConfirmedOsfException;
import io.cos.cas.osf.authentication.exception.InstitutionSsoFailedException;
import io.cos.cas.osf.authentication.exception.InvalidOneTimePasswordException;
import io.cos.cas.osf.authentication.exception.InvalidPasswordException;
import io.cos.cas.osf.authentication.exception.InvalidUserStatusException;
import io.cos.cas.osf.authentication.exception.InvalidVerificationKeyException;
import io.cos.cas.osf.authentication.exception.OneTimePasswordRequiredException;
import io.cos.cas.osf.authentication.exception.TermsOfServiceConsentRequiredException;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link OsfCasCoreWebflowConfiguration}.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@Configuration("osfCasCoreWebflowConfiguration")
@AutoConfigureBefore(CasCoreWebflowConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OsfCasCoreWebflowConfiguration extends CasCoreWebflowConfiguration {

    @Override
    @Bean
    public Set<Class<? extends Throwable>> handledAuthenticationExceptions() {

        // OSF CAS Customization: Add newly created OSF-specific authentication exceptions to the handled error list,
        //                        which is required to enable all authentication exception handlers and respective
        //                        handling actions to handle these exceptions.
        Set<Class<? extends Throwable>> errors = new LinkedHashSet<>();
        errors.add(AccountNotConfirmedIdpException.class);
        errors.add(AccountNotConfirmedOsfException.class);
        errors.add(InvalidOneTimePasswordException.class);
        errors.add(InstitutionSsoFailedException.class);
        errors.add(InvalidPasswordException.class);
        errors.add(InvalidUserStatusException.class);
        errors.add(InvalidVerificationKeyException.class);
        errors.add(OneTimePasswordRequiredException.class);
        errors.add(TermsOfServiceConsentRequiredException.class);

        // Add built-in exceptions after OSF-specific exceptions since order matters
        errors.addAll(super.handledAuthenticationExceptions());

        return errors;
    }
}
