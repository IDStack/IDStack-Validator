package org.idstack.validator.api;

import org.idstack.validator.feature.Constant;
import org.idstack.validator.feature.FeatureImpl;

import java.io.*;
import java.util.Properties;

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
}
