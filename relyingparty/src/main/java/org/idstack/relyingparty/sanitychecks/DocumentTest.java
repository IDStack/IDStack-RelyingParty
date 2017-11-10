package org.idstack.relyingparty.sanitychecks;

import org.idstack.feature.document.Document;
import org.idstack.relyingparty.response.confidence.TestResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Sachithra Dangalla
 * @date 11/7/2017
 * @since 1.0
 */
public class DocumentTest {
    private Document document;


    public DocumentTest(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public ArrayList<TestResult> getSanityTestResults() {
        ArrayList<TestResult> testResultResponses = new ArrayList<TestResult>() {
            @Override
            public boolean add(TestResult s) {
                if (s != null) {
                    return super.add(s);
                }
                return false;
            }
        };
        return testResultResponses;
    }

    ;

    public Date getDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date formattedDate = null;
        try {
            formattedDate = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }

    public TestResult checkDateOrder(Date date1, String name1, Date date2, String name2) {
        if (date1 == null || date2 == null) {
            return null;
        }
        String message = "  " + name1 + "     <     " + name2;
        boolean status = false;
        if (date1.before(date2)) {
            status = true;
        }
        return new TestResult(message, status);
    }

    public TestResult checkNicDobValidity(String nic, Date dob) {
        if (dob == null) {
            return null;
        }
        String message = "NIC match with Date of Birth";
        String year = "19" + nic.substring(0, 2);
        String dobYear = new SimpleDateFormat("yyyy").format(dob);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dob);
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        int nicDay = Integer.parseInt(nic.substring(2, 5));
        if (nicDay >= 500) {
            nicDay -= 501;
        }
        boolean status = year.equals(dobYear) && dayOfYear == nicDay;

        TestResult testResult = new TestResult(message, status);
        return testResult;
    }

    public TestResult checkNicGenderValidity(String nic, int gender) {
        String message = "NIC match with Gender";
        int nicValue = Integer.parseInt(nic.substring(2, 5));
        int nicGender = nicValue < 500 ? 0 : 1;
        TestResult testResult = new TestResult(message, gender == nicGender);
        return testResult;
    }

    public TestResult checkNICFormat(String nic) {
        //TODO check multiple formats
        String message = " NIC format";
        int year = Integer.parseInt(nic.substring(0, 2)) + 1900;
        boolean status = false;
        String validity = "Invalid";
        if (Calendar.getInstance().get(Calendar.YEAR) > year) {
            if (nic.matches("\\d{9}[A-Z]")) {
                status = true;
                validity = "Valid";
            }
        }
        TestResult testResult = new TestResult(validity + message, status);
        return testResult;
    }

    public TestResult checkPassportNumberFormat(String pType, String pNumber, String countryCode) {
        if (!countryCode.equals("LKA")) {
            return null;
        }
        String message = " Passport Number format";
        String validity = "Invalid";
        boolean status = false;
        Map<String, String> passportCodes = new HashMap<>();
        passportCodes.put("PA", "N");
        passportCodes.put("PB", "N");
        passportCodes.put("PC", "OL");
        passportCodes.put("PD", "D");
        if (pNumber.startsWith(passportCodes.get(pType))) {
            status = true;
            validity = "Valid";
        }
        TestResult testResult = new TestResult(validity + message, status);
        return testResult;
    }

}
