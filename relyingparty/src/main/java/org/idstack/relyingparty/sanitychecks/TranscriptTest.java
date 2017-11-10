package org.idstack.relyingparty.sanitychecks;

import org.idstack.feature.document.Document;
import org.idstack.relyingparty.response.confidence.TestResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author Sachithra Dangalla
 * @date 11/10/2017
 * @since 1.0
 */
public class TranscriptTest extends DocumentTest {
    public TranscriptTest(Document document) {
        super(document);
    }

    @Override
    public ArrayList<TestResult> getSanityTestResults() {
        ArrayList<TestResult> testResultResponses = super.getSanityTestResults();

        LinkedHashMap<String, String> content = this.getDocument().getContent();

        //get details
        Date dob = getDate(content.get("date_of_birth"));
        String dobName = "Date of Birth";
        Date dateIssued = getDate(content.get("date_of_issue"));
        String dateIssuedName = "Date of Issue";
        Date dateGraduation = getDate(content.get("date_of_graduation"));
        String dateGraduationName = "Date of Graduation";

        //get tests
        testResultResponses.add(this.checkDateOrder(dob, dobName, dateIssued, dateIssuedName));
        testResultResponses.add(this.checkDateOrder(dob, dobName, dateGraduation, dateGraduationName));
        return testResultResponses;
    }


}
