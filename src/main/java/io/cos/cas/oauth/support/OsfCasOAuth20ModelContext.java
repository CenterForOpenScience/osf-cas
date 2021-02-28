package io.cos.cas.oauth.support;

import io.cos.cas.osf.configuration.model.OsfUrlProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OsfCasOAuth20ModelContext}.
 *
 * This class stores information that are required by the front-end template for OSF CAS OAuth 2.0 views.
 *
 * @author Longze Chen
 * @since 21.x.0
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
@val
public class OsfCasOAuth20ModelContext {

    private String errorCode = "";

    private String errorMsg = "";

    private String errorParam = "";

    private OsfUrlProperties osfUrl = new OsfUrlProperties();

    /**
     * Retrieve the model map that can be passed to {@link org.springframework.web.servlet.ModelAndView#ModelAndView(String, Map)}.
     *
     * @return a model map
     */
    public Map<String, ?> getModelContextMap() {
        val modelMap = new HashMap<String, Object>();
        modelMap.put(OsfCasOAuth20Constants.ERROR_NAME, this.errorCode);
        modelMap.put(OsfCasOAuth20Constants.ERROR_MSG, this.errorMsg);
        modelMap.put(OsfCasOAuth20Constants.ERROR_PARAM, this.errorParam);
        modelMap.put(OsfCasOAuth20Constants.OSF_URL, this.osfUrl);
        return modelMap;
    }
}
