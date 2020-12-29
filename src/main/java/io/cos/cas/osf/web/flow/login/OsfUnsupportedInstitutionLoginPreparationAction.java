package io.cos.cas.osf.web.flow.login;

import lombok.extern.slf4j.Slf4j;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OsfUnsupportedInstitutionLoginPreparationAction}.
 *
 * Extends {@link OsfAbstractLoginPreparationAction} to prepare the unsupported institution login page.
 *
 * @author Longze Chen
 * @since 20.1.0
 */
@Slf4j
public class OsfUnsupportedInstitutionLoginPreparationAction extends OsfAbstractLoginPreparationAction {

    public OsfUnsupportedInstitutionLoginPreparationAction(
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
        return success();
    }
}
