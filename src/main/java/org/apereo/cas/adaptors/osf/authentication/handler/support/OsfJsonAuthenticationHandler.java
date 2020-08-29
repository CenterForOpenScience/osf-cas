package org.apereo.cas.adaptors.osf.authentication.handler.support;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.adaptors.osf.authentication.credential.OsfJsonCredential;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.BooleanUtils;

import org.springframework.core.io.Resource;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is {@link OsfJsonAuthenticationHandler}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Slf4j
public class OsfJsonAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private final ObjectMapper mapper;
    private final Resource resource;

    @SuppressWarnings("deprecation")
    public OsfJsonAuthenticationHandler(
            final String name,
            final ServicesManager servicesManager,
            final PrincipalFactory principalFactory,
            final Integer order,
            final Resource resource
    ) {
        super(name, servicesManager, principalFactory, order);
        this.resource = resource;
        this.mapper = new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }

    @SneakyThrows
    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) {

        OsfJsonCredential originalCredential = (OsfJsonCredential) credential;
        OsfJsonCredential osfJsonCredential = (OsfJsonCredential) credential.getClass().getDeclaredConstructor().newInstance();
        BeanUtils.copyProperties(osfJsonCredential, originalCredential);
        transformUsername(osfJsonCredential);
        transformPassword(osfJsonCredential);
        LOGGER.debug("Attempting authentication internally for transformed credential [{}]", osfJsonCredential);
        return authenticateUsernamePasswordInternal(osfJsonCredential, originalCredential.getPassword());
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
            final UsernamePasswordCredential credential,
            final String originalPassword
    ) throws GeneralSecurityException, PreventedException {

        final Map<String, OsfJsonUserAccount> map = readAccountsFromResource();
        final String username = credential.getUsername();
        if (!map.containsKey(username)) {
            throw new AccountNotFoundException();
        }

        final String password = credential.getPassword();
        final OsfJsonUserAccount account = map.get(username);
        if (matches(password, account.getPassword())) {
            switch (account.getStatus()) {
                case DISABLED:
                    throw new AccountDisabledException();
                case EXPIRED:
                    throw new AccountExpiredException();
                case LOCKED:
                    throw new AccountLockedException();
                case MUST_CHANGE_PASSWORD:
                    throw new AccountPasswordMustChangeException();
                case OK:
                default:
                    LOGGER.debug("Account status is OK");
            }
            final List<MessageDescriptor> warnings = new ArrayList<>();
            if (account.getExpirationDate() != null) {
                final LocalDate now = LocalDate.now(ZoneOffset.UTC);
                if (now.isEqual(account.getExpirationDate()) || now.isAfter(account.getExpirationDate())) {
                    throw new AccountExpiredException();
                }
                if (getPasswordPolicyConfiguration() != null) {
                    final LocalDate warningPeriod = account.getExpirationDate().minusDays(
                            getPasswordPolicyConfiguration().getPasswordWarningNumberOfDays()
                    );
                    if (now.isAfter(warningPeriod) || now.isEqual(warningPeriod)) {
                        final long daysRemaining = ChronoUnit.DAYS.between(now, account.getExpirationDate());
                        final String code = "password.expiration.loginsRemaining";
                        final String message = "You have {0} logins remaining before you MUST change your password.";
                        warnings.add(new DefaultMessageDescriptor(code, message, new Serializable[]{daysRemaining}));
                    }
                }
            }
            Principal principal = this.principalFactory.createPrincipal(username, account.getAttributes());
            return createHandlerResult(credential, principal, warnings);
        }

        throw new FailedLoginException();
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return OsfJsonCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {

        if (!(credential instanceof OsfJsonCredential)) {
            LOGGER.debug("Credential is not compatible with and thus not accepted by handler [{}]", getName());
            return false;
        }
        if (this.credentialSelectionPredicate == null) {
            LOGGER.debug(
                    "No credential selection criteria is defined for handler [{}]. " +
                            "Credential is accepted for further processing",
                    getName()
            );
            return true;
        }
        LOGGER.debug("Examining credential [{}] eligibility for authentication handler [{}]", credential, getName());
        final boolean testResult = this.credentialSelectionPredicate.test(credential);
        LOGGER.debug(
                "Credential [{}] eligibility is [{}] for authentication handler [{}]",
                credential,
                getName(),
                BooleanUtils.toStringTrueFalse(testResult)
        );
        return testResult;
    }

    /**
     * Load all OSF users from a white-list JSON file which pre-defines the users and respective attributes.
     */
    private Map<String, OsfJsonUserAccount> readAccountsFromResource() throws PreventedException {
        try {
            return mapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new PreventedException(e);
        }
    }
}
