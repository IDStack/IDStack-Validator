package org.idstack.validator.feature.document;

/**
 * @author Chanaka Lakmal
 * @date 31/5/2017
 * @since 1.0
 */
public class Extractor {

    String id;
    Signature signature;

    public Extractor(String id, Signature signature) {
        this.id = id;
        this.signature = signature;
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
}
