package org.idstack.validator.api;

import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

    public String saveBasicConfiguration(String json) {
        return FeatureImpl.getFactory().saveBasicConfiguration(configFilePath, json);
    }

    public String saveDocumentConfiguration(String json) {
        return FeatureImpl.getFactory().saveDocumentConfiguration(configFilePath, json);
    }

    public String saveWhiteListConfiguration(String json) {
        return FeatureImpl.getFactory().saveWhiteListConfiguration(configFilePath, json);
    }

    public String saveBlackListConfiguration(String json) {
        return FeatureImpl.getFactory().saveBlackListConfiguration(configFilePath, json);
    }

    public Object getConfiguration(String type, String property) {
        String src = null;
        switch (type) {
            case Constant.GlobalAttribute.BASIC_CONFIG:
                src = Constant.GlobalAttribute.BASIC_CONFIG_FILE_NAME;
                break;
            case Constant.GlobalAttribute.DOCUMENT_CONFIG:
                src = Constant.GlobalAttribute.DOCUMENT_CONFIG_FILE_NAME;
                break;
            case Constant.GlobalAttribute.WHITELIST_CONFIG:
                src = Constant.GlobalAttribute.WHITELIST_CONFIG_FILE_NAME;
                break;
            case Constant.GlobalAttribute.BLACKLIST_CONFIG:
                src = Constant.GlobalAttribute.BLACKLIST_CONFIG_FILE_NAME;
                break;
            default:
                break;
        }
        return FeatureImpl.getFactory().getConfiguration(configFilePath + src, property);
    }

    public String saveCertificate(String category, MultipartFile certificate, String password) {
        String src = null;
        String type = null;
        boolean flag = false;

        switch (category) {
            case Constant.GlobalAttribute.PUB_CERTIFICATE:
                src = getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_FILE_PATH);
                type = getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_TYPE);
                break;
            case Constant.GlobalAttribute.PVT_CERTIFICATE:
                src = getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_FILE_PATH);
                type = getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_TYPE);
                flag = true;
                break;
            default:
                break;
        }

        File file = FeatureImpl.getFactory().saveCertificate(configFilePath, src, type, flag, password, getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_PASSWORD_TYPE));

        try {
            certificate.transferTo(file);
            return Constant.Status.OK;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FileSystemResource getPublicCertificate(String uuid) {
        return new FileSystemResource(new File(getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_FILE_PATH) + uuid + getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_TYPE)));
    }

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
        String uuid = (String) getConfiguration(Constant.GlobalAttribute.BASIC_CONFIG_FILE_NAME, Constant.UUID);
        return src + uuid + type;
    }

    private String getPassword() {
        String src = getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_FILE_PATH);
        String type = getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_PASSWORD_TYPE);
        String uuid = (String) getConfiguration(Constant.GlobalAttribute.BASIC_CONFIG_FILE_NAME, Constant.UUID);
        return src + uuid + type;
    }

    private String getProperty(String property) {
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
}
