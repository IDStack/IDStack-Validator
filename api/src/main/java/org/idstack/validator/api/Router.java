package org.idstack.validator.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.idstack.feature.Parser;
import org.idstack.feature.document.Document;
import org.idstack.feature.document.MetaData;
import org.idstack.feature.document.Validator;
import org.idstack.validator.JsonSigner;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * @author Chanaka Lakmal
 * @date 23/8/2017
 * @since 1.0
 */

@Component
public class Router {

    protected String signDocumentAutomatically(FeatureImpl feature, String json, MultipartFile pdf, String email, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String storeFilePath) throws IOException {
        Document document = Parser.parseDocumentJson(json);
        String documentConfig = (String) feature.getConfiguration(configFilePath, Constant.Configuration.DOCUMENT_CONFIG_FILE_NAME, Optional.of(document.getMetaData().getDocumentType()));
        if (documentConfig == null)
            return "Cannot process document type : " + document.getMetaData().getDocumentType();
        boolean isAutomaticProcessable = Boolean.parseBoolean(documentConfig.split(",")[0]);
        if (!isAutomaticProcessable) {
            JsonObject doc = new JsonParser().parse(json).getAsJsonObject();
            JsonObject metadataObject = doc.getAsJsonObject(Constant.JsonAttribute.META_DATA);
            MetaData metaData = new Gson().fromJson(metadataObject.toString(), MetaData.class);
            feature.storeDocuments(pdf.getBytes(), storeFilePath, email, metaData.getDocumentType(), Constant.FileExtenstion.JSON, UUID.randomUUID().toString());
            return "Wait";
        }
        return signDocument(feature, json, pdf, document, documentConfig, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType);
    }

    protected String signDocumentManually(FeatureImpl feature, String json, MultipartFile pdf, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType) throws IOException {
        Document document = Parser.parseDocumentJson(json);
        String documentConfig = (String) feature.getConfiguration(configFilePath, Constant.Configuration.DOCUMENT_CONFIG_FILE_NAME, Optional.of(document.getMetaData().getDocumentType()));
        return signDocument(feature, json, pdf, document, documentConfig, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType);
    }

    // TODO : improve this by checking 'issuer in the validators list'
    // TODO : restrict to sign by previous signer
    private String signDocument(FeatureImpl feature, String json, MultipartFile pdf, Document document, String documentConfig, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType) {

        boolean isExtractorIssuer = Boolean.parseBoolean(documentConfig.split(",")[1]);
        boolean isContentSignable = Boolean.parseBoolean(documentConfig.split(",")[2]);

        if (isExtractorIssuer)
            if (!document.getExtractor().getSignature().getUrl().equals(document.getMetaData().getIssuer().getUrl()))
                return "Extractor should be the issuer";

        ArrayList<String> urlList = new ArrayList<>();
        urlList.add(document.getExtractor().getSignature().getUrl());
        for (Validator validator : document.getValidators()) {
            urlList.add(validator.getSignature().getUrl());
        }

        Properties whitelist = (Properties) feature.getConfiguration(configFilePath, Constant.Configuration.WHITELIST_CONFIG_FILE_NAME, Optional.empty());
        Properties blacklist = (Properties) feature.getConfiguration(configFilePath, Constant.Configuration.BLACKLIST_CONFIG_FILE_NAME, Optional.empty());
        boolean isBlackListed = !Collections.disjoint(blacklist.values(), urlList);
        boolean isWhiteListed = !Collections.disjoint(whitelist.values(), urlList);

        if (isBlackListed)
            return "One or more signatures are blacklisted";

        if (!isContentSignable && !isWhiteListed)
            return "Nothing to be signed";

        urlList.retainAll(whitelist.values());

        try {
            //TODO : call sign pdf method and return pdf as well
            JsonSigner jsonSigner = new JsonSigner(feature.getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType),
                    feature.getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType),
                    feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));
            return jsonSigner.signJson(json, isContentSignable, urlList);
        } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | CMSException | CloneNotSupportedException | NoSuchProviderException | OperatorCreationException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getConfigFileName(String type) {
        switch (type) {
            case Constant.Configuration.BASIC_CONFIG:
                return Constant.Configuration.BASIC_CONFIG_FILE_NAME;
            case Constant.Configuration.DOCUMENT_CONFIG:
                return Constant.Configuration.DOCUMENT_CONFIG_FILE_NAME;
            case Constant.Configuration.WHITELIST_CONFIG:
                return Constant.Configuration.WHITELIST_CONFIG_FILE_NAME;
            case Constant.Configuration.BLACKLIST_CONFIG:
                return Constant.Configuration.BLACKLIST_CONFIG_FILE_NAME;
            default:
                return null;
        }
    }
}
