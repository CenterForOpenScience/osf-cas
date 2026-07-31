package io.cos.cas.osf.orcidtoken;

import io.cos.cas.osf.util.crypto.OrcidTokenCipherExecutor;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * This is {@link OsfOrcidTokenCryptoConverter}.
 *
 * Transparently encrypts / decrypts {@link OsfOrcidToken#accessToken} and {@link OsfOrcidToken#refreshToken} so that
 * ORCID's long-lived OAuth tokens are never persisted to {@code osf_orcid_oauth_token} in plaintext.
 *
 * @author Longze Chen
 * @since 26.1.0
 */
@Converter
public class OsfOrcidTokenCryptoConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(final String attribute) {
        return OrcidTokenCipherExecutor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(final String dbData) {
        return OrcidTokenCipherExecutor.decrypt(dbData);
    }
}
