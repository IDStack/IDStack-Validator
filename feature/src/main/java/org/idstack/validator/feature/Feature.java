package org.idstack.validator.feature;

import java.io.File;
import java.util.Map;

/**
 * @author Chanaka Lakmal
 * @date 23/8/2017
 * @since 1.0
 */
public interface Feature {

    String getProperty(String property);

    boolean saveBasicConfiguration(String org, String email);

    boolean saveDocumentConfiguration(Map<String, String> configurations);

    boolean saveWhiteListConfiguration(Map<String, String> configurations);

    boolean saveBlackListConfiguration(Map<String, String> configurations);

    Object getConfiguration(String type, String property);

    File saveCertificate(String category, String password);

    String signDocument(String json);

}
