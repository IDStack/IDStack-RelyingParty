package org.idstack.relyingparty.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.idstack.feature.document.MetaData;
import org.idstack.relyingparty.ConfidenceScore;
import org.idstack.relyingparty.CorrelationScore;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * @author Chanaka Lakmal
 * @date 3/9/2017
 * @since 1.0
 */

@Component
public class Router {

    protected String getConfidenceScore(String json) {
        return new Gson().toJson(Collections.singletonMap("score", new ConfidenceScore().getSingleDocumentScore(json)));
    }

    protected String getCorrelationScore(String json) {
        ArrayList<String> jsonList = new ArrayList<>();
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();
        for (int i = 1; i <= object.size(); i++) {
            jsonList.add(object.get(String.valueOf(i)).toString());
        }
        LinkedHashMap<String, double[]> scores = new CorrelationScore().getMultipleDocumentScore(jsonList);
        return new Gson().toJson(scores);
    }

    protected String evaluateDocuments(FeatureImpl feature, String storeFilePath, MultipartHttpServletRequest request, String json, String email) throws IOException {
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();

        if (object.size() != request.getFileMap().size()) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PARAMETER));
        }

        for (int i = 1; i <= object.size(); i++) {
            JsonObject doc = object.getAsJsonObject(String.valueOf(i));
            JsonObject metadataObject = doc.getAsJsonObject(Constant.JsonAttribute.META_DATA);
            MetaData metaData = new Gson().fromJson(metadataObject.toString(), MetaData.class);
            String uuid = UUID.randomUUID().toString();
            feature.storeDocuments(doc.toString().getBytes(), storeFilePath, email, metaData.getDocumentType(), Constant.FileExtenstion.JSON, uuid);
            MultipartFile pdf = request.getFileMap().get(String.valueOf(i));
            feature.storeDocuments(pdf.getBytes(), storeFilePath, email, request.getParameter("doc-type-" + i), Constant.FileExtenstion.PDF, uuid);
        }

        return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.SUCCESS));
    }
}
