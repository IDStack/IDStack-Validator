package org.idstack.validator.api.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.idstack.feature.Parser;
import org.idstack.feature.configuration.DocConfig;
import org.idstack.feature.configuration.DocumentConfig;
import org.idstack.feature.configuration.list.BlackList;
import org.idstack.feature.configuration.list.WhiteList;
import org.idstack.feature.document.Document;
import org.idstack.feature.document.MetaData;
import org.idstack.feature.document.Validator;
import org.idstack.feature.verification.ExtractorVerifier;
import org.idstack.feature.verification.SignatureVerifier;
import org.idstack.validator.JsonSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * @author Chanaka Lakmal
 * @date 23/8/2017
 * @since 1.0
 */

@Component
public class Router {

    @Autowired
    private ExtractorVerifier extractorVerifier;

    @Autowired
    private SignatureVerifier signatureVerifier;

    protected String signDocumentAutomatically(FeatureImpl feature, String json, String email, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String storeFilePath, String tmpFilePath, String pubFilePath) throws IOException {
        Document document;
        try {
            document = Parser.parseDocumentJson(json);
        } catch (Exception e) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_JSON_INVALID));
        }
        DocumentConfig documentConfig = (DocumentConfig) feature.getConfiguration(configFilePath, Constant.Configuration.DOCUMENT_CONFIG_FILE_NAME);
        DocConfig docConfig = getDocConfig(documentConfig.getDocument(), document.getMetaData().getDocumentType());
        if (docConfig == null)
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.WARN_DOCUMENT_TYPE));

        JsonObject doc = new JsonParser().parse(json).getAsJsonObject();
        JsonObject metadataObject = doc.getAsJsonObject(Constant.JsonAttribute.META_DATA);
        MetaData metaData = new Gson().fromJson(metadataObject.toString(), MetaData.class);

        String uuid = UUID.randomUUID().toString();

        feature.storeDocuments(doc.toString().getBytes(), storeFilePath, configFilePath, pubFilePath, email, metaData.getDocumentType(), Constant.FileExtenstion.JSON, uuid, 1);

        if (!docConfig.isAutomaticProcessable())
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.INFO_WILL_NOTIFY));

        return signDocument(feature, json, Optional.of(uuid), document, docConfig, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType, storeFilePath, tmpFilePath, pubFilePath);
    }

    protected String signDocumentManually(FeatureImpl feature, String json, Optional<String> requestId, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String storeFilePath, String tmpFilePath, String pubFilePath) throws IOException {
        Document document;
        try {
            document = Parser.parseDocumentJson(json);
        } catch (Exception e) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_JSON_INVALID));
        }

        DocumentConfig documentConfig = (DocumentConfig) feature.getConfiguration(configFilePath, Constant.Configuration.DOCUMENT_CONFIG_FILE_NAME);
        DocConfig docConfig = getDocConfig(documentConfig.getDocument(), document.getMetaData().getDocumentType());

        return signDocument(feature, json, requestId, document, docConfig, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType, storeFilePath, tmpFilePath, pubFilePath);
    }

    private String signDocument(FeatureImpl feature, String json, Optional<String> requestId, Document document, DocConfig docConfig, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String storeFilePath, String tmpFilePath, String pubFilePath) {

        if (docConfig.isIssuerEqualExtractor())
            if (!document.getExtractor().getSignature().getUrl().equals(document.getMetaData().getIssuer().getUrl()))
                return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_EXTRACTOR_ISSUER));

        ArrayList<String> urlList = new ArrayList<>();
        urlList.add(document.getExtractor().getSignature().getUrl());
        for (Validator validator : document.getValidators()) {
            urlList.add(validator.getSignature().getUrl());
        }

        WhiteList whiteList = (WhiteList) feature.getConfiguration(configFilePath, Constant.Configuration.WHITELIST_CONFIG_FILE_NAME);
        BlackList blackList = (BlackList) feature.getConfiguration(configFilePath, Constant.Configuration.BLACKLIST_CONFIG_FILE_NAME);
        boolean isBlackListed = !Collections.disjoint(blackList.getBlackList(), urlList);
        boolean isWhiteListed = !Collections.disjoint(whiteList.getWhiteList(), urlList);

        if (isBlackListed)
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_BLACKLISTED));

        if (!docConfig.isContentSignable() && !isWhiteListed)
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_NOTHING_SIGNABLE));

        urlList.retainAll(whiteList.getWhiteList());

        try {
            boolean isValidExtractor = extractorVerifier.verifyExtractorSignature(json, tmpFilePath);
            if (!isValidExtractor)
                return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_EXTRACTOR_SIGNATURE));

            ArrayList<Boolean> isValidValidators = signatureVerifier.verifyJson(json, tmpFilePath);
            if (isValidValidators.contains(false))
                return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VALIDATOR_SIGNATURE));

            JsonSigner jsonSigner = new JsonSigner(feature.getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType),
                    feature.getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType),
                    feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));

            Document validatedDocument = Parser.parseDocumentJson(jsonSigner.signJson(json, docConfig.isContentSignable(), urlList));

            Path jsonPath = feature.createTempFile(new Gson().toJson(validatedDocument).getBytes(), tmpFilePath, UUID.randomUUID().toString() + Constant.FileExtenstion.JSON).toPath();
            String jsonUrl = feature.parseLocalFilePathAsOnlineUrl(jsonPath.toString(), configFilePath);

            // This will send an email to owner with files
            if (requestId.isPresent()) {
                String message = feature.populateEmailBody(requestId.get(), validatedDocument.getMetaData().getDocumentType().toUpperCase(), jsonUrl);
                feature.sendEmail(feature.getEmailByRequestId(storeFilePath, requestId.get()), "IDStack Document Extraction", message);

                // This will add the request id into request configuration list
                feature.saveRequestConfiguration(configFilePath, requestId.get());
            }

            return new Gson().toJson(validatedDocument);

        } catch (IOException | CMSException | CloneNotSupportedException | OperatorCreationException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    protected String saveDocument(FeatureImpl feature, MultipartFile pdf, String configFilePath, String tmpFilePath) throws IOException {
        String tmpPath = feature.createTempFile(pdf.getBytes(), tmpFilePath, UUID.randomUUID().toString() + Constant.FileExtenstion.PDF).toString();
        String tmpUrl = feature.parseLocalFilePathAsOnlineUrl(tmpPath, configFilePath);
        return new Gson().toJson(Collections.singletonMap(Constant.TEMP_URL, tmpUrl));
    }

    protected DocConfig getDocConfig(final List<DocConfig> list, final String name) {
        return list.stream().filter(o -> o.getDocType().equals(name)).findFirst().get();
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