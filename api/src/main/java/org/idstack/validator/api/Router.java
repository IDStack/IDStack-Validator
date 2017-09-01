package org.idstack.validator.api;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.idstack.feature.Parser;
import org.idstack.feature.document.Document;
import org.idstack.feature.document.Validator;
import org.idstack.validator.JsonSigner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
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
    public final String pvtCertPasswordType = getProperty(Constant.GlobalAttribute.PVT_CERTIFICATE_PASSWORD_TYPE);
    public final String pubCertFilePath = getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_FILE_PATH);
    public final String pubCertType = getProperty(Constant.GlobalAttribute.PUB_CERTIFICATE_TYPE);

    public String signDocument(String json) {

        Document document = Parser.parseDocumentJson(json);

        ArrayList<String> urlList = new ArrayList<>();
        urlList.add(document.getExtractor().getSignature().getUrl());
        for (Validator validator : document.getValidators()) {
            urlList.add(validator.getSignature().getUrl());
        }

        Properties whitelist = (Properties) FeatureImpl.getFactory().getConfiguration(configFilePath, Constant.GlobalAttribute.WHITELIST_CONFIG_FILE_NAME, Constant.ALL);
        Properties blacklist = (Properties) FeatureImpl.getFactory().getConfiguration(configFilePath, Constant.GlobalAttribute.BLACKLIST_CONFIG_FILE_NAME, Constant.ALL);
        boolean isBlackListed = !Collections.disjoint(blacklist.values(), urlList);
        boolean isWhiteListed = !Collections.disjoint(whitelist.values(), urlList);

        if (!isBlackListed) {
            String documentConfig = (String) FeatureImpl.getFactory().getConfiguration(configFilePath, Constant.GlobalAttribute.DOCUMENT_CONFIG_FILE_NAME, document.getMetaData().getDocumentType());
            boolean isAutomaticProcessable = Boolean.parseBoolean(documentConfig.split(",")[0]);
            boolean isExtractorIssuer = Boolean.parseBoolean(documentConfig.split(",")[1]);
            boolean isContentSignable = Boolean.parseBoolean(documentConfig.split(",")[2]);

            if (isAutomaticProcessable) {
                // TODO : improve this by checking 'issuer in the validators list'
                if (isExtractorIssuer)
                    if (!document.getExtractor().getSignature().getUrl().equals(document.getMetaData().getIssuer().getUrl()))
                        return "Extractor should be the issuer";

                if (!isContentSignable && !isWhiteListed)
                    return "Nothing to be signed";

                urlList.retainAll(whitelist.values());

                try {
                    JsonSigner jsonSigner = new JsonSigner(FeatureImpl.getFactory().getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType), FeatureImpl.getFactory().getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType));
                    return jsonSigner.signJson(json, isContentSignable, urlList);
                } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | CMSException | CloneNotSupportedException | NoSuchProviderException | OperatorCreationException | KeyStoreException e) {
                    throw new RuntimeException(e);
                }
            }

            return "Wait";
        }

        throw new IllegalArgumentException("One or more signatures are blacklisted");
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
