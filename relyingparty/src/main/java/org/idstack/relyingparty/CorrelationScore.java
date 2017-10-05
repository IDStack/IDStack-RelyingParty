package org.idstack.relyingparty;


import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.tuple.Pair;
import org.idstack.feature.Constant;
import org.idstack.feature.Parser;
import org.idstack.feature.document.Document;
import org.idstack.relyingparty.response.AttributeScore;
import org.idstack.relyingparty.response.CorrelationScoreResponse;
import org.idstack.relyingparty.response.SuperAttribute;

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
    public CorrelationScoreResponse getMultipleDocumentScore(ArrayList<String> documentJSONs) {
        int docsLength = documentJSONs.size();
        Document[] docs = new Document[docsLength];
        for (int i = 0; i < docsLength; i++) {
            Document doc = Parser.parseDocumentJson(documentJSONs.get(i));
            docs[i] = doc;
        }
        SuperAttribute name = new SuperAttribute(getNameCorrelationScore(docs));
        SuperAttribute address = new SuperAttribute(getAddressCorrelationScore(docs));
        SuperAttribute dob = new SuperAttribute(getDOBCorrelationScore(docs));
        SuperAttribute gender = new SuperAttribute(getGenderCorrelationScore(docs));
        SuperAttribute nic = new SuperAttribute(getNICCorrelationScore(docs));

        CorrelationScoreResponse cs = new CorrelationScoreResponse(name, address, dob, gender, nic);

        return cs;
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
                        flatAttributeMap.put(key, (((JsonPrimitive) names.get(key)).getAsString()));
                    }
                }
            }
        }
        for (String attributeName : attribute.getRight()) {
            String nameSeg = flatAttributeMap.get(attributeName);
            if (nameSeg != null) {
                sb.add(nameSeg);
            }
        }

        name = sb.toString();
        return name;
    }

    private ArrayList<AttributeScore> getNameCorrelationScore(Document[] docs) {

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < docs.length; i++) {
            Document doc = docs[i];
            String name = getConcatenatedValue(doc, Constant.Attribute.NAME);
            if (!name.isEmpty()) {
                names.add(name);
            }
        }

        NameScore ns = new NameScore(names);
        ArrayList<AttributeScore> nameScores = ns.getNameScore();

        return nameScores;
    }


    private ArrayList<AttributeScore> getAddressCorrelationScore(Document[] docs) {
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < docs.length; i++) {
            Document doc = docs[i];
            String name = getConcatenatedValue(doc, Constant.Attribute.ADDRESS);
            if (!name.isEmpty()) {
                names.add(name);
            } else {
                names.add("");
            }
        }

        NameScore ns = new NameScore(names);
        ArrayList<AttributeScore> nameScores = ns.getNameScore();

        return nameScores;
    }

    //TODO find date comparison
    private ArrayList<AttributeScore> getDOBCorrelationScore(Document[] docs) {
        int docsLength = docs.length;
        ArrayList<AttributeScore> attrScore = new ArrayList<>();
        String[] nics = new String[docsLength];
        //count candidates with occurrences count
        Map<String, Integer> candidates = new HashMap<>();
        for (int i = 0; i < docs.length; i++) {
            Document doc = docs[i];
            String nic = "";
            //iterate over "dob" attributes
            for (String attributeName : Constant.Attribute.DOB.getRight()) {
                if (doc.getContent().get(attributeName) != null) {
                    nic = ((JsonPrimitive) doc.getContent().get(attributeName)).getAsString().toLowerCase();
                    Integer count = candidates.get(nic);
                    candidates.put(nic, count != null ? count + 1 : 1);
                    break;
                }
            }
            if (nic.isEmpty()) {
                nic = "";
                Integer count = candidates.get(nic);
                candidates.put("", count != null ? count + 1 : 1);
            }

            nics[i] = nic;
        }

        //select most popular nic
        String popular = "";
        if (!candidates.isEmpty()) {
            popular = getPopularString(candidates);

        }
        //set scores
        for (int i = 0; i < docs.length; i++) {
            double score = (!popular.isEmpty() && nics[i].equals(popular)) ? 100 : 0;
            AttributeScore as = new AttributeScore(nics[i], score);

            attrScore.add(as);
        }
        return attrScore;
    }

    private ArrayList<AttributeScore> getNICCorrelationScore(Document[] docs) {
        int docsLength = docs.length;
        ArrayList<AttributeScore> attrScore = new ArrayList<>();
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
                    candidates.put(nic, count != null ? count + 1 : 1);

                    break;
                }
            }
            if (nic.isEmpty()) {
                nic = "";
                Integer count = candidates.get(nic);
                candidates.put("", count != null ? count + 1 : 1);
            }

            nics[i] = nic;
        }

        //select most popular nic
        String popular = "";
        if (!candidates.isEmpty()) {
            popular = getPopularString(candidates);

        }
        //set scores
        for (int i = 0; i < docs.length; i++) {
            double score = (!popular.isEmpty() && nics[i].equals(popular)) ? 100 : 0;
            AttributeScore as = new AttributeScore(nics[i], score);
            attrScore.add(as);
        }

        return attrScore;
    }

    private ArrayList<AttributeScore> getGenderCorrelationScore(Document[] docs) {

        int docsLength = docs.length;
        int[] genders = new int[docsLength];
        String[] genderNames = new String[docsLength];
        ArrayList<AttributeScore> attrScore = new ArrayList<>();
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
                    candidates.put(targetClass + 1, count != null ? count + 1 : 1);
                    break;
                } else {
                    //there is no gender value for doc[i]
                    genders[i] = -1;
                    Integer count = candidates.get(0);
                    candidates.put(0, count != null ? count + 1 : 1);
                }
            }
            genderNames[i] = gender;
        }

        if (!candidates.isEmpty()) {
            //select most popular gender
            int popular = getPopularInt(candidates);

            //set scores
            for (int i = 0; i < docs.length; i++) {
                double score = (popular != 0 && genders[i] == popular) ? 100 : 0;
                AttributeScore as = new AttributeScore(genderNames[i], score);
                attrScore.add(as);
            }
        }

        return attrScore;
    }

    private String getPopularString(Map<String, Integer> candidates) {
        if (candidates.get("") != null) {
            if (candidates.get("") >= candidates.size() / 2) {
                return "";
            }
        }

        String popular = Collections.max(candidates.entrySet(),
                new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                }).getKey();

        return popular;
    }

    private int getPopularInt(Map<Integer, Integer> candidates) {
        if (candidates.get(0) != null) {
            if (candidates.get(0) >= candidates.size() / 2) {
                //if half or more entries are empty, the popular is the empty
                return 0;
            }
        }
        int popular = Collections.max(candidates.entrySet(),
                new Comparator<Map.Entry<Integer, Integer>>() {
                    @Override
                    public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                }).getKey();
        return popular;
    }

}
