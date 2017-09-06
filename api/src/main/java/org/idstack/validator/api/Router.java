package org.idstack.validator.api;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.idstack.feature.Parser;
import org.idstack.feature.document.Document;
import org.idstack.feature.document.Validator;
import org.idstack.validator.JsonSigner;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

@Component
public class Router {

    public final String apiKey = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.API_KEY);
    public final String configFilePath = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.CONFIG_FILE_PATH);
    public final String pvtCertFilePath = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PVT_CERTIFICATE_FILE_PATH);
    public final String pvtCertType = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PVT_CERTIFICATE_TYPE);
    public final String pvtCertPasswordType = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PVT_CERTIFICATE_PASSWORD_TYPE);
    public final String pubCertFilePath = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PUB_CERTIFICATE_FILE_PATH);
    public final String pubCertType = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PUB_CERTIFICATE_TYPE);

    public String signDocument(String json) {

        Document document = Parser.parseDocumentJson(json);
        String documentConfig = (String) FeatureImpl.getFactory().getConfiguration(configFilePath, Constant.GlobalAttribute.DOCUMENT_CONFIG_FILE_NAME, document.getMetaData().getDocumentType());

        if (documentConfig == null)
            return "Cannot process document type : " + document.getMetaData().getDocumentType();

        boolean isAutomaticProcessable = Boolean.parseBoolean(documentConfig.split(",")[0]);

        if (!isAutomaticProcessable)
            return "Wait";

        boolean isExtractorIssuer = Boolean.parseBoolean(documentConfig.split(",")[1]);

        if (isExtractorIssuer)
            if (!document.getExtractor().getSignature().getUrl().equals(document.getMetaData().getIssuer().getUrl()))
                return "Extractor should be the issuer";
        // TODO : improve this by checking 'issuer in the validators list'

        boolean isContentSignable = Boolean.parseBoolean(documentConfig.split(",")[2]);

        ArrayList<String> urlList = new ArrayList<>();
        urlList.add(document.getExtractor().getSignature().getUrl());
        for (Validator validator : document.getValidators()) {
            urlList.add(validator.getSignature().getUrl());
        }

        Properties whitelist = (Properties) FeatureImpl.getFactory().getConfiguration(configFilePath, Constant.GlobalAttribute.WHITELIST_CONFIG_FILE_NAME, Constant.ALL);
        Properties blacklist = (Properties) FeatureImpl.getFactory().getConfiguration(configFilePath, Constant.GlobalAttribute.BLACKLIST_CONFIG_FILE_NAME, Constant.ALL);
        boolean isBlackListed = !Collections.disjoint(blacklist.values(), urlList);
        boolean isWhiteListed = !Collections.disjoint(whitelist.values(), urlList);

        if (isBlackListed)
            return "One or more signatures are blacklisted";

        if (!isContentSignable && !isWhiteListed)
            return "Nothing to be signed";

        urlList.retainAll(whitelist.values());

        try {
            JsonSigner jsonSigner = new JsonSigner(FeatureImpl.getFactory().getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType),
                    FeatureImpl.getFactory().getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType),
                    FeatureImpl.getFactory().getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));
            return jsonSigner.signJson(json, isContentSignable, urlList);
        } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | CMSException | CloneNotSupportedException | NoSuchProviderException | OperatorCreationException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private FileInputStream getPropertiesFile() {
        try {
            return new FileInputStream(getClass().getClassLoader().getResource(Constant.GlobalAttribute.SYSTEM_PROPERTIES_FILE_NAME).getFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
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
