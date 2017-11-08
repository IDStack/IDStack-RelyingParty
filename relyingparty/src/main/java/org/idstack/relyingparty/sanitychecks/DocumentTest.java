package org.idstack.relyingparty.sanitychecks;

import org.idstack.feature.document.Document;
import org.idstack.relyingparty.response.confidence.TestResult;

import java.util.ArrayList;

/**
 * @author Sachithra Dangalla
 * @date 11/7/2017
 * @since 1.0
 */
public abstract class DocumentTest {
    private Document document;

    public DocumentTest(Document document) {
        this.document = document;
    }

    public abstract ArrayList<TestResult> getSanityTestResults();
}
