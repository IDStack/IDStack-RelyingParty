package org.idstack.relyingparty.api;

import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Chanaka Lakmal
 * @date 3/9/2017
 * @since 1.0
 */

@RestController
public class APIHandler {

    @Autowired
    Router router;

    @RequestMapping("/")
    public void root(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.sendRedirect("http://idstack.one/relyingparty");
    }

    @RequestMapping(value = "/{version}/{apikey}/saveconfig/basic", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveBasicConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestBody String json) {
        if (!FeatureImpl.getFactory().validateRequest(version, router.apiKey, apikey))
            return Constant.Status.ERROR_REQUEST;
        return FeatureImpl.getFactory().saveBasicConfiguration(router.configFilePath, json);
    }

    @RequestMapping(value = "/{version}/getconfig/{type}/{property}", method = RequestMethod.GET)
    @ResponseBody
    public Object getConfigurationFile(@PathVariable("version") String version, @PathVariable("type") String type, @PathVariable("property") String property) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.ERROR_REQUEST;
        return FeatureImpl.getFactory().getConfiguration(router.configFilePath, Constant.Configuration.BASIC_CONFIG_FILE_NAME, property);
    }

    @RequestMapping(value = "/{version}/store", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String storeDocuments(@PathVariable("version") String version, @RequestBody String json, @RequestHeader("Token") String token) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.ERROR_REQUEST;
        return router.storeDocuments(json, token);
    }

    @RequestMapping(value = "/{version}/{apikey}/evaluate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String evaluateDocument(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestBody String json) {
        if (!FeatureImpl.getFactory().validateRequest(version, router.apiKey, apikey))
            return Constant.Status.ERROR_REQUEST;
        return router.evaluateDocument(json);
    }
}