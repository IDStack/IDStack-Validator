package org.idstack.validator.feature;

import java.io.File;
import java.util.Map;
import java.util.Properties;

/**
 * @author Chanaka Lakmal
 * @date 23/8/2017
 * @since 1.0
 */
public interface Feature {

    String getProperty(String propertyFile, String property);

    boolean saveBasicConfiguration(String org, String email);

    boolean saveDocumentConfiguration(Map<String, String> configurations);

    boolean saveWhiteListConfiguration(Map<String, String> configurations);

    boolean saveBlackListConfiguration(Map<String, String> configurations);

    Properties loadConfiguration(String type);

    File saveCertificate(String category, String password);

    String signDocument(String json);

}
