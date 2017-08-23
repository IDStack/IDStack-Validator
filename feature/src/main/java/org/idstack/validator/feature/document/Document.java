package org.idstack.validator.feature.document;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author Chanaka Lakmal
 * @date 31/5/2017
 * @since 1.0
 */
public class Document {

    private MetaData meta_data;
    private LinkedHashMap<String, Object> content;
    private Extractor extractor;
    private ArrayList<Signer> signers;

    public Document(MetaData meta_data, LinkedHashMap<String, Object> content, Extractor extractor, ArrayList<Signer> signers) {
        this.meta_data = meta_data;
        this.content = content;
        this.extractor = extractor;
        this.signers = signers;
    }

    public MetaData getMetaData() {
        return meta_data;
    }

    public void setMetaData(MetaData meta_data) {
        this.meta_data = meta_data;
    }

    public LinkedHashMap<String, Object> getContent() {
        return content;
    }

    public void setContent(LinkedHashMap<String, Object> content) {
        this.content = content;
    }

    public Extractor getExtractor() {
        return extractor;
    }

    public void setExtractor(Extractor extractor) {
        this.extractor = extractor;
    }

    public ArrayList<Signer> getSigners() {
        return signers;
    }

    public void setSigners(ArrayList<Signer> signers) {
        this.signers = signers;
    }
}
