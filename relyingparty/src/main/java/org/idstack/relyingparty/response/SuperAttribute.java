package org.idstack.relyingparty.response;

import java.util.ArrayList;

/**
 * @author Sachithra Dangalla
 * @date 10/5/2017
 * @since 1.0
 */
public class SuperAttribute {
    private double avgScore;
    private String avgColorCode;
    ArrayList<AttributeScore> values;

    private static final String colorRed = "#F00501";
    private static final String colorYellow = "#EFD101";
    private static final String colorGreen = "#66BB2A";

    public SuperAttribute(ArrayList<AttributeScore> values) {
        this.values = values;
        double tot = 0;
        int docCount = 0;
        for (AttributeScore as : values) {
            tot += as.getScore();
            docCount++;
        }
        double avg = tot / docCount;
        this.avgScore = avg;
        this.avgColorCode = getColorCode(avg);
    }

    public static String getColorCode(double avg) {
        String stat = colorYellow;
        if (avg < 25) {
            //TODO add constant
            stat = colorRed;
        } else if (avg > 90) {
            stat = colorGreen;
        }
        return stat;
    }
}
