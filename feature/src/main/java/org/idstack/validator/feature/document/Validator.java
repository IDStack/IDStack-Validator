package org.idstack.validator.feature.document;

import java.util.ArrayList;

/**
 * @author Chanaka Lakmal
 * @date 31/5/2017
 * @since 1.0
 */
public class Validator {

    String id;
    Signature signature;
    boolean signed_content;
    ArrayList<String> signed_signatures;

    public Validator(String id, Signature signature, boolean signed_content, ArrayList<String> signed_signatures) {
        this.id = id;
        this.signature = signature;
        this.signed_content = signed_content;
        this.signed_signatures = signed_signatures;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public boolean getSignedAttributes() {
        return signed_content;
    }

    public void setSignedAttributes(boolean signed_content) {
        this.signed_content = signed_content;
    }

    public ArrayList<String> getSignedSignatures() {
        return signed_signatures;
    }

    public void setSignedSignatures(ArrayList<String> signed_signatures) {
        this.signed_signatures = signed_signatures;
    }
}
