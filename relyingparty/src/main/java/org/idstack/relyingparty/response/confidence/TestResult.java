package org.idstack.relyingparty.response.confidence;

/**
 * @author Sachithra Dangalla
 * @date 11/7/2017
 * @since 1.0
 */
public class TestResult {
    private String message;
    private int status;

    public TestResult(String message, int status) {
        this.message = message;
        this.status = status;
    }

}
