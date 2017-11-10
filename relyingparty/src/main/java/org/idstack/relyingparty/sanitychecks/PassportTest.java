package org.idstack.relyingparty.sanitychecks;

import org.idstack.feature.document.Document;
import org.idstack.relyingparty.CorrelationScore;
import org.idstack.relyingparty.response.confidence.TestResult;

import java.util.*;

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
        ArrayList<TestResult> testResultResponses = super.getSanityTestResults();

        LinkedHashMap<String, String> content = this.getDocument().getContent();

        //get details
        String passportType = content.get("passport_type");
        String passportNo = content.get("passport_no");
        String countryCode = content.get("country_code");
        String nic = content.get("id_no");
        int gender = CorrelationScore.getGenderClass(content.get("sex"));
        Date dob = getDate(content.get("date_of_birth"));
        String dobName = "Date of Birth";
        Date dateIssued = getDate(content.get("date_of_issue"));
        String dateIssuedName = "Date of Issue";
        Date dateExpiry = getDate(content.get("date_of_expiry"));
        String dateExpiryName = "Date of Expiry";

        //get tests
        testResultResponses.add(this.checkPassportNumberFormat(passportType, passportNo, countryCode));
        testResultResponses.add(this.checkNICFormat(nic));
        testResultResponses.add(this.checkNicDobValidity(nic, dob));
        testResultResponses.add(this.checkNicGenderValidity(nic, gender));
        testResultResponses.add(this.checkDateOrder(dob, dobName, dateIssued, dateIssuedName));
        testResultResponses.add(this.checkDateOrder(dateIssued, dateIssuedName, dateExpiry, dateExpiryName));
        return testResultResponses;
    }


}
