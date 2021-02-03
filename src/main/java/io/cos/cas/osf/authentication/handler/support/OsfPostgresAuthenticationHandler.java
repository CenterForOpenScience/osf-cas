package io.cos.cas.osf.authentication.handler.support;

import io.cos.cas.osf.authentication.credential.OsfPostgresCredential;
import io.cos.cas.osf.authentication.exception.AccountNotConfirmedIdpException;
import io.cos.cas.osf.authentication.exception.AccountNotConfirmedOsfException;
import io.cos.cas.osf.authentication.exception.InvalidOneTimePasswordException;
import io.cos.cas.osf.authentication.exception.InvalidPasswordException;
import io.cos.cas.osf.authentication.exception.InvalidUserStatusException;
import io.cos.cas.osf.authentication.exception.OneTimePasswordRequiredException;
import io.cos.cas.osf.authentication.exception.InvalidVerificationKeyException;
import io.cos.cas.osf.authentication.exception.TermsOfServiceConsentRequiredException;
import io.cos.cas.osf.authentication.support.DelegationProtocol;
import io.cos.cas.osf.authentication.support.OsfUserStatus;
import io.cos.cas.osf.authentication.support.OsfUserUtils;
import io.cos.cas.osf.authentication.support.OsfPasswordUtils;
import io.cos.cas.osf.authentication.support.TotpUtils;
import io.cos.cas.osf.dao.JpaOsfDao;
import io.cos.cas.osf.model.OsfGuid;
import io.cos.cas.osf.model.OsfTotp;
import io.cos.cas.osf.model.OsfUser;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.transforms.NoOpPrincipalNameTransformer;

import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link OsfPostgresAuthenticationHandler}.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@Getter
@Setter
@Slf4j
public class OsfPostgresAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    @NotNull
    private PrincipalNameTransformer principalNameTransformer = new NoOpPrincipalNameTransformer();

    @NotNull
    private final JpaOsfDao jpaOsfDao;

    public OsfPostgresAuthenticationHandler(
            final String name,
            final ServicesManager servicesManager,
            final PrincipalFactory principalFactory,
            final Integer order,
            final JpaOsfDao jpaOsfDao
    ) {
        super(name, servicesManager, principalFactory, order);
        this.jpaOsfDao = jpaOsfDao;
    }

    @Override
    protected final AuthenticationHandlerExecutionResult doAuthentication(
            Credential credential
    ) throws GeneralSecurityException {
        OsfPostgresCredential osfPostgresCredential = (OsfPostgresCredential) credential;
        transformUsername(osfPostgresCredential);
        if (osfPostgresCredential.isRemotePrincipal()) {
            osfPostgresCredential.setPassword(null);
            osfPostgresCredential.setVerificationKey(null);
        } else {
            transformPasswordOrVerificationKey(osfPostgresCredential);
            transformOneTimePassword(osfPostgresCredential);
        }
        LOGGER.debug("Attempting authentication internally for transformed credential [{}]",osfPostgresCredential);
        return authenticateOsfPostgresInternal(osfPostgresCredential);
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return OsfPostgresCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OsfPostgresCredential;
    }

    protected final AuthenticationHandlerExecutionResult authenticateOsfPostgresInternal(
            final OsfPostgresCredential credential
    ) throws GeneralSecurityException {

        final String username = credential.getUsername();
        final String plainTextPassword = credential.getPassword();
        final String verificationKey = credential.getVerificationKey();
        final String oneTimePassword = credential.getOneTimePassword();
        final String institutionId = credential.getInstitutionId();
        final boolean isRememberMe = credential.isRememberMe();
        final boolean isTermsOfServiceChecked = credential.isTermsOfServiceChecked();
        final boolean isRemotePrincipal = credential.isRemotePrincipal();
        final DelegationProtocol delegationProtocol = credential.getDelegationProtocol();

        LOGGER.debug(
                "Credential metadata: username=[{}], password=[{}], verificationKey=[{}], oneTimePassword=[{}], " +
                        "rememberMe=[{}], remotePrincipal=[{}], institutionId=[{}], delegationProtocol=[{}]",
                username,
                StringUtils.isNoneBlank(plainTextPassword),
                StringUtils.isNoneBlank(verificationKey),
                StringUtils.isNoneBlank(oneTimePassword),
                isRememberMe,
                isRemotePrincipal,
                institutionId,
                delegationProtocol
        );

        final OsfUser osfUser = jpaOsfDao.findOneUserByEmail(username);
        if (osfUser == null) {
            throw new AccountNotFoundException("User [" + username + "] not found");
        }
        final OsfGuid osfGuid = jpaOsfDao.findGuidByUser(osfUser);
        if (osfGuid == null) {
            throw new InvalidUserStatusException("User [" + username + "] does not have a valid OSF GUID");
        }
        final String userStatus = OsfUserUtils.verifyUserStatus(osfUser);

        if (isRemotePrincipal) {
            LOGGER.info("Skip password and verification key check for institution SSO [" + username + "] @ [" + institutionId +"]");
        } else {
            if (plainTextPassword != null) {
                if (!OsfPasswordUtils.verifyPassword(plainTextPassword, osfUser.getPassword())) {
                    throw new InvalidPasswordException("Invalid password for user [" + username + "]");
                }
            } else if (verificationKey != null) {
                if (!verificationKey.equals(osfUser.getVerificationKey())) {
                    throw new InvalidVerificationKeyException("Invalid verification key for user [" + username + "]");
                }
            } else {
                LOGGER.info("Missing credential for user [" + username + "] @ [" + institutionId +"]");
                throw new FailedLoginException("Missing credential for user [" + username + "]");
            }
        }

        final OsfTotp osfTotp = jpaOsfDao.findOneTotpByOwnerId(osfUser.getId());
        if (osfTotp != null && osfTotp.isActive()) {
            if (oneTimePassword == null) {
                throw new OneTimePasswordRequiredException("2FA TOTP required for user [" + username + "]");
            }
            try {
                final long transformedOneTimePassword = Long.parseLong(oneTimePassword);
                if (!TotpUtils.checkCode(osfTotp.getTotpSecretBase32(), transformedOneTimePassword)) {
                    throw new InvalidOneTimePasswordException("Invalid 2FA TOTP for user [" + username + "] (Type 1)");
                }
            } catch (final Exception e) {
                throw new InvalidOneTimePasswordException("Invalid 2FA TOTP for user [" + username + "] (Type 2)");
            }
        }

        if (!osfUser.isTermsOfServiceAccepted() && !isTermsOfServiceChecked) {
            LOGGER.info("Terms of service consent is required for [" + username + "]");
            throw new TermsOfServiceConsentRequiredException("Terms of service consent is required for [" + username + "]");
        }

        if (OsfUserStatus.USER_NOT_CONFIRMED_OSF.equals(userStatus)) {
            throw new AccountNotConfirmedOsfException(
                    "User [" + username + "] is registered via OSF but not confirmed"
            );
        } else if (OsfUserStatus.USER_NOT_CONFIRMED_IDP.equals(userStatus)) {
            throw new AccountNotConfirmedIdpException(
                    "User [" + username + "] is registered via external IdP but not confirmed"
            );
        }  else if (OsfUserStatus.USER_DISABLED.equals(userStatus)) {
            throw new AccountDisabledException("User [" + username + "] is disabled");
        } else if (OsfUserStatus.USER_NOT_CLAIMED.equals(userStatus)) {
            throw new InvalidUserStatusException("User [" + username + "] is not claimed");
        } else if (OsfUserStatus.USER_MERGED.equals(userStatus)) {
            throw new InvalidUserStatusException("User [" + username + "] has been merged into another user");
        } else if (OsfUserStatus.USER_STATUS_UNKNOWN.equals(userStatus)) {
            throw new InvalidUserStatusException("User [" + username + "] is inactive with unknown status");
        }

        final Map<String, List<Object>> attributesToRelease = new LinkedHashMap<>();
        attributesToRelease.put("osfGuid", Collections.singletonList(osfGuid.getGuid()));
        attributesToRelease.put("username", Collections.singletonList(osfUser.getUsername()));
        attributesToRelease.put("givenName", Collections.singletonList(osfUser.getGivenName()));
        attributesToRelease.put("familyName", Collections.singletonList(osfUser.getFamilyName()));
        final Principal principal = this.principalFactory.createPrincipal(osfGuid.getGuid(), attributesToRelease);
        final List<MessageDescriptor> warnings = new ArrayList<>();
        return createHandlerResult(credential, principal, warnings);
    }

    private void transformUsername(final OsfPostgresCredential credential) throws AccountNotFoundException {
        if (StringUtils.isBlank(credential.getUsername())) {
            throw new AccountNotFoundException("Original username is null");
        }
        LOGGER.debug("Transforming credential username via [{}]", this.principalNameTransformer.getClass().getName());
        final String transformedUsername = this.principalNameTransformer.transform(credential.getUsername());
        if (StringUtils.isBlank(transformedUsername)) {
            throw new AccountNotFoundException("Transformed username is null");
        }
        credential.setUsername(transformedUsername.toLowerCase());
    }

    private void transformPassword(
            final OsfPostgresCredential credential
    ) throws InvalidPasswordException {
        if (StringUtils.isBlank(credential.getPassword())) {
            throw new InvalidPasswordException("Original password is null");
        }
        LOGGER.debug("Transforming credential password via [{}]", this.principalNameTransformer.getClass().getName());
        final String transformedPassword = this.principalNameTransformer.transform(credential.getPassword());
        if (StringUtils.isBlank(transformedPassword)) {
            throw new InvalidPasswordException("Transformed password is null");
        }
        credential.setPassword(transformedPassword);
    }

    private void transformPasswordOrVerificationKey(
            final OsfPostgresCredential credential
    ) throws InvalidPasswordException, InvalidVerificationKeyException {
        if (StringUtils.isNotBlank(credential.getVerificationKey())) {
            LOGGER.debug(
                    "Transforming credential verification key via [{}]",
                    this.principalNameTransformer.getClass().getName()
            );
            String transformedVerificationKey
                    = this.principalNameTransformer.transform(credential.getVerificationKey());
            if (StringUtils.isBlank(transformedVerificationKey)) {
                throw new InvalidVerificationKeyException("Transformed verification key null");
            }
            credential.setVerificationKey(transformedVerificationKey);
            credential.setPassword(null);
        } else {
            transformPassword(credential);
            credential.setVerificationKey(null);
        }
    }

    private void transformOneTimePassword(final OsfPostgresCredential credential) {
        if (!StringUtils.isBlank(credential.getOneTimePassword())) {
            LOGGER.debug(
                    "Transforming credential one-time password via [{}]",
                    this.principalNameTransformer.getClass().getName()
            );
            credential.setOneTimePassword(this.principalNameTransformer.transform(credential.getOneTimePassword()));
        } else {
            LOGGER.debug("Original one-time password is null");
        }
    }
}
