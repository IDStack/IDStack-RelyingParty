package org.idstack.relyingparty;

/**
 * @author Sachithra Dangalla
 * @date 8/30/2017
 * @since 1.0
 */

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;

import static org.idstack.relyingparty.Soundex.soundex;

public class NameScore {
    private ArrayList<String> names;
    private ArrayList<String> soundexesAll;
    private Map<Integer, ArrayList<String>> singularSoundex;
    private ListMultimap<DoubleKey, String> soundexMultimap;

    public NameScore(ArrayList<String> names) {
        this.names = names;
        this.soundexesAll = new ArrayList<>(); // all soundexes present in all documents
        this.singularSoundex = new HashMap<>(); //map of documents and their soundexes
        this.soundexMultimap = ArrayListMultimap.create(); //map of soundex, document id (as double key pair) and name value
        getSoundexMap(names);
    }

    private ListMultimap<DoubleKey, String> getSoundexMap(ArrayList<String> names) {
        for (int i = 0; i < names.size(); i++) {
            ArrayList<String> soundexes1 = new ArrayList<>();
            for (String s : names.get(i).split(" ")) {
                s = s.toLowerCase();
                String soundexCode = soundex(s);
                if (!soundexCode.isEmpty()) {
                    soundexes1.add(soundexCode);
                    if (!this.soundexesAll.contains(soundexCode)) {
                        this.soundexesAll.add(soundexCode);
                    }
                    DoubleKey doubleKey = new DoubleKey(soundexCode, i);
                    this.soundexMultimap.put(doubleKey, s);
                }
            }

            this.singularSoundex.put(i, soundexes1);
        }
        return this.soundexMultimap;
    }

    /**
     * @param index1, documentID1
     * @param index2, documentID2
     * @return
     */
    private double getTwoNameOrderScore(int index1, int index2) {
        ArrayList<String> first = this.singularSoundex.get(index1);
        ArrayList<String> second = this.singularSoundex.get(index2);
        if (this.singularSoundex.get(index1).size() > this.singularSoundex.get(index2).size()) {
            first = this.singularSoundex.get(index2);
            second = this.singularSoundex.get(index1);
        }

        //get order score
        double orderScore = 0;
        int count = second.size();
        int pointer2 = 0;

        for (int p1 = 0; p1 < first.size(); p1++) {
            String name1 = first.get(p1);
            if (!second.contains(name1)) {
                count++;
            }
            for (int p2 = pointer2; p2 < second.size(); p2++) {
                if (name1.equals(second.get(p2))) {
                    orderScore++;
                    pointer2 = p2 + 1;
                    break;
                }
            }
        }
        return orderScore / count;
    }

    /**
     * @param index1, documentID1
     * @param index2, documentID2
     * @return
     */
    private double getTwoNameStringScore(int index1, int index2) {

        //string similarity score
        double stringScore = 0;
        NormalizedLevenshtein similarity = new NormalizedLevenshtein();
        for (String key : this.soundexesAll) {

            List<String> values1 = this.soundexMultimap.get(new DoubleKey(key, index1));
            List<String> values2 = this.soundexMultimap.get(new DoubleKey(key, index2));
            if (values1.size() == 1 && values2.size() == 1) {
                stringScore += (1 - similarity.distance(values1.get(0), values2.get(0)));
            } else {
                //TODO consider multiple names in one doc per one soundex
            }
        }
        //average the similarities of each soundex
        stringScore = stringScore / this.soundexesAll.size();
        return stringScore;
    }

    /**
     * @return the score of each document
     */
    public ArrayList<Double> getNameScore() {
        //TODO detect and remove/process initials

        int nameCount = this.names.size();
        double[][] pairScores = new double[nameCount][nameCount];
        for (int i = 0; i < nameCount; i++) {
            for (int j = 0; j < nameCount; j++) {
                if (i == j || pairScores[i][j] != 0) {
                    continue;
                }
                double orderScore = this.getTwoNameOrderScore(i, j);
                double stringScore = this.getTwoNameStringScore(i, j);

                //TODO find the weight for order and string similarity

                double score = (orderScore + stringScore) / 2;

                pairScores[i][j] = score;
                pairScores[j][i] = score;
            }
        }
        ArrayList<Double> docScores = new ArrayList<>(nameCount);
        for (int i = 0; i < nameCount; i++) {
            double rowSum = DoubleStream.of(pairScores[i]).sum();
            docScores.add(rowSum / (nameCount - 1)); //-1 for (i,i)
        }

        return docScores;
    }

    public class DoubleKey {
        String key1;
        int key2;

        public DoubleKey(String key1, int key2) {
            this.key1 = key1;
            this.key2 = key2;
        }

        @Override
        public int hashCode() {
            return this.key2;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof DoubleKey)) {
                return false;
            }

            DoubleKey other = (DoubleKey) o;
            if (this.key2 != other.key2) return false;
            if (!this.key1.equals(other.key1)) return false;

            return true;
        }

        public String getKey1() {
            return key1;
        }

        public void setKey1(String key1) {
            this.key1 = key1;
        }

        public int getKey2() {
            return key2;
        }

        public void setKey2(int key2) {
            this.key2 = key2;
        }
    }
}

