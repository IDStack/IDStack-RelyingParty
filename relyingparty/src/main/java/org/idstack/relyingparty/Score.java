package org.idstack.relyingparty;


import com.google.gson.JsonPrimitive;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import org.apache.commons.lang3.tuple.Pair;
import org.idstack.feature.Constant;
import org.idstack.feature.Parser;
import org.idstack.feature.document.Document;

import java.util.*;

/**
 * @author Sachithra Dangalla
 * @date 5/31/2017
 * @since 1.0
 */

@SuppressWarnings("Duplicates")
public class Score {

    /**
     * Calculates a score for a single document based on the document's signed attributes by signers.
     *
     * @param documentJSON, a String representation of a valid JSON object
     * @return calculated score
     */
    public double getSingleDocumentScore(String documentJSON) {

        Document doc = Parser.parseDocumentJson(documentJSON);

        int signedAttributesCount = 0;
        int allAttributesCount = 0;

        for (String k : doc.getContent().keySet()) {
            allAttributesCount += 1;
        }

        return (double) signedAttributesCount * 100 / allAttributesCount;
    }

    /**
     * @param documentJSONs
     * @return a map of String keys and double[] values (array of name scores of documents)
     * keys = "name", "address", "dob", "nic", "gender"
     */
    public LinkedHashMap<String, double[]> getMultipleDocumentScore(ArrayList<String> documentJSONs) {
        int docsLength = documentJSONs.size();
        Document[] docs = new Document[docsLength];
        for (int i = 0; i < docsLength; i++) {
            Document doc = Parser.parseDocumentJson(documentJSONs.get(i));
            docs[i] = doc;
        }
        LinkedHashMap<String, double[]> attributeScores = new LinkedHashMap<>();
        attributeScores.put("name", getNameCorrelationScore(docs));
        attributeScores.put("address", getAddressCorrelationScore(docs));
        attributeScores.put("dob", getDOBCorrelationScore(docs));
        attributeScores.put("nic", getNICCorrelationScore(docs));
        attributeScores.put("gender", getGenderCorrelationScore(docs));

        return attributeScores;
    }

    private String getConcatenatedValue(Document doc, Pair<Double, String[]> attribute) {
        String name = "";
        StringJoiner sb = new StringJoiner(" ");
        LinkedHashMap<String, Object> content = doc.getContent();
        for (String attributeName : attribute.getRight()) {
            if (content.get(attributeName) != null) {
                Object value = content.get(attributeName);
                if (value instanceof JsonPrimitive) {
                    sb.add(((JsonPrimitive) value).getAsString());
                } else {
                    //assume only one nested level
                    LinkedHashMap<String, Object> names = (LinkedHashMap<String, Object>) value;
                    for (String key : names.keySet()) {
                        sb.add(((JsonPrimitive) names.get(key)).getAsString());
                    }
                }
            }
        }

        name = sb.toString();
        return name;
    }

    private double[] getNameCorrelationScore(Document[] docs) {
        double[] scores = new double[docs.length];
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < docs.length; i++) {
            Document doc = docs[i];
            String name = getConcatenatedValue(doc, Constant.Attribute.NAME);
            if (name.isEmpty()) {
                scores[i] = -100;
            } else {
                scores[i] = 0;
                names.add(name);
            }
        }

        NameScore ns = new NameScore(names);
        ArrayList<Double> nameScores = ns.getNameScore();

        int nsPointer = 0;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] == 0) {
                scores[i] = nameScores.get(nsPointer);
                nsPointer++;
            }
        }
        return scores;
    }

    private double[] getNameCorrelationScore_old(Document[] docs) {
        int docsLength = docs.length;
        double[] scores = new double[docsLength];
        String[] names = new String[docsLength];
        //count candidates with occurrences count
        Map<String, Integer> candidates = new HashMap<>();
        for (int i = 0; i < docs.length; i++) {
            Document doc = docs[i];
            String name = getConcatenatedValue(doc, Constant.Attribute.NAME);
            names[i] = name;
            //if name is not present set -100
            scores[i] = name.isEmpty() ? -100 : 0;
        }

        NormalizedLevenshtein similarity = new NormalizedLevenshtein(); // TODO use weighted lavenshtein
        for (int i = 0; i < docsLength; i++) {
            if (scores[i] == 0) {
                String name = names[i];
                int myScore = 0;
                int neighborCount = 0;
                for (int j = 0; j < docsLength; j++) {
                    if (j != i) {
                        neighborCount += 1;
                        myScore += (1 - similarity.distance(name, names[j]));
                    }
                }
                scores[i] = myScore / neighborCount;
            }
        }
        return scores;
    }

    private double[] getAddressCorrelationScore(Document[] docs) {
        int docsLength = docs.length;
        double[] scores = new double[docsLength];
        String[] names = new String[docsLength];
        //count candidates with occurrences count
        Map<String, Integer> candidates = new HashMap<>();
        for (int i = 0; i < docs.length; i++) {
            Document doc = docs[i];
            String name = getConcatenatedValue(doc, Constant.Attribute.ADDRESS);
            names[i] = name;
            //if name is not present set -100
            scores[i] = name.isEmpty() ? -100 : 0;
        }

        NormalizedLevenshtein similarity = new NormalizedLevenshtein(); // TODO use weighted lavenshtein
        for (int i = 0; i < docsLength; i++) {
            if (scores[i] == 0) {
                String name = names[i];
                int myScore = 0;
                int neighborCount = 0;
                for (int j = 0; j < docsLength; j++) {
                    if (j != i) {
                        neighborCount += 1;
                        myScore += (1 - similarity.distance(name, names[j]));
                    }
                }
                scores[i] = myScore / neighborCount;
            }
        }
        return scores;
    }

    //TODO find date comparison
    private double[] getDOBCorrelationScore(Document[] docs) {
        int docsLength = docs.length;
        double[] scores = new double[docsLength];
        String[] names = new String[docsLength];
        //count candidates with occurrences count
        Map<String, Integer> candidates = new HashMap<>();
        for (int i = 0; i < docs.length; i++) {
            Document doc = docs[i];
            String name = getConcatenatedValue(doc, Constant.Attribute.DOB);
            names[i] = name;
            //if name is not present set -100
            scores[i] = name.isEmpty() ? -100 : 0;
        }

        NormalizedLevenshtein similarity = new NormalizedLevenshtein(); // TODO use weighted lavenshtein
        for (int i = 0; i < docsLength; i++) {
            if (scores[i] == 0) {
                String name = names[i];
                int myScore = 0;
                int neighborCount = 0;
                for (int j = 0; j < docsLength; j++) {
                    if (j != i) {
                        neighborCount += 1;
                        myScore += (1 - similarity.distance(name, names[j]));
                    }
                }
                scores[i] = myScore / neighborCount;
            }
        }
        return scores;
    }

    private double[] getNICCorrelationScore(Document[] docs) {
        int docsLength = docs.length;
        double[] scores = new double[docsLength];
        String[] nics = new String[docsLength];
        //count candidates with occurrences count
        Map<String, Integer> candidates = new HashMap<>();
        for (int i = 0; i < docs.length; i++) {
            Document doc = docs[i];
            String nic = "";
            //iterate over "nic" attributes
            for (String attributeName : Constant.Attribute.NIC.getRight()) {
                if (doc.getContent().get(attributeName) != null) {
                    nic = ((JsonPrimitive) doc.getContent().get(attributeName)).getAsString().toLowerCase();
                    Integer count = candidates.get(nic);
                    candidates.put(nic, count != null ? count + 1 : 0);
                    break;
                }
            }

            nics[i] = nic;
            //if nic is not present set -100
            scores[i] = nic.isEmpty() ? -100 : 0;
        }

        //select most popular nic
        if (!candidates.isEmpty()) {
            String popular = Collections.max(candidates.entrySet(),
                    new Comparator<Map.Entry<String, Integer>>() {
                        @Override
                        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                            return o1.getValue().compareTo(o2.getValue());
                        }
                    }).getKey();

            //set scores
            for (int i = 0; i < docs.length; i++) {
                scores[i] = (nics[i].equals(popular) && scores[i] == 0) ? 1 : 0;
            }
        }
        return scores;
    }

    private double[] getGenderCorrelationScore(Document[] docs) {
        String[] male = {"male", "m"};
        String[] female = {"female", "f"};
        int docsLength = docs.length;
        double[] scores = new double[docsLength];
        String[] genders = new String[docsLength];
        //count candidates with occurrences count
        Map<String, Integer> candidates = new HashMap<>();
        for (int i = 0; i < docs.length; i++) {
            Document doc = docs[i];
            String gender = "";
            //iterate over "gender" attributes
            for (String attributeName : Constant.Attribute.SEX.getRight()) {
                if (doc.getContent().get(attributeName) != null) {
                    gender = ((JsonPrimitive) doc.getContent().get(attributeName)).getAsString().toLowerCase();
                    for (String s : male) {
                        if (gender.equals(s)) {
                            gender = "male";
                            break;
                        }
                    }
                    for (String s : female) {
                        if (gender.equals(s)) {
                            gender = "female";
                            break;
                        }
                    }
                    Integer count = candidates.get(gender);
                    candidates.put(gender, count != null ? count + 1 : 0);
                    break;
                }
            }

            genders[i] = gender;
            //if gender is not present set -100
            scores[i] = gender.isEmpty() ? -100 : 0;
        }

        if (!candidates.isEmpty()) {
            //select most popular gender
            String popular = Collections.max(candidates.entrySet(),
                    new Comparator<Map.Entry<String, Integer>>() {
                        @Override
                        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                            return o1.getValue().compareTo(o2.getValue());
                        }
                    }).getKey();

            //set scores
            for (int i = 0; i < docs.length; i++) {
                scores[i] = (genders[i].equals(popular) && scores[i] == 0) ? 1 : 0;
            }
        }

        return scores;
    }

}
