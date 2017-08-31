package org.idstack.validator.api;

import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author Chanaka Lakmal
 * @date 23/8/2017
 * @since 1.0
 */
public class Router {

    public boolean saveBasicConfiguration(String json) {
        return FeatureImpl.getFactory().saveBasicConfiguration(json);
    }

    public boolean saveDocumentConfiguration(String json) {
        return FeatureImpl.getFactory().saveDocumentConfiguration(json);
    }

    public boolean saveWhiteListConfiguration(String json) {
        return FeatureImpl.getFactory().saveWhiteListConfiguration(json);
    }

    public boolean saveBlackListConfiguration(String json) {
        return FeatureImpl.getFactory().saveBlackListConfiguration(json);
    }

    public Object getConfiguration(String type, String property) {
        String src = null;
        switch (type) {
            case "basic":
                src = Constant.GlobalAttribute.BASIC_CONFIG_FILE_NAME;
                break;
            case "document":
                src = Constant.GlobalAttribute.DOCUMENT_CONFIG_FILE_NAME;
                break;
            case "whitelist":
                src = Constant.GlobalAttribute.WHITELIST_CONFIG_FILE_NAME;
                break;
            case "blacklist":
                src = Constant.GlobalAttribute.BLACKLIST_CONFIG_FILE_NAME;
                break;
            default:
                break;
        }
        return FeatureImpl.getFactory().getConfiguration(src, property);
    }

    public boolean saveCertificate(String category, MultipartFile certificate, String password) {
        File file = FeatureImpl.getFactory().saveCertificate(category, password);
        try {
            certificate.transferTo(file);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FileSystemResource getPublicCertificate(String uuid) {
        return new FileSystemResource(new File(FeatureImpl.getFactory().getPublicCertificate(uuid)));
    }


    public String signDocument(String json) {
        return FeatureImpl.getFactory().signDocument(json);
    }
}
