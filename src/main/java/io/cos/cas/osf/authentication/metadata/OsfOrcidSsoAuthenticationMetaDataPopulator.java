package io.cos.cas.osf.authentication.metadata;

import io.cos.cas.osf.authentication.credential.OsfOrcidSsoCredential;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;

/**
 * This is {@link OsfOrcidSsoAuthenticationMetaDataPopulator}.
 *
 * @author Longze Chen
 * @since 26.1.0
 */
@Getter
@ToString(callSuper = true)
@Slf4j
public class OsfOrcidSsoAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    /** Add attribute to authentication metadata. */
    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        transaction.getPrimaryCredential().ifPresent(r -> {
            final OsfOrcidSsoCredential credential = (OsfOrcidSsoCredential) r;
            LOGGER.info(
                    "[ORCiD SSO] Credential is of type [{}], thus adding attributes [{}, {}, and optionally {} if not null/blank)]",
                    OsfOrcidSsoCredential.class.getSimpleName(),
                    OsfOrcidSsoCredential.AUTHENTICATION_ATTRIBUTE_ORCID_ID,
                    OsfOrcidSsoCredential.AUTHENTICATION_ATTRIBUTE_ORCID_ACCESS_TOKEN,
                    OsfOrcidSsoCredential.AUTHENTICATION_ATTRIBUTE_ORCID_REFRESH_TOKEN
            );
            builder.addAttribute(
                    OsfOrcidSsoCredential.AUTHENTICATION_ATTRIBUTE_ORCID_ID,
                    credential.getOrcidId()
            );
            builder.addAttribute(
                    OsfOrcidSsoCredential.AUTHENTICATION_ATTRIBUTE_ORCID_ACCESS_TOKEN,
                    credential.getOrcidAccessToken()
            );
            final String refreshToken = credential.getOrcidRefreshToken();
            if (StringUtils.isNotBlank(refreshToken)) {
                builder.addAttribute(
                        OsfOrcidSsoCredential.AUTHENTICATION_ATTRIBUTE_ORCID_REFRESH_TOKEN,
                        refreshToken
                );
            }
        });
    }

    /** {@link OsfOrcidSsoAuthenticationMetaDataPopulator} only supports {@link OsfOrcidSsoCredential} */
    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OsfOrcidSsoCredential;
    }
}
