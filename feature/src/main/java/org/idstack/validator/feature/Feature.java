package org.idstack.validator.feature;

import java.io.File;
import java.util.Properties;

/**
 * @author Chanaka Lakmal
 * @date 23/8/2017
 * @since 1.0
 */
public interface Feature {

    String getProperty(String property);

    boolean saveBasicConfiguration(String json);

    boolean saveDocumentConfiguration(String json);

    boolean saveWhiteListConfiguration(String json);

    boolean saveBlackListConfiguration(String json);

    Object getConfiguration(String type, String property);

    File saveCertificate(String category, String password);

    String signDocument(String json);

    String getPublicCertificate(String uuid);

    String getPrivateCertificate();

    String getPassword();

    Properties parseJson(String json);

}
