package org.idstack.relyingparty.response.correlation;

/**
 * @author Sachithra Dangalla
 * @date 10/5/2017
 * @since 1.0
 */
public class AttributeScore {
    private String text;
    private double score;
    private String colorCode;

    public AttributeScore(String text, double score) {
        this.text = text;
        this.score = score;
        this.colorCode = SuperAttribute.getColorCode(score);
    }

    public double getScore() {
        return score;
    }
}
