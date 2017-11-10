package org.idstack.relyingparty.response.confidence;

import org.idstack.relyingparty.response.correlation.SuperAttribute;

import java.util.ArrayList;

/**
 * @author Sachithra Dangalla
 * @date 11/7/2017
 * @since 1.0
 */
public class ConfidenceScoreResponse {
    private String score;
    ArrayList<TestResult> tests;

    public ConfidenceScoreResponse(double score, ArrayList<TestResult> tests) {
        this.score = SuperAttribute.formatScore(score);
        this.tests = tests;
    }
}
