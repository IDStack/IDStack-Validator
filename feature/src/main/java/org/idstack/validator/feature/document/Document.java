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
    private ArrayList<Validator> validators;

    public Document(MetaData meta_data, LinkedHashMap<String, Object> content, Extractor extractor, ArrayList<Validator> validators) {
        this.meta_data = meta_data;
        this.content = content;
        this.extractor = extractor;
        this.validators = validators;
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

    public ArrayList<Validator> getValidators() {
        return validators;
    }

    public void setValidators(ArrayList<Validator> validators) {
        this.validators = validators;
    }
}
