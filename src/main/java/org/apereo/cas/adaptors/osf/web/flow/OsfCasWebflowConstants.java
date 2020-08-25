package org.apereo.cas.adaptors.osf.web.flow;

/**
 * This is {@link OsfCasWebflowConstants}, which expands the default {@link org.apereo.cas.web.flow.CasWebflowConstants}
 * interface by adding OSF CAS customized action and state IDs.
 *
 * @author Longze Chen
 * @since 6.2.1
 */

public interface OsfCasWebflowConstants {

    String ACTION_ID_OSF_NON_INTERACTIVE_AUTHENTICATION_CHECK = "osfNonInteractiveAuthenticationCheckAction";

    String STATE_ID_OSF_NON_INTERACTIVE_AUTHENTICATION_CHECK = "osfNonInteractiveAuthenticationCheck";
}
