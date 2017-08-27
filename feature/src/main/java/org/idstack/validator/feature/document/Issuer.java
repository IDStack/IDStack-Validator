package org.idstack.validator.feature.document;

/**
 * @author Chanaka Lakmal
 * @date 27/8/2017
 * @since 1.0
 */
public class Issuer {

    private String name;
    private String email;
    private String url;

    public Issuer(String name, String email, String url) {
        this.name = name;
        this.email = email;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
