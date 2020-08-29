package org.apereo.cas.adaptors.osf.authentication.handler.support;

import org.apereo.cas.adaptors.osf.authentication.credential.OsfPostgresCredential;
import org.apereo.cas.adaptors.osf.authentication.support.AuthenticationUtils;
import org.apereo.cas.adaptors.osf.authentication.support.OsfUserStatus;
import org.apereo.cas.adaptors.osf.daos.JpaOsfDao;
import org.apereo.cas.adaptors.osf.models.OsfGuid;
import org.apereo.cas.adaptors.osf.models.OsfUser;
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

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.AccountLockedException;
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
 * @since 6.2.1
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
        transformPasswordOrVerificationKey(osfPostgresCredential);
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

        final OsfUser osfUser = jpaOsfDao.findOneUserByEmail(username);
        if (osfUser == null) {
            throw new AccountNotFoundException("User with username [" + username + "] not found");
        }
        final OsfGuid osfGuid = jpaOsfDao.findGuidByUser(osfUser);
        if (osfGuid == null) {
            throw new FailedLoginException("User with username [" + username + "] does not have a valid OSF GUID");
        }
        final String userStatus = AuthenticationUtils.verifyUserStatus(osfUser);

        if (plainTextPassword != null) {
            if (!AuthenticationUtils.verifyPassword(plainTextPassword, osfUser.getPassword())) {
                throw new FailedLoginException("Invalid password for user with username [" + username + "]");
            }
        } else if (verificationKey != null) {
            if (!verificationKey.equals(osfUser.getVerificationKey())) {
                throw new FailedLoginException("Invalid verification key for user with username [" + username + "]");
            }
        } else {
            throw new FailedLoginException("Missing credential for user with username [" + username + "]");
        }

        if (OsfUserStatus.USER_NOT_CONFIRMED_OSF.equals(userStatus)) {
            throw new AccountLockedException(
                    "User with username [" + username + "] is registered via OSF sign-up but not confirmed"
            );
        } else if (OsfUserStatus.USER_NOT_CONFIRMED_IDP.equals(userStatus)) {
            throw new AccountLockedException(
                    "User with username [" + username + "] is registered via external IdP but not confirmed "
            );
        }  else if (OsfUserStatus.USER_DISABLED.equals(userStatus)) {
            throw new AccountDisabledException("User with username [" + username + "] is disabled");
        } else if (OsfUserStatus.USER_NOT_CLAIMED.equals(userStatus)) {
            throw new AccountLockedException("User with username [" + username + "] is not claimed");
        } else if (OsfUserStatus.USER_MERGED.equals(userStatus)) {
            throw new AccountLockedException("User with username [" + username + "] has been merged into another user");
        } else if (OsfUserStatus.USER_STATUS_UNKNOWN.equals(userStatus)) {
            throw new AccountLockedException("User with username [" + username + "] is inactive with unknown status");
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
    ) throws FailedLoginException, AccountNotFoundException {
        if (StringUtils.isBlank(credential.getPassword())) {
            throw new FailedLoginException("Original password is null");
        }
        LOGGER.debug("Transforming credential password via [{}]", this.principalNameTransformer.getClass().getName());
        final String transformedPassword = this.principalNameTransformer.transform(credential.getPassword());
        if (StringUtils.isBlank(transformedPassword)) {
            throw new FailedLoginException("Transformed password is null");
        }
        credential.setPassword(transformedPassword);
    }

    private void transformPasswordOrVerificationKey(
            final OsfPostgresCredential credential
    ) throws FailedLoginException, AccountNotFoundException{
        if (StringUtils.isNotBlank(credential.getVerificationKey())) {
            LOGGER.debug(
                    "Transforming credential verification key via [{}]",
                    this.principalNameTransformer.getClass().getName()
            );
            String transformedVerificationKey
                    = this.principalNameTransformer.transform(credential.getVerificationKey());
            if (StringUtils.isBlank(transformedVerificationKey)) {
                throw new FailedLoginException("Transformed verification key null");
            }
            credential.setVerificationKey(transformedVerificationKey);
            credential.setPassword(null);
        } else {
            transformPassword(credential);
            credential.setVerificationKey(null);
        }
    }
}
