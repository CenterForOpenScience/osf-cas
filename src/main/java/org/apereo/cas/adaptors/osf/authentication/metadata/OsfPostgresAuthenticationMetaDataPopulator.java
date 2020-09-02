package org.apereo.cas.adaptors.osf.authentication.metadata;

import org.apereo.cas.adaptors.osf.authentication.credential.OsfPostgresCredential;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link OsfPostgresAuthenticationMetaDataPopulator}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Getter
@ToString(callSuper = true)
@Slf4j
public class OsfPostgresAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        transaction.getPrimaryCredential().ifPresent(r -> {
            final OsfPostgresCredential credential = (OsfPostgresCredential) r;
            LOGGER.debug(
                    "Credential is of type [{}], thus adding attributes [{}, {}, {}, {}]",
                    OsfPostgresCredential.class.getSimpleName(),
                    OsfPostgresCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME,
                    OsfPostgresCredential.AUTHENTICATION_ATTRIBUTE_REMOTE_PRINCIPAL,
                    OsfPostgresCredential.AUTHENTICATION_ATTRIBUTE_DELEGATION_PROTOCOL,
                    OsfPostgresCredential.AUTHENTICATION_ATTRIBUTE_INSTITUTION_ID
            );
            builder.addAttribute(
                    OsfPostgresCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME,
                    credential.isRememberMe()
            );
            builder.addAttribute(
                    OsfPostgresCredential.AUTHENTICATION_ATTRIBUTE_REMOTE_PRINCIPAL,
                    credential.isRemotePrincipal()
            );
            builder.addAttribute(
                    OsfPostgresCredential.AUTHENTICATION_ATTRIBUTE_DELEGATION_PROTOCOL,
                    credential.getDelegationProtocol()
            );
            builder.addAttribute(
                    OsfPostgresCredential.AUTHENTICATION_ATTRIBUTE_INSTITUTION_ID,
                    credential.getInstitutionId()
            );
        });
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OsfPostgresCredential;
    }
}
