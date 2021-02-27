package io.cos.cas.oauth.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.CollectionUtils;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * This class adds customized features to support OAuth 2.0 for OSF CAS that are not provided by its vanilla Apereo CAS counterpart.
 *
 * See {@link org.apereo.cas.support.oauth.util.OAuth20Utils}.
 *
 * @author Longze Chen
 * @since 21.x.0
 */
@Slf4j
@UtilityClass
public class OsfCasOAuth20Utils {

    private static final ObjectMapper MAPPER
            = new ObjectMapper().findAndRegisterModules().configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);

    /**
     * Write to the output this error with customized status.
     *
     * @param response the response
     * @param error    the error message
     * @param status   an {@link HttpStatus} object
     * @return json-backed view.
     */
    public static ModelAndView writeError(final HttpServletResponse response, final String error, final HttpStatus status) {
        val model = CollectionUtils.wrap(OAuth20Constants.ERROR, error);
        val mv = new ModelAndView(new MappingJackson2JsonView(MAPPER), (Map) model);
        mv.setStatus(status);
        response.setStatus(status.value());
        return mv;
    }
}
