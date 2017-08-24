package org.idstack.validator.api;

import org.idstack.validator.feature.Constant;
import org.idstack.validator.feature.FeatureImpl;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;

/**
 * @author Chanaka Lakmal
 * @date 23/8/2017
 * @since 1.0
 */
public class Router {

    public boolean saveConfigurationFile() {
        String src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.CONFIG_FILE_PATH);
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            output = new FileOutputStream(src + Constant.GlobalAttribute.CONFIG_FILE_NAME);
            prop.setProperty("ORGANIZATION", "University of Moratuwa");
            prop.setProperty("EMAIL", "info@mrt.ac.lk");
            prop.store(output, null);
            return true;
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public String loadConfigurationFile() {
        String src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.CONFIG_FILE_PATH);
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(src + Constant.GlobalAttribute.CONFIG_FILE_NAME);
            prop.load(input);
            return prop.toString();
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

    public boolean saveCertificate(String category, MultipartFile certificate) {
        String src = null;
        String type = null;

        switch (category) {
            case Constant.GlobalAttribute.PUB_CERTIFICATE:
                src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_FILE_PATH);
                type = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_TYPE);
                break;
            case Constant.GlobalAttribute.PVT_CERTIFICATE:
                src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_FILE_PATH);
                type = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_TYPE);
                break;
            default:
                break;
        }

        UUID uuid = UUID.randomUUID();
        File file = new File(src + uuid + type);

        try {
            Files.createDirectories(Paths.get(src));
            certificate.transferTo(file);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
