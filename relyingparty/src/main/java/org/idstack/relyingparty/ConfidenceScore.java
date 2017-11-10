package org.idstack.relyingparty;

import org.idstack.feature.Constant;
import org.idstack.feature.document.Document;
import org.idstack.feature.document.Validator;
import org.idstack.relyingparty.response.confidence.ConfidenceScoreResponse;
import org.idstack.relyingparty.response.confidence.TestResult;
import org.idstack.relyingparty.sanitychecks.PassportTest;
import org.idstack.relyingparty.sanitychecks.TranscriptTest;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * @author Sachithra Dangalla
 * @date 9/11/2017
 * @since 1.0
 */
public class ConfidenceScore {
    /**
     * Calculates a score for a single document based on the document's signed attributes by signers.
     *
     * @param doc parsed document object
     * @return calculated score
     */
    public ConfidenceScoreResponse getSingleDocumentScore(Document doc) {

        //100% if issuer = extractor
        String issuerURL = doc.getMetaData().getIssuer().getUrl();
        String extractorURL = doc.getExtractor().getSignature().getUrl();

        ArrayList<TestResult> testResults = null;
        String docType = doc.getMetaData().getDocumentType();
        if (docType.equals(Constant.DocumentType.PASSPORT)) {
            PassportTest pt = new PassportTest(doc);
            testResults = pt.getSanityTestResults();
        } else if (docType.equals(Constant.DocumentType.TRANSCRIPT)) {
            TranscriptTest tt = new TranscriptTest(doc);
            testResults = tt.getSanityTestResults();
        }
        if (issuerURL.equals(extractorURL)) {
            ConfidenceScoreResponse confidenceScoreResponse = new ConfidenceScoreResponse(100, testResults);
            return confidenceScoreResponse;
        }

        //else
        int allSigns = 0;           //all validators
        int contentSigns = 0;       //validators who signed the content
        int signatureSigns = 0;     //validators whose ancestor signed the content

        ArrayList<Validator> validators = doc.getValidators();

        allSigns = validators.size();

        for (int i = 0; i < validators.size(); i++) {
            Validator validator = validators.get(i);

            if (validator.getSignedAttributes()) {
                contentSigns++;
            }
            signatureSigns += validator.getSignedSignatures().size();
        }

        //algo
        //TODO set different weights for content and signatures
        double score = (allSigns <= 1) ? 100 : (double) ((2 * signatureSigns / (allSigns - 1)) + contentSigns) * 100 / (2 * allSigns);

        //round off to 2 decimal places
        DecimalFormat df = new DecimalFormat("#.##");
        score = Double.valueOf(df.format(score));

        ConfidenceScoreResponse confidenceScoreResponse = new ConfidenceScoreResponse(score, testResults);
        return confidenceScoreResponse;
    }
}
