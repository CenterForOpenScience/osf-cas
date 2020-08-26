package org.apereo.cas.adaptors.generic;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.adaptors.osf.authentication.credential.OsfCredential;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.transforms.NoOpPrincipalNameTransformer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

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
import java.util.Map;

/**
 * This is {@link JsonResourceAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @author Longze Chen
 * @since 5.3.0
 */
@Slf4j
public class JsonResourceAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private final ObjectMapper mapper;
    private final Resource resource;
    private final PrincipalNameTransformer principalNameTransformer;

    public JsonResourceAuthenticationHandler(
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
        this.principalNameTransformer = new NoOpPrincipalNameTransformer();
    }

    @SneakyThrows
    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) {

        LOGGER.debug("The credential class is [{}]", credential.getClass().getSimpleName());
        val originalCredential = (OsfCredential) credential;
        LOGGER.debug("The credential class has been casted to [{}]", OsfCredential.class.getSimpleName());
        val osfCredential = (OsfCredential) credential.getClass().getDeclaredConstructor().newInstance();
        BeanUtils.copyProperties(osfCredential, originalCredential);
        LOGGER.debug("A new credential has been created with copied properties");

        transformUsername(osfCredential);
        transformPasswordOrVerificationKey(osfCredential);
        LOGGER.debug("Attempting authentication internally for transformed credential [{}]",osfCredential);
        return authenticateUsernamePasswordInternal(osfCredential, originalCredential.getPassword());
    }

    @SneakyThrows
    protected void transformPasswordOrVerificationKey(final OsfCredential osfCredential) {

        if (StringUtils.isNotBlank(osfCredential.getVerificationKey())) {
            LOGGER.debug("Verification key found, deprecate password transformation");
            LOGGER.debug(
                    "Transforming credential verification key via [{}]",
                    this.principalNameTransformer.getClass().getName()
            );
            val transformedVerificationKey
                    = this.principalNameTransformer.transform(osfCredential.getVerificationKey());
            if (StringUtils.isBlank(transformedVerificationKey)) {
                throw new AccountNotFoundException("Transformed verification key null.");
            }
            osfCredential.setVerificationKey(transformedVerificationKey);
            osfCredential.setPassword(null);
        } else {
            LOGGER.debug("Verification key is null, go on to password transformation");
            transformPassword(osfCredential);
            osfCredential.setVerificationKey(null);
        }
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
            final UsernamePasswordCredential credential,
            final String originalPassword
    ) throws GeneralSecurityException, PreventedException {

        val map = readAccountsFromResource();
        val username = credential.getUsername();
        if (!map.containsKey(username)) {
            throw new AccountNotFoundException();
        }

        val password = credential.getPassword();
        val verificationKey = ((OsfCredential) credential).getVerificationKey();
        val account = map.get(username);
        val accountVerificationKey = account.getAttributes().get("verificationKey");
        val isVerificationKeyMatch = accountVerificationKey.contains(verificationKey);
        val isPasswordMatch = password != null && matches(password, account.getPassword());
        LOGGER.debug(
                "[isPasswordMatch, isVerificationKeyMatch] = [{}, {}]",
                isPasswordMatch,
                isVerificationKeyMatch
        );
        if (isVerificationKeyMatch || isPasswordMatch) {
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

            val warnings = new ArrayList<MessageDescriptor>();
            if (account.getExpirationDate() != null) {
                val now = LocalDate.now(ZoneOffset.UTC);
                if (now.isEqual(account.getExpirationDate()) || now.isAfter(account.getExpirationDate())) {
                    throw new AccountExpiredException();
                }
                if (getPasswordPolicyConfiguration() != null) {
                    val warningPeriod = account.getExpirationDate()
                            .minusDays(getPasswordPolicyConfiguration().getPasswordWarningNumberOfDays());
                    if (now.isAfter(warningPeriod) || now.isEqual(warningPeriod)) {
                        val daysRemaining = ChronoUnit.DAYS.between(now, account.getExpirationDate());
                        warnings.add(new DefaultMessageDescriptor(
                                "password.expiration.loginsRemaining",
                                "You have {0} logins remaining before you MUST change your password.",
                                new Serializable[]{daysRemaining}));
                    }
                }
            }
            val principal = this.principalFactory.createPrincipal(username, account.getAttributes());
            return createHandlerResult(credential, principal, warnings);
        }

        throw new FailedLoginException();
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {

        LOGGER.debug("supports() ... ");

        val isOsfCredentialAssignableFrom = OsfCredential.class.isAssignableFrom(clazz);
        LOGGER.debug(
                "Is [{}] assignable from [{}]? -> [{}]",
                OsfCredential.class.getSimpleName(),
                clazz.getSimpleName(),
                isOsfCredentialAssignableFrom
        );
        return isOsfCredentialAssignableFrom;
    }

    @Override
    public boolean supports(final Credential credential) {

        val isInstanceOfOsfCredential = credential instanceof OsfCredential;
        LOGGER.debug(
                "Is [{}] an instance of [{}]? -> [{}]",
                credential.getClass().getSimpleName(),
                OsfCredential.class.getSimpleName(),
                isInstanceOfOsfCredential
        );

        if (!isInstanceOfOsfCredential) {
            LOGGER.debug("Credential is not compatible with OSF and is not accepted by handler [{}]", getName());
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
        val result = this.credentialSelectionPredicate.test(credential);
        LOGGER.debug(
                "Credential [{}] eligibility is [{}] for authentication handler [{}]",
                credential,
                getName(),
                BooleanUtils.toStringTrueFalse(result)
        );
        return result;
    }

    private Map<String, CasUserAccount> readAccountsFromResource() throws PreventedException {
        try {
            return mapper.readValue(resource.getInputStream(),
                    new TypeReference<>() {
                    });
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new PreventedException(e);
        }
    }
}
