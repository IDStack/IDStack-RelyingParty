package org.idstack.relyingparty;

import org.idstack.feature.Parser;
import org.idstack.feature.document.Document;
import org.idstack.feature.document.Validator;

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
     * @param documentJSON, a String representation of a valid JSON object
     * @return calculated score
     */
    public double getSingleDocumentScore(String documentJSON) {

        Document doc = Parser.parseDocumentJson(documentJSON);

        //100% if issuer = extractor
        String issuerURL = doc.getMetaData().getIssuer().getUrl();
        String extractorURL = doc.getExtractor().getSignature().getUrl();
        if (issuerURL.equals(extractorURL)) {
            return 100;
        }

        //else
        int allSigns = 0;               //all validators
        int contentSigns = 0;     //validators who signed the content
        int signatureSigns = 0;   //validators whose ancestor signed the content

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
        double score = (double) ((2 * signatureSigns / (allSigns - 1)) + contentSigns) / (2 * allSigns);

        return score;
    }
}
