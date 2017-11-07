package org.idstack.relyingparty.sanitychecks;

import org.idstack.feature.document.Document;
import org.idstack.relyingparty.response.confidence.TestResult;

import java.util.ArrayList;

/**
 * @author Sachithra Dangalla
 * @date 11/7/2017
 * @since 1.0
 */
public class PassportTest extends DocumentTest {

    public PassportTest(Document document) {
        super(document);
    }

    @Override
    public ArrayList<TestResult> getSanityTestResults() {
        ArrayList<TestResult> testResultResponses = new ArrayList<>();
        testResultResponses.add(this.matchNicGenderDob());

        return testResultResponses;
    }

    private TestResult matchNicGenderDob() {
        return new TestResult("NIC is matching the DOB and gender", 0);
    }

}
