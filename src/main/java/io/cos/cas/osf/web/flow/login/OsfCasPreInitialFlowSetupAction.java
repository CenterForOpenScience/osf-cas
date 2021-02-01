package io.cos.cas.osf.web.flow.login;

import io.cos.cas.osf.configuration.model.OsfUrlProperties;

import io.cos.cas.osf.web.flow.support.OsfCasWebflowConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * This is {@link OsfCasPreInitialFlowSetupAction}.
 *
 * @author Longze Chen
 * @since 21.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class OsfCasPreInitialFlowSetupAction extends AbstractAction {

    @NotNull
    private final OsfUrlProperties osfUrlProperties;

    @Override
    protected Event doExecute(final RequestContext context) {
        final OsfUrlProperties osfUrl = Optional.of(context).map(
                requestContext -> (OsfUrlProperties) requestContext.getFlowScope().get(OsfCasWebflowConstants.FLOW_PARAMETER_OSF_URL)
        ).orElse(null);
        if (osfUrl == null) {
            context.getFlowScope().put(OsfCasWebflowConstants.FLOW_PARAMETER_OSF_URL, osfUrlProperties);
        }
        return success();
    }
}
