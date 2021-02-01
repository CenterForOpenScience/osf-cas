package io.cos.cas.osf.web.flow.login;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.web.flow.actions.AbstractAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This is {@link OsfAbstractLoginPreparationAction}.
 *
 * Extends {@link AbstractAuthenticationAction} and serve as the base abstract class for implementations of different
 * pre-login check / preparation actions. Currently, it has minimal functionalities such as 1) defining the constant
 * names for URL parameters / names amd for flow context variable names; 2) overriding a few behavior methods so they
 * are NOOP.
 *
 * @author Longze Chen
 * @since 20.1.0
 */
public abstract class OsfAbstractLoginPreparationAction extends AbstractAuthenticationAction {

    protected static final String PARAMETER_LOGIN_CONTEXT = "osfCasLoginContext";

    protected static final String PARAMETER_SERVICE = "service";

    protected static final String PARAMETER_CAMPAIGN = "campaign";

    protected static final String PARAMETER_CAMPAIGN_VALUE = "institution";

    protected static final String PARAMETER_INSTITUTION_ID = "institutionId";

    protected static final String PARAMETER_ORCID_CLIENT_TYPE = "orcid";

    protected static final String PARAMETER_CAS_CLIENT_TYPE = "cas";

    protected static final String PARAMETER_ORCID_REDIRECT = "redirectOrcid";

    protected static final String PARAMETER_ORCID_REDIRECT_VALUE = "true";

    protected static final String PARAMETER_REDIRECT_SOURCE = "casRedirectSource";

    protected static final List<String> EXPECTED_REDIRECT_CODES = new LinkedList<>(Arrays.asList("tomcat", "cas"));

    public OsfAbstractLoginPreparationAction(
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy
    ) {
        super(
                initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy
        );
    }

    @Override
    protected Event doPreExecute(RequestContext context) {
        return null;
    }

    @Override
    protected Event doExecute(RequestContext context) {
        return error();
    }

    @Override
    protected void onWarn(final RequestContext context) {
    }

    @Override
    protected void onSuccess(final RequestContext context) {
    }

    @Override
    protected void onError(final RequestContext context) {
    }
}
