package io.cos.cas.osf.web.flow.login;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OsfInstitutionLoginPreparationAction}.
 *
 * Extends {@link OsfAbstractLoginPreparationAction} to prepare the institution login page.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
public class OsfInstitutionLoginPreparationAction extends OsfAbstractLoginPreparationAction {

    public OsfInstitutionLoginPreparationAction(
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
    protected Event doExecute(RequestContext context) {
        return error();
    }
}
