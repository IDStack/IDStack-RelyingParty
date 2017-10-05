package org.idstack.relyingparty;


import com.google.gson.JsonPrimitive;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import org.apache.commons.lang3.tuple.Pair;
import org.idstack.feature.Constant;
import org.idstack.feature.Parser;
import org.idstack.feature.document.Document;

import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Sachithra Dangalla
 * @date 5/31/2017
 * @since 1.0
 */

@SuppressWarnings("Duplicates")
public class CorrelationScore {


    /**
     * @param documentJSONs
     * @return a map of String keys and double[] values (array of name scores of documents)
     */
    public LinkedHashMap<String, double[]> getMultipleDocumentScore(ArrayList<String> documentJSONs) {
        int docsLength = documentJSONs.size();
        Document[] docs = new Document[docsLength];
        for (int i = 0; i < docsLength; i++) {
            Document doc = Parser.parseDocumentJson(documentJSONs.get(i));
            docs[i] = doc;
        }
        LinkedHashMap<String, double[]> attributeScores = new LinkedHashMap<>();
        attributeScores.put(Constant.Attribute.NAME.getLeft(), getNameCorrelationScore(docs));
        attributeScores.put(Constant.Attribute.ADDRESS.getLeft(), getAddressCorrelationScore(docs));
        attributeScores.put(Constant.Attribute.DOB.getLeft(), getDOBCorrelationScore(docs));
        attributeScores.put(Constant.Attribute.NIC.getLeft(), getNICCorrelationScore(docs));
        attributeScores.put(Constant.Attribute.SEX.getLeft(), getGenderCorrelationScore(docs));

        return attributeScores;
    }

    private String getConcatenatedValue(Document doc, Pair<String, String[]> attribute) {
        String name;
        StringJoiner sb = new StringJoiner(" ");
        LinkedHashMap<String, Object> content = doc.getContent();
        Map<String, String> flatAttributeMap = new HashMap<>();
        for (String attributeName : attribute.getRight()) {
            if (content.get(attributeName) != null) {
                Object value = content.get(attributeName);
                if (value instanceof JsonPrimitive) {
                    flatAttributeMap.put(attributeName, ((JsonPrimitive) value).getAsString());
                } else {
                    //assume only one nested level
                    LinkedHashMap<String, Object> names = (LinkedHashMap<String, Object>) value;
                    for (String key : names.keySet()) {
                        flatAttributeMap.put(attributeName, (((JsonPrimitive) names.get(key)).getAsString()));
                    }
                }
            }
        }
        for (String attributeName : attribute.getRight()) {
            String nameSeg = flatAttributeMap.get(attributeName);
            if (nameSeg != null) {
                sb.add(nameSeg);
                System.out.println(nameSeg);
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
            scores[i] = 0;
            if (!name.isEmpty()) {
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
            scores[i] = 0;
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
                double score = myScore * 100 / neighborCount;
                //round off to 2 decimal places
                DecimalFormat df = new DecimalFormat("#.##");
                score = Double.valueOf(df.format(score));
                scores[i] = score;
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
            scores[i] = 0;
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
                scores[i] = myScore * 100 / neighborCount;
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
            scores[i] = 0;
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
                scores[i] = (nics[i].equals(popular) && scores[i] == 0) ? 100 : 0;
            }
        }
        return scores;
    }

    private double[] getGenderCorrelationScore(Document[] docs) {

        int docsLength = docs.length;
        double[] scores = new double[docsLength];
        int[] genders = new int[docsLength];
        //count candidates with occurrences count
        Map<Integer, Integer> candidates = new HashMap<>();
        for (int i = 0; i < docs.length; i++) {
            Document doc = docs[i];
            String gender = "";
            //iterate over "gender" attributes
            for (String attributeName : Constant.Attribute.SEX.getRight()) {
                if (doc.getContent().get(attributeName) != null) {
                    gender = ((JsonPrimitive) doc.getContent().get(attributeName)).getAsString().toLowerCase();
                    //determine the target class of the value
                    int targetClass = 0;
                    for (int j = 0; j < Constant.Attribute.Gender.TARGET_CLASSES.length; j++) {
                        String[] targetClassValues = Constant.Attribute.Gender.TARGET_CLASSES[j];
                        for (String s : targetClassValues) {
                            if (gender.equals(s)) {
                                genders[i] = j;
                                targetClass = j;
                                break;
                            }
                        }
                    }

                    Integer count = candidates.get(targetClass);
                    candidates.put(targetClass, count != null ? count + 1 : 0);
                    break;
                } else {
                    //there is no gender value for doc[i]
                    genders[i] = -1;
                }
            }

//            genders[i] = gender;
            scores[i] = 0;
        }

        if (!candidates.isEmpty()) {
            //select most popular gender
            int popular = Collections.max(candidates.entrySet(),
                    new Comparator<Map.Entry<Integer, Integer>>() {
                        @Override
                        public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                            return o1.getValue().compareTo(o2.getValue());
                        }
                    }).getKey();

            //set scores
            for (int i = 0; i < docs.length; i++) {
                scores[i] = (genders[i] == popular && scores[i] == 0) ? 100 : 0;
            }
        }

        return scores;
    }

}
