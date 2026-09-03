package io.cos.cas.osf.authentication.credential;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.credential.AbstractCredential;

import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.validation.ValidationContext;

/**
 * This is {@link OsfOrcidSsoCredential}.
 *
 * @author Longze Chen
 * @since 26.2.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OsfOrcidSsoCredential extends AbstractCredential {

    /** Serial version UID. */
    private static final long serialVersionUID = 7983138918562300147L;

    /** The prefix which is added to {@link #orcidId} in {@link #getId()}. */
    public static final String CREDENTIAL_ID_PREFIX = "OrcidProfile#";

    /** Attribute name for ORCiD ID, which is released to OSF. */
    public static final String AUTHENTICATION_ATTRIBUTE_ORCID_ID = "orcidId";

    /** Attribute name for Access Token, which is released to OSF. */
    public static final String AUTHENTICATION_ATTRIBUTE_ORCID_ACCESS_TOKEN = "orcidAccessToken";

    /** Attribute name for Refresh Token, which is released to OSF. */
    public static final String AUTHENTICATION_ATTRIBUTE_ORCID_REFRESH_TOKEN = "orcidRefreshToken";

    /** ORCiD ID. */
    private String orcidId;

    /**  ORCiD Access Token. */
    private String orcidAccessToken;

    /** ORCiD Refresh Token. */
    private String orcidRefreshToken;

    /**
     * Get the unique identifier for this credential.
     *
     * @return the credential ID, formed as {@code CREDENTIAL_ID_PREFIX + orcidId}
     */
    @Override
    public String getId() {
        return CREDENTIAL_ID_PREFIX + this.getOrcidId();
    }

    /**
     * Check if credential is valid. {@link #orcidId} and {@link #orcidAccessToken} must not be null or empty.
     *
     * @return {@code true} if both {@code orcidId} and {@code orcidAccessToken} are non-null and non-blank,
     *         {@code false} otherwise
     */
    @Override
    @JsonIgnore
    public boolean isValid() {
        return StringUtils.isNoneBlank(this.orcidId, this.orcidAccessToken);
    }

    /**
     * Validate this credential, adding an error message to the given context if it is not valid.
     *
     * @param context the validation context to which any error messages are added
     */
    @Override
    public void validate(final ValidationContext context) {
        if (!isValid()) {
            val messages = context.getMessageContext();
            messages.addMessage(new MessageBuilder()
                    .error()
                    .source("token")
                    .defaultText("Unable to accept credential with an empty or unspecified ORCiD ID and/or tokens")
                    .build());
        }
    }
}
