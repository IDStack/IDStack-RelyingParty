package org.idstack.relyingparty.response.correlation;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * @author Sachithra Dangalla
 * @date 10/5/2017
 * @since 1.0
 */
public class SuperAttribute {
    private String avgScore;
    private String status;
    private ArrayList<AttributeScore> values;

    public SuperAttribute(ArrayList<AttributeScore> values) {
        this.values = values;
        double tot = 0;
        int docCount = 0;
        for (AttributeScore as : values) {
            tot += as.getScore();
            docCount++;
        }
        double avg = tot / docCount;
        this.avgScore = formatScore(avg);
        this.status = getStatus(avg);
    }

    public static String getStatus(double avg) {
        String colorRed = "danger";
        String colorYellow = "warning";
        String colorGreen = "success";
        String stat = colorYellow;
        if (avg < 25) {
            //TODO add constant
            stat = colorRed;
        } else if (avg > 90) {
            stat = colorGreen;
        }
        return stat;
    }

    public static String formatScore(double score) {
        //round off to 2 decimal places
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(score);
    }
}
