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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OsfOrcidSsoCredential extends AbstractCredential {

    private static final long serialVersionUID = 7983138918562300147L;

    public static final String CREDENTIAL_ID_PREFIX = "OrcidProfile#";

    public static final String AUTHENTICATION_ATTRIBUTE_ORCID_ID = "orcidId";

    public static final String AUTHENTICATION_ATTRIBUTE_ORCID_ACCESS_TOKEN = "orcidAccessToken";

    public static final String AUTHENTICATION_ATTRIBUTE_ORCID_REFRESH_TOKEN = "orcidRefreshToken";

    private String orcidId;

    private String orcidAccessToken;

    private String orcidRefreshToken;

    @Override
    public String getId() {
        return CREDENTIAL_ID_PREFIX + this.getOrcidId();
    }

    @Override
    @JsonIgnore
    public boolean isValid() {
        return StringUtils.isNotBlank(getId())
                && StringUtils.isNotBlank(getOrcidAccessToken())
                && StringUtils.isNotBlank(getOrcidRefreshToken());
    }

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
