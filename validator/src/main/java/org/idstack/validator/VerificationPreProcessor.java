package org.idstack.validator;

import org.idstack.feature.document.Document;
import org.idstack.feature.document.Validator;

import java.util.ArrayList;

/**
 * @author Chandu Herath
 * @date 04/06/2017
 * @since 1.0
 */

public class VerificationPreProcessor {

    @SuppressWarnings("Duplicates")
    public Document getSignedDigitizedVersion(Document wholeVersion, int signatureID) {
        //remove content if content is not signed
        if (!wholeVersion.getValidators().get(signatureID).getSignedAttributes()) {
            wholeVersion.getContent().clear();
        }

        //removing signatures which are not signed

        //get the signed signature list for this signature
        ArrayList<String> signedSignatureList = wholeVersion.getValidators().get(signatureID).getSignedSignatures();

        if (signedSignatureList.size() > 0) {
            //Collect all the signer Ids in the complete json
            ArrayList<String> signerIDList = new ArrayList<>();
            for (int i = 0; i < wholeVersion.getValidators().size(); i++) {
                signerIDList.add(wholeVersion.getValidators().get(i).getId());
            }

            //Create a new signer array list to assign it later to the final version
            ArrayList<Validator> signerList = new ArrayList();

            for (int i = 0; i < signerIDList.size(); i++) {
                String temp = signerIDList.get(i);
                for (int j = 0; j < signedSignatureList.size(); j++) {
                    if (signedSignatureList.get(j).equals(temp)) {
                        signerList.add(wholeVersion.getValidators().get(i));
                    }
                }
            }
            wholeVersion.setValidators(signerList);
        } else {
            wholeVersion.getValidators().clear();
        }

        return wholeVersion;
    }
}
