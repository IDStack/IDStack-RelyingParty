package org.idstack.relyingparty.response.confidence;

import org.idstack.relyingparty.CorrelationScore;
import org.idstack.relyingparty.response.correlation.CorrelationScoreResponse;
import org.idstack.relyingparty.response.correlation.SuperAttribute;

/**
 * @author Sachithra Dangalla
 * @date 11/7/2017
 * @since 1.0
 */
public class TestResult {
    private String message;
    private boolean status;

    public TestResult(String message, boolean status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
