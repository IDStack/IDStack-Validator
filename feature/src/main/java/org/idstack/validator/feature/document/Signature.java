package org.idstack.validator.feature.document;

/**
 * @author Chanaka Lakmal
 * @date 31/5/2017
 * @since 1.0
 */
public class Signature {

    private String message_digest;
    private String url;

    public Signature(String message_digest, String url) {
        this.message_digest = message_digest;
        this.url = url;
    }

    public String getMessageDigest() {
        return message_digest;
    }

    public void setMessageDigest(String message_digest) {
        this.message_digest = message_digest;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
