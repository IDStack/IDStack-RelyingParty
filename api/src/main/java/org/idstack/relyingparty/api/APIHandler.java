package org.idstack.relyingparty.api;

import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

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
    public String saveConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestBody String json) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return FeatureImpl.getFactory().saveBasicConfiguration(router.configFilePath, json);
    }

    @RequestMapping(value = {"/{version}/{apikey}/getconfig/{type}/{property}", "/{version}/{apikey}/getconfig/{type}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getConfigurationFile(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("type") String type, @PathVariable("property") Optional<String> property) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return FeatureImpl.getFactory().getConfigurationAsJson(router.configFilePath, Constant.Configuration.BASIC_CONFIG_FILE_NAME, property);
    }

    @RequestMapping(value = "/{version}/store", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String storeDocuments(@PathVariable("version") String version, @RequestParam(value = "json") String json, @RequestParam(value = "email") String email, MultipartHttpServletRequest request) throws IOException {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        return router.storeDocuments(request, json, email);
    }

    @RequestMapping(value = "/{version}/{apikey}/evaluate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String evaluateDocument(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestBody String json) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return router.evaluateDocument(json);
    }
}