package org.idstack.relyingparty.api.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.idstack.feature.Parser;
import org.idstack.feature.configuration.BasicConfig;
import org.idstack.feature.document.Document;
import org.idstack.feature.document.MetaData;
import org.idstack.feature.verification.ExtractorVerifier;
import org.idstack.feature.verification.SignatureVerifier;
import org.idstack.relyingparty.ConfidenceScore;
import org.idstack.relyingparty.CorrelationScore;
import org.idstack.relyingparty.response.correlation.CorrelationScoreResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * @author Chanaka Lakmal
 * @date 3/9/2017
 * @since 1.0
 */

@Component
public class Router {

    @Autowired
    private ExtractorVerifier extractorVerifier;

    @Autowired
    private SignatureVerifier signatureVerifier;

    protected String getConfidenceScoreByUrl(String jsonUrl, String pubFilePath) {
        String json;
        try {
            URI uri = new URI(jsonUrl);
            json = new String(Files.readAllBytes(Paths.get(pubFilePath).resolve(uri.getPath().startsWith(File.separator) ? uri.getPath().substring(1) : uri.getPath())));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        Document document;
        try {
            document = Parser.parseDocumentJson(json);
        } catch (Exception e) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_JSON_INVALID));
        }

        return new Gson().toJson(new ConfidenceScore().getSingleDocumentScore(document));
    }

    protected String getCorrelationScoreByRequestId(FeatureImpl feature, String storeFilePath, String requestId) {
        String json = feature.getDocumentListByRequestId(storeFilePath, requestId);
        JsonArray jsonList = new JsonParser().parse(json).getAsJsonObject().get(Constant.JSON_LIST).getAsJsonArray();
        CorrelationScoreResponse csr = new CorrelationScore().getMultipleDocumentScore(jsonList);
        if (csr == null) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_JSON_INVALID));
        }
        return new Gson().toJson(csr);
    }

    protected String evaluateDocuments(FeatureImpl feature, String storeFilePath, String configFilePath, String json, String email, String tmpFilePath, String pubFilePath) {
        JsonArray jsonList = new JsonParser().parse(json).getAsJsonObject().get(Constant.JSON_LIST).getAsJsonArray();
        String uuid = UUID.randomUUID().toString();

        for (int i = 0; i < jsonList.size(); i++) {
            JsonObject doc = jsonList.get(i).getAsJsonObject();

            try {
                boolean isValidExtractor = extractorVerifier.verifyExtractorSignature(doc.toString(), tmpFilePath);
                if (!isValidExtractor) {
                    return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_EXTRACTOR_SIGNATURE));
                }

                ArrayList<Boolean> isValidValidators = signatureVerifier.verifyJson(doc.toString(), tmpFilePath);
                if (isValidValidators.contains(false)) {
                    return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VALIDATOR_SIGNATURE));
                }

            } catch (OperatorCreationException | CMSException | IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }

        for (int i = 1; i <= jsonList.size(); i++) {
            JsonObject doc = jsonList.get(i - 1).getAsJsonObject();
            JsonObject metadataObject = doc.getAsJsonObject(Constant.JsonAttribute.META_DATA);
            MetaData metaData = new Gson().fromJson(metadataObject.toString(), MetaData.class);
            feature.storeDocuments(doc.toString().getBytes(), storeFilePath, configFilePath, pubFilePath, email, metaData.getDocumentType(), Constant.FileExtenstion.JSON, uuid, i);
        }

        return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.SUCCESS));
    }

    protected String sendEmail(FeatureImpl feature, String requestId, String status, String message, String configFilePath, String storeFilePath) {
        BasicConfig basicConfig = (BasicConfig) feature.getConfiguration(configFilePath, Constant.Configuration.BASIC_CONFIG_FILE_NAME);
        String body = feature.populateEmailBodyForRelyingParty(requestId, status.toUpperCase(), message, basicConfig);
        String response = feature.sendEmail(feature.getEmailByRequestId(storeFilePath, requestId), "RELYING PARTY - IDStack Document Evaluation", body);
        return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, response));
    }
}
