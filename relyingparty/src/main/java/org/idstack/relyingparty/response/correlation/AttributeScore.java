package org.idstack.relyingparty.response.correlation;

/**
 * @author Sachithra Dangalla
 * @date 10/5/2017
 * @since 1.0
 */
public class AttributeScore {
    private String text;
    private String score;
    private String status;

    public AttributeScore(String text, double score) {
        this.text = text;
        this.score = SuperAttribute.formatScore(score);
        this.status = SuperAttribute.getStatus(score);
    }

    public double getScore() {
        return Double.parseDouble(this.score);
    }
}
