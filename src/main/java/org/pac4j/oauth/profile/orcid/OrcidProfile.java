package org.pac4j.oauth.profile.orcid;

import org.pac4j.oauth.profile.OAuth20Profile;

import java.net.URI;

/**
 * <p>This class is the user profile for ORCiD with appropriate getters.</p>
 * <p>It is returned by the {@link org.pac4j.oauth.client.OrcidClient}.</p>
 *
 * @author Jens Tinglev
 * @author Longze Chen
 * @since 1.6.0
 * @version 4.0.3
 */

public class OrcidProfile extends OAuth20Profile {

    private static final long serialVersionUID = 7626472295622786149L;

    public String getOrcid() {
        return (String) getAttribute(OrcidProfileDefinition.ORCID);
    }

    public boolean getClaimed() {
        return (Boolean) getAttribute(OrcidProfileDefinition.CLAIMED);
    }

    public String getCreationMethod() {
        return (String) getAttribute(OrcidProfileDefinition.CREATION_METHOD);
    }

    @Override
    public String getTypedId() {
        // Get the user identifier with a prefix which is the profile type using simple class name without package.
        // As for OrcidProfile, this identifier is unique through all providers.
        return this.getClass().getSimpleName() + SEPARATOR + this.getId();
    }

    @Override
    public String getFirstName() {
        return (String) getAttribute(OrcidProfileDefinition.FIRST_NAME);
    }

    @Override
    public String getFamilyName() {
        return (String) getAttribute(OrcidProfileDefinition.FAMILY_NAME);
    }

    @Override
    public URI getProfileUrl() {
        return (URI) getAttribute(OrcidProfileDefinition.URI);
    }
}
