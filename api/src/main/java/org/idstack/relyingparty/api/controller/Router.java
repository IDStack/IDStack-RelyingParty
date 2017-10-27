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
import org.idstack.feature.document.MetaData;
import org.idstack.feature.sign.pdf.JsonPdfMapper;
import org.idstack.feature.sign.pdf.PdfCertifier;
import org.idstack.feature.verification.ExtractorVerifier;
import org.idstack.feature.verification.SignatureVerifier;
import org.idstack.relyingparty.ConfidenceScore;
import org.idstack.relyingparty.CorrelationScore;
import org.idstack.relyingparty.response.CorrelationScoreResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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

    protected String getConfidenceScore(String json) {
        return new Gson().toJson(Collections.singletonMap(Constant.SCORE, new ConfidenceScore().getSingleDocumentScore(json)));
    }

    protected String getConfidenceScoreByUrl(String jsonUrl, String pubFilePath) {
        String json;
        try {
            URI uri = new URI(jsonUrl);
            json = new String(Files.readAllBytes(Paths.get(pubFilePath).resolve(uri.getPath().startsWith(File.separator) ? uri.getPath().substring(1) : uri.getPath())));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        return new Gson().toJson(Collections.singletonMap(Constant.SCORE, new ConfidenceScore().getSingleDocumentScore(json)));
    }

    protected String getCorrelationScore(String json) {
        JsonArray jsonList = new JsonParser().parse(json).getAsJsonObject().get(Constant.JSON_LIST).getAsJsonArray();
        CorrelationScoreResponse csr = new CorrelationScore().getMultipleDocumentScore(jsonList);
        return new Gson().toJson(csr);
    }

    protected String getCorrelationScoreByRequestId(FeatureImpl feature, String storeFilePath, String configFilePath, String requestId) {
        String json = feature.getDocuments(storeFilePath, requestId);
        JsonArray jsonList = new JsonParser().parse(json).getAsJsonObject().get(Constant.JSON_LIST).getAsJsonArray();
        CorrelationScoreResponse csr = new CorrelationScore().getMultipleDocumentScore(jsonList);
        return new Gson().toJson(csr);
    }

    protected String evaluateDocuments(FeatureImpl feature, String storeFilePath, String configFilePath, MultipartHttpServletRequest request, String json, String email, String tmpFilePath, String pubFilePath) {
        JsonArray jsonList = new JsonParser().parse(json).getAsJsonObject().get(Constant.JSON_LIST).getAsJsonArray();
        String uuid = UUID.randomUUID().toString();

        if (jsonList.size() != request.getFileMap().size())
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PARAMETER));

        Parser parser = new Parser();
        for (int i = 1; i <= jsonList.size(); i++) {
            JsonObject doc = jsonList.get(i - 1).getAsJsonObject();

            try {
                boolean isValidExtractor = extractorVerifier.verifyExtractorSignature(doc.toString(), tmpFilePath);
                if (!isValidExtractor)
                    return "Extractor's signature is not valid";
                ArrayList<Boolean> isValidValidators = signatureVerifier.verifyJson(doc.toString(), tmpFilePath);
                if (isValidValidators.contains(false))
                    return "One or more validator signatures are not valid";

                MultipartFile pdf = request.getFileMap().get(String.valueOf(i));
                String pdfUrl = feature.storeDocuments(pdf.getBytes(), storeFilePath, configFilePath, pubFilePath, email, request.getParameter(Constant.DOCUMENT_TYPE + i), Constant.FileExtenstion.PDF, uuid, i);
                String pdfPath = feature.parseUrlAsLocalFilePath(pdfUrl, pubFilePath);

                String hashInPdf = new JsonPdfMapper().getHashOfTheOriginalContent(pdfPath);
                if (hashInPdf == null)
                    return "Upload a signed pdf";

                String hashInJson = parser.parseDocumentJson(doc.toString()).getMetaData().getPdfHash();

                //TODO : uncomment after modifying hashing mechanism
                if (!(hashInJson.equals(hashInPdf))) {
                    //return "Pdf and the machine readable file are not not matching each other";
                }

                PdfCertifier pdfCertifier = new PdfCertifier();

                boolean verifiedPdf = pdfCertifier.verifySignatures(pdfPath);
                if (!verifiedPdf) {
                    return "One or more signatures in the Pdf are invalid";
                }

            } catch (OperatorCreationException | CMSException | IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }

            String docType = request.getParameter(Constant.DOCUMENT_TYPE + i);

            JsonObject metadataObject = doc.getAsJsonObject(Constant.JsonAttribute.META_DATA);
            MetaData metaData = new Gson().fromJson(metadataObject.toString(), MetaData.class);

            if (!docType.equals(metaData.getDocumentType()))
                return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PARAMETER));

            feature.storeDocuments(doc.toString().getBytes(), storeFilePath, configFilePath, pubFilePath, email, metaData.getDocumentType(), Constant.FileExtenstion.JSON, uuid, i);
        }

        return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.SUCCESS));
    }
}
