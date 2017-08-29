package org.idstack.validator.feature;

import org.idstack.validator.feature.document.Document;
import org.idstack.validator.feature.document.Validator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Chanaka Lakmal
 * @date 23/8/2017
 * @since 1.0
 */

public class FeatureImpl implements Feature {

    private static FeatureImpl feature = null;

    public static FeatureImpl getFactory() {
        if (feature == null) {
            return new FeatureImpl();
        }
        return feature;
    }

    @Override
    public String getProperty(String property) {
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

    @Override
    public boolean saveBasicConfiguration(String org, String email) {
        String src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.CONFIG_FILE_PATH);
        String uuid = (String) FeatureImpl.getFactory().getConfiguration(Constant.GlobalAttribute.BASIC_CONFIG_FILE_NAME, "UUID");
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            Files.createDirectories(Paths.get(src));
            output = new FileOutputStream(src + Constant.GlobalAttribute.BASIC_CONFIG_FILE_NAME);
            prop.setProperty("ORGANIZATION", org);
            prop.setProperty("EMAIL", email);
            prop.setProperty("UUID", UUID.randomUUID().toString());
            if (uuid != null)
                prop.setProperty("UUID", uuid);
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

    @Override
    public boolean saveDocumentConfiguration(Map<String, String> configurations) {
        ArrayList<String> configList = (ArrayList<String>) Stream.of(configurations.get("document").split(",")).collect(Collectors.toList());
        String src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.CONFIG_FILE_PATH);
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            Files.createDirectories(Paths.get(src));
            output = new FileOutputStream(src + Constant.GlobalAttribute.DOCUMENT_CONFIG_FILE_NAME);
            for (String config : configList) {
                prop.setProperty(config.split("#")[0].trim(), config.split("#")[1].trim());
            }
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

    @Override
    public boolean saveWhiteListConfiguration(Map<String, String> configurations) {
        ArrayList<String> configList = (ArrayList<String>) Stream.of(configurations.get("whitelist").split(",")).collect(Collectors.toList());
        String src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.CONFIG_FILE_PATH);
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            Files.createDirectories(Paths.get(src));
            output = new FileOutputStream(src + Constant.GlobalAttribute.WHITELIST_CONFIG_FILE_NAME);
            for (int i = 0; i < configList.size(); i++) {
                prop.setProperty("WHITE_URL_" + i, configList.get(i).trim());
            }
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

    @Override
    public boolean saveBlackListConfiguration(Map<String, String> configurations) {
        ArrayList<String> configList = (ArrayList<String>) Stream.of(configurations.get("blacklist").split(",")).collect(Collectors.toList());
        String src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.CONFIG_FILE_PATH);
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            Files.createDirectories(Paths.get(src));
            output = new FileOutputStream(src + Constant.GlobalAttribute.BLACKLIST_CONFIG_FILE_NAME);
            for (int i = 0; i < configList.size(); i++) {
                prop.setProperty("BLACK_URL_" + i, configList.get(i).trim());
            }
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

    @Override
    public Object getConfiguration(String fileName, String property) {
        String src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.CONFIG_FILE_PATH) + fileName;

        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(src);
            prop.load(input);
            if (property.equals("*"))
                return prop;
            else
                return prop.get(property);
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

    @Override
    public File saveCertificate(String category, String password) {
        String src = null;
        String type = null;
        boolean flag = false;

        switch (category) {
            case Constant.GlobalAttribute.PUB_CERTIFICATE:
                src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_FILE_PATH);
                type = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_TYPE);
                break;
            case Constant.GlobalAttribute.PVT_CERTIFICATE:
                src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_FILE_PATH);
                type = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_TYPE);
                flag = true;
                break;
            default:
                break;
        }

        try {
            Files.createDirectories(Paths.get(src));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String uuid = (String) FeatureImpl.getFactory().getConfiguration(Constant.GlobalAttribute.BASIC_CONFIG_FILE_NAME, "UUID");

        if (flag) {
            Properties prop = new Properties();
            OutputStream output = null;
            try {
                output = new FileOutputStream(src + uuid + FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_PASSWORD_TYPE));
                prop.setProperty("PASSWORD", password);
                prop.store(output, null);
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

        return new File(src + uuid + type);
    }

    @Override
    public String signDocument(String json) {

        Document document = Parser.parseDocumentJson(json);

        ArrayList<String> urlList = new ArrayList<>();
        urlList.add(document.getExtractor().getSignature().getUrl());
        for (Validator validator : document.getValidators()) {
            urlList.add(validator.getSignature().getUrl());
        }

        Properties whitelist = (Properties) getConfiguration(Constant.GlobalAttribute.WHITELIST_CONFIG_FILE_NAME, "*");
        Properties blacklist = (Properties) getConfiguration(Constant.GlobalAttribute.BLACKLIST_CONFIG_FILE_NAME, "*");
        boolean isBlackListed = !Collections.disjoint(blacklist.values(), urlList);
        boolean isWhiteListed = !Collections.disjoint(whitelist.values(), urlList);

        if (!isBlackListed) {
            String documentConfig = (String) FeatureImpl.getFactory().getConfiguration(Constant.GlobalAttribute.DOCUMENT_CONFIG_FILE_NAME, document.getMetaData().getDocumentType());
            boolean isAutomaticProcessable = Boolean.parseBoolean(documentConfig.split(":")[0]);
            boolean isExtractorIssuer = Boolean.parseBoolean(documentConfig.split(":")[1]);
            boolean isContentSignable = Boolean.parseBoolean(documentConfig.split(":")[2]);

            if (isAutomaticProcessable) {
                // TODO : improve this by checking 'issuer in the validators list'
                if (isExtractorIssuer)
                    if (!document.getExtractor().getSignature().getUrl().equals(document.getMetaData().getIssuer().getUrl()))
                        return "Extractor should be the issuer";

                if (!isContentSignable && !isWhiteListed)
                    return "Nothing to be signed";

                boolean flag = urlList.retainAll(whitelist.values());
                if (flag) {
                    // TODO
                    // call sign method(document, isContentSignable, urlList)
                    // sign method should sign only the urlList available in the JSON
                }
            }

            return "Wait";
        }

        throw new IllegalArgumentException("One or more signatures are blacklisted");
    }

    @Override
    public String getPublicCertificate(String uuid) {
        String src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_FILE_PATH);
        String type = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_TYPE);
        return src + uuid + type;
    }

    @Override
    public String getPrivateCertificate() {
        String src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_FILE_PATH);
        String type = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_TYPE);
        String uuid = (String) FeatureImpl.getFactory().getConfiguration(Constant.GlobalAttribute.BASIC_CONFIG_FILE_NAME, "UUID");
        return src + uuid + type;
    }

    @Override
    public String getPassword() {
        String src = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_FILE_PATH);
        String type = FeatureImpl.getFactory().getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_PASSWORD_TYPE);
        String uuid = (String) FeatureImpl.getFactory().getConfiguration(Constant.GlobalAttribute.BASIC_CONFIG_FILE_NAME, "UUID");
        return src + uuid + type;
    }

}
