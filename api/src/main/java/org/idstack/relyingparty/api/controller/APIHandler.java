package org.idstack.relyingparty.api.controller;

import com.google.gson.Gson;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Chanaka Lakmal
 * @date 3/9/2017
 * @since 1.0
 */

@RestController
public class APIHandler {

    @Autowired
    private Router router;

    @Autowired
    private FeatureImpl feature;

    @Value(value = "classpath:" + Constant.Configuration.SYSTEM_PROPERTIES_FILE_NAME)
    private Resource resource;

    private String apiKey;
    private String configFilePath;
    private String storeFilePath;
    private String tmpFilePath;
    private String pubFilePath;

    @PostConstruct
    void init() throws IOException {
        apiKey = feature.getProperty(resource.getInputStream(), Constant.Configuration.API_KEY);
        configFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.CONFIG_FILE_PATH);
        storeFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.STORE_FILE_PATH);
        tmpFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.TEMP_FILE_PATH);
        pubFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.PUB_FILE_PATH);
    }

    @RequestMapping(value = {"/", "/{version}", "/{version}/{apikey}"})
    public void root(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.sendRedirect("http://docs.idstack.apiary.io/");
    }

    /**
     * Save the configurations received at the configured URL at idstack.properties file
     *
     * @param version api version
     * @param apikey  api key
     * @param json    json of configuration
     * @return status of saving
     */
    @RequestMapping(value = "/{version}/{apikey}/saveconfig/basic", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestBody String json) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return feature.saveBasicConfiguration(configFilePath, json);
    }

    /**
     * Returned the saved configurations. If property is mentioned this returns only the mentioned property from the given type otherwise everything
     *
     * @param version  api version
     * @param apikey   api key
     * @param property property of configuration
     * @return configuration as json
     */
    @RequestMapping(value = {"/{version}/{apikey}/getconfig/basic/{property}", "/{version}/{apikey}/getconfig/{type}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("property") Optional<String> property) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return feature.getConfigurationAsJson(configFilePath, Constant.Configuration.BASIC_CONFIG_FILE_NAME, property);
    }

    /**
     * Evaluate the confidence score of single json document
     *
     * @param version api version
     * @param apikey  api key
     * @param json    signed json
     * @return confidence score
     */
    @RequestMapping(value = "/{version}/{apikey}/confidence", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getConfidenceScore(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestBody String json) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return router.getConfidenceScore(json, tmpFilePath);
    }

    /**
     * Evaluate the confidence score of single json document. Json document is sent as a URL
     *
     * @param version api version
     * @param apikey  api key
     * @param jsonUrl URL of the signed json
     * @return confidence score
     */
    @RequestMapping(value = "/{version}/{apikey}/confidence/url", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getConfidenceScoreByUrl(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "json_url") String jsonUrl) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return router.getConfidenceScoreByUrl(jsonUrl);
    }

    /**
     * Evaluate the correlation score of set of json documents
     *
     * @param version api version
     * @param apikey  api key
     * @param json    signed set of jsons
     * @return correlation score
     */
    @RequestMapping(value = "/{version}/{apikey}/correlation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getCorrelationScore(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestBody String json) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return router.getCorrelationScore(json, tmpFilePath);
    }

    /**
     * Evaluate the correlation score of set of json documents. Set of json documents are sent by request id
     *
     * @param version   api version
     * @param apikey    api key
     * @param requestId request id of the json set stored
     * @return correlation score
     */
    @RequestMapping(value = "/{version}/{apikey}/correlation/request", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getCorrelationScoreByRequestId(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "request_id") String requestId) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return router.getCorrelationScoreByRequestId(feature, storeFilePath, configFilePath, requestId);
    }

    /**
     * Get the stored documents in the configured store path
     *
     * @param version api version
     * @param apikey  api key
     * @return document list
     */
    @RequestMapping(value = "/{version}/{apikey}/getdocstore", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getStoredDocuments(@PathVariable("version") String version, @PathVariable("apikey") String apikey) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return feature.getDocumentStore(storeFilePath, configFilePath, true).replaceAll(pubFilePath, File.separator);
    }

    /**
     * Get the stored documents in the configured store path by request id
     *
     * @param version api version
     * @param apikey  api key
     * @return document list
     */
    @RequestMapping(value = "/{version}/{apikey}/getdocstore/request", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getStoredDocumentsByRequestId(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "request_id") String requestId) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return feature.getDocumentStore(storeFilePath, configFilePath, true, requestId).replaceAll(pubFilePath, File.separator);
    }

    //*************************************************** PUBLIC API ***************************************************

    /**
     * Store the signed jsons + signed pdfs received for evaluate
     *
     * @param version api version
     * @param json    set of signed jsons
     * @param email   email of the sender
     * @param request request object for access the signed pdf files
     * @return status of saving
     * @throws IOException if file cannot be converted into bytes
     */
    @RequestMapping(value = "/{version}/evaluate", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String evaluateDocuments(@PathVariable("version") String version, @RequestParam(value = "json") String json, @RequestParam(value = "email") String email, MultipartHttpServletRequest request) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        return router.evaluateDocuments(feature, storeFilePath, request, json, email, tmpFilePath);
    }
}