package org.idstack.relyingparty.response;

import java.util.ArrayList;

/**
 * @author Sachithra Dangalla
 * @date 10/5/2017
 * @since 1.0
 */
public class SuperAttribute {
    private double avgScore;
    private int status;
    ArrayList<AttributeScore> values;

    public SuperAttribute(ArrayList<AttributeScore> values) {
        this.values = values;
        double tot = 0;
        int docCount = 0;
        for (AttributeScore as : values) {
            tot += as.getScore();
            docCount++;
        }
        double avg = tot / docCount;
        int stat = 0;
        if (avg == 0) {
            //TODO add constant
            stat = -1;
        } else if (avg == 100) {
            stat = 1;
        }
        this.avgScore = avg;
        this.status = stat;
    }
}
