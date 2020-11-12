package io.cos.cas.osf.web.flow.login;

import io.cos.cas.osf.authentication.support.OsfInstitutionUtils;
import io.cos.cas.osf.dao.JpaOsfDao;
import io.cos.cas.osf.web.support.OsfCasLoginContext;

import lombok.extern.slf4j.Slf4j;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link OsfInstitutionLoginPreparationAction}.
 *
 * Extends {@link OsfAbstractLoginPreparationAction} to prepare the institution login page.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@Slf4j
public class OsfInstitutionLoginPreparationAction extends OsfAbstractLoginPreparationAction {

    @NotNull
    private final JpaOsfDao jpaOsfDao;

    public OsfInstitutionLoginPreparationAction(
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            final JpaOsfDao jpaOsfDao
    ) {
        super(
                initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy
        );
        this.jpaOsfDao = jpaOsfDao;
    }

    @Override
    protected Event doExecute(RequestContext context) {

        String target;
        String service = context.getRequestParameters().get("service");
        if (service != null) {
            service = URLEncoder.encode(service, StandardCharsets.UTF_8);
            target = URLEncoder.encode(String.format("/login?service=%s", service), StandardCharsets.UTF_8);
        } else {
            target = URLEncoder.encode("/login", StandardCharsets.UTF_8);
        }

        String institutionId = null;
        OsfCasLoginContext loginContext;
        loginContext = Optional.of(context).map(requestContext
                -> (OsfCasLoginContext) requestContext.getFlowScope().get(PARAMETER_LOGIN_CONTEXT)).orElse(null);
        if (loginContext != null) {
            institutionId = loginContext.getInstitutionId();
            if (!OsfInstitutionUtils.validateInstitutionForLogin(jpaOsfDao, institutionId)) {
                loginContext.setInstitutionId(null);
                context.getFlowScope().put(PARAMETER_LOGIN_CONTEXT, loginContext);
                institutionId = null;
            }
        }

        final Map<String, String> institutionLoginUrlMap
                = OsfInstitutionUtils.getInstitutionLoginUrlMap(jpaOsfDao, target, institutionId);
        final Map<String, String> institutionLoginUrlMapSorted;
        if (institutionId != null) {
            institutionLoginUrlMapSorted = institutionLoginUrlMap;
        } else {
            institutionLoginUrlMap.put("", " -- select an institution -- ");
            institutionLoginUrlMapSorted = OsfInstitutionUtils.sortByValue(institutionLoginUrlMap);
        }
        context.getFlowScope().put("institutions", institutionLoginUrlMapSorted);
        return success();
    }
}
