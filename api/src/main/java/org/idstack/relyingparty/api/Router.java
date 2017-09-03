package org.idstack.relyingparty.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.idstack.relyingparty.Score;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author Chanaka Lakmal
 * @date 3/9/2017
 * @since 1.0
 */

@Component
public class Router {

    public final String configFilePath = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.CONFIG_FILE_PATH);
    public final String storeFilePath = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.STORE_FILE_PATH);

    public String storeDocuments(String json, String token) {
        ArrayList<String> jsonList = new ArrayList<>();
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();

        for (int i = 1; i <= object.size(); i++) {
            jsonList.add(object.get(String.valueOf(i)).toString());
        }

        FeatureImpl.getFactory().storeDocuments(jsonList, storeFilePath, token);
        return Constant.Status.OK + " - " + token;
    }

    public String evaluateDocument(String json) {

        ArrayList<String> jsonList = new ArrayList<>();
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();

        for (int i = 1; i <= object.size(); i++) {
            jsonList.add(object.get(String.valueOf(i)).toString());
        }

        LinkedHashMap<String, double[]> scores = new Score().getMultipleDocumentScore(jsonList);
        StringBuffer stringBuffer = new StringBuffer();

        for (String m : scores.keySet()) {
            stringBuffer.append("Score for " + m + ":\n");
            for (double score : scores.get(m)) {
                stringBuffer.append(score + "\t");
            }
            stringBuffer.append("\n\n");
        }
        return stringBuffer.toString();
    }

    private FileInputStream getPropertiesFile() {
        try {
            return new FileInputStream(getClass().getClassLoader().getResource(Constant.GlobalAttribute.SYSTEM_PROPERTIES_FILE_NAME).getFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
