package org.idstack.relyingparty.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.idstack.feature.document.MetaData;
import org.idstack.relyingparty.CorrelationScore;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * @author Chanaka Lakmal
 * @date 3/9/2017
 * @since 1.0
 */

@Component
public class Router {

    public final String apiKey = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.Configuration.API_KEY);
    public final String configFilePath = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.Configuration.CONFIG_FILE_PATH);
    public final String storeFilePath = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.Configuration.STORE_FILE_PATH);

    public String evaluateDocument(String json) {

        ArrayList<String> jsonList = new ArrayList<>();
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();

        for (int i = 1; i <= object.size(); i++) {
            jsonList.add(object.get(String.valueOf(i)).toString());
        }

        LinkedHashMap<String, double[]> scoreMap = new CorrelationScore().getMultipleDocumentScore(jsonList);
        return new Gson().toJson(scoreMap);
    }

    public String storeDocuments(MultipartHttpServletRequest request, String json, String email) throws IOException {
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();

        if (object.size() != request.getFileMap().size()) {
            return Constant.Status.STATUS_ERROR_PARAMETER;
        }

        for (int i = 1; i <= object.size(); i++) {
            JsonObject doc = object.getAsJsonObject(String.valueOf(i));
            JsonObject metadataObject = doc.getAsJsonObject(Constant.JsonAttribute.META_DATA);
            MetaData metaData = new Gson().fromJson(metadataObject.toString(), MetaData.class);
            String uuid = UUID.randomUUID().toString();
            FeatureImpl.getFactory().storeDocuments(doc.toString().getBytes(), storeFilePath, email, metaData.getDocumentType(), Constant.FileExtenstion.JSON, uuid);
            MultipartFile pdf = request.getFileMap().get(String.valueOf(i));
            FeatureImpl.getFactory().storeDocuments(pdf.getBytes(), storeFilePath, email, request.getParameter("doc-type-" + i), Constant.FileExtenstion.PDF, uuid);
        }

        for (int i = 1; i <= request.getFileMap().size(); i++) {

        }

        return Constant.Status.OK;
    }

    private FileInputStream getPropertiesFile() {
        try {
            return new FileInputStream(getClass().getClassLoader().getResource(Constant.Configuration.SYSTEM_PROPERTIES_FILE_NAME).getFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
