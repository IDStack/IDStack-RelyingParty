package org.idstack.relyingparty.response;

/**
 * @author Sachithra Dangalla
 * @date 10/5/2017
 * @since 1.0
 */
public class AttributeScore {
    private String text;
    private double score;

    public AttributeScore(String text, double score) {
        this.text = text;
        this.score = score;
    }

    public double getScore() {
        return score;
    }
}
