package org.idstack.validator;

import org.idstack.feature.document.Document;
import org.idstack.feature.document.Validator;

import java.util.ArrayList;

/**
 * @author Chandu Herath
 * @date 30/05/2017
 * @since 1.0
 */

public class SignPreProcessor {

    @SuppressWarnings("Duplicates")
    public Document getSignOnlyDigitizedVersion(Document wholeVersion, boolean signContent, ArrayList<String> signList) throws CloneNotSupportedException {
        //removing content if it is not supposed to be signed
        if (!signContent) {
            wholeVersion.getContent().clear();
        }

        //removing signatures witch are not supposed to be signed
        if (signList != null) {
            ArrayList<String> signerIDList = new ArrayList<>();
            for (int i = 0; i < wholeVersion.getValidators().size(); i++) {
                signerIDList.add(wholeVersion.getValidators().get(i).getId());
            }

            ArrayList<Validator> signerList = new ArrayList();

            for (int i = 0; i < signerIDList.size(); i++) {
                String temp = signerIDList.get(i);
                for (int j = 0; j < signList.size(); j++) {
                    if (signList.get(j).equals(temp)) {
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
