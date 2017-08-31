package org.idstack.validator.api;

import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Chanaka Lakmal
 * @date 23/8/2017
 * @since 1.0
 */
public class Router {

    public final String configFilePath = getProperty(Constant.GlobalAttribute.CONFIG_FILE_PATH);
    public final String pvtCertFilePath = getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_FILE_PATH);
    public final String pvtCertType = getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_TYPE);
    public final String pvtCertPassword = getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_PASSWORD);
    public final String pubCertFilePath = getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_FILE_PATH);
    public final String pubCertType = getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_TYPE);

    public String signDocument(String json) {
        String output = FeatureImpl.getFactory().signDocument(configFilePath, json);
        if (output.equals(Constant.Status.OK)) {
            // TODO
            // call sign method(document, isContentSignable, urlList, getPrivateCertificate(), getPassword())
            // sign method should sign only the urlList available in the JSON
        }
        return output;
    }

    private String getPrivateCertificate() {
        String src = getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_FILE_PATH);
        String type = getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_TYPE);
        String uuid = (String) FeatureImpl.getFactory().getConfiguration(configFilePath, Constant.GlobalAttribute.BASIC_CONFIG_FILE_NAME, Constant.UUID);
        return src + uuid + type;
    }

    private String getPassword() {
        String src = getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_FILE_PATH);
        String type = getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_PASSWORD_TYPE);
        String uuid = (String) FeatureImpl.getFactory().getConfiguration(configFilePath, Constant.GlobalAttribute.BASIC_CONFIG_FILE_NAME, Constant.UUID);
        return src + uuid + type;
    }

    protected String getProperty(String property) {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(getClass().getClassLoader().getResource(Constant.GlobalAttribute.SYSTEM_PROPERTIES_FILE_NAME).getFile());
            prop.load(input);
            return prop.getProperty(property);
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    protected String getConfigFileName(String type) {
        switch (type) {
            case Constant.GlobalAttribute.BASIC_CONFIG:
                return Constant.GlobalAttribute.BASIC_CONFIG_FILE_NAME;
            case Constant.GlobalAttribute.DOCUMENT_CONFIG:
                return Constant.GlobalAttribute.DOCUMENT_CONFIG_FILE_NAME;
            case Constant.GlobalAttribute.WHITELIST_CONFIG:
                return Constant.GlobalAttribute.WHITELIST_CONFIG_FILE_NAME;
            case Constant.GlobalAttribute.BLACKLIST_CONFIG:
                return Constant.GlobalAttribute.BLACKLIST_CONFIG_FILE_NAME;
            default:
                return null;
        }
    }
}
