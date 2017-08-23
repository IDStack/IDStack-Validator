package org.idstack.validator.feature.document;

/**
 * @author Chanaka Lakmal
 * @date 31/5/2017
 * @since 1.0
 */
public class MetaData {

    private String name;
    private String version;
    private String document_id;
    private String document_type;
    private String issuer;

    public MetaData(String name, String version, String document_id, String document_type, String issuer) {
        this.name = name;
        this.version = version;
        this.document_id = document_id;
        this.document_type = document_type;
        this.issuer = issuer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDocumentId() {
        return document_id;
    }

    public void setDocumentId(String document_id) {
        this.document_id = document_id;
    }

    public String getDocumentType() {
        return document_type;
    }

    public void setDocumentType(String document_type) {
        this.document_type = document_type;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
