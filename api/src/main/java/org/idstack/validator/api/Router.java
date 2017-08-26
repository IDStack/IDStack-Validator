package org.idstack.validator.api;

import org.idstack.validator.feature.Constant;
import org.idstack.validator.feature.FeatureImpl;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Chanaka Lakmal
 * @date 23/8/2017
 * @since 1.0
 */
public class Router {

    public boolean saveBasicConfiguration(String org, String email) {
        return FeatureImpl.getFactory().saveBasicConfiguration(org, email);
    }

    public boolean saveDocumentConfiguration(Map<String, String> configurations) {
        return FeatureImpl.getFactory().saveDocumentConfiguration(configurations);
    }

    public boolean saveWhiteListConfiguration(Map<String, String> configurations) {
        return FeatureImpl.getFactory().saveWhiteListConfiguration(configurations);
    }

    public boolean saveBlackListConfiguration(Map<String, String> configurations) {
        return FeatureImpl.getFactory().saveBlackListConfiguration(configurations);
    }

    public String loadConfiguration(String type) {
        return FeatureImpl.getFactory().loadConfiguration(type);
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

    public FileSystemResource getCertificate(String uuid) {
        String src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_FILE_PATH);
        String type = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_TYPE);
        return new FileSystemResource(new File(src + uuid + type));
    }
}
