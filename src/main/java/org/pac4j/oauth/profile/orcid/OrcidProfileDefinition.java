package org.pac4j.oauth.profile.orcid;

import static org.pac4j.core.profile.AttributeLocation.PROFILE_ATTRIBUTE;

import org.pac4j.core.profile.converter.Converters;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oauth.profile.definition.OAuth20ProfileDefinition;
import org.pac4j.scribe.model.OrcidToken;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;

/**
 * This class is the Orcid profile definition.
 *
 * @author Jens Tinglev
 * @author Longze Chen
 * @since 1.6.0
 * @version 4.0.3
 */
public class OrcidProfileDefinition extends OAuth20ProfileDefinition<OrcidProfile, OAuth20Configuration> {

    public static final String ORCID = "common:path";
    public static final String GIVEN_NAME = "personal-details:given-names";
    public static final String FAMILY_NAME = "personal-details:family-name";
    public static final String URI = "common:uri";
    public static final String CREATION_METHOD = "history:creation-method";
    public static final String CLAIMED = "history:claimed";

    public OrcidProfileDefinition() {
        super(x -> new OrcidProfile());
        primary(ORCID, Converters.STRING);
        primary(GIVEN_NAME, Converters.STRING);
        primary(FAMILY_NAME, Converters.STRING);
        primary(URI, Converters.URL);
        primary(CREATION_METHOD, Converters.STRING);
        primary(CLAIMED, Converters.BOOLEAN);
    }

    @Override
    public String getProfileUrl(final OAuth2AccessToken accessToken, final OAuth20Configuration configuration) {
        if (accessToken instanceof OrcidToken) {
            return String.format("https://pub.orcid.org/v2.0/%s/record", ((OrcidToken) accessToken).getOrcid());
        } else {
            throw new OAuthException("Token in getProfileUrl is not an OrcidToken");
        }
    }

    @Override
    public OrcidProfile extractUserProfile(String body) {
        OrcidProfile profile = newProfile();
        if (body == null || body.isEmpty()) {
            raiseProfileExtractionError(body);
        }
        profile.setId(CommonHelper.substringBetween(body, "<" + ORCID + ">", "</" + ORCID + ">"));
        for(final String attribute : getPrimaryAttributes()) {
            convertAndAdd(profile, PROFILE_ATTRIBUTE, attribute,
                    CommonHelper.substringBetween(body, "<" + attribute + ">", "</" + attribute + ">"));
        }
        return profile;
    }
}
