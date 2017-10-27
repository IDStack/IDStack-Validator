package org.idstack.validator.api.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextpdf.text.DocumentException;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.idstack.feature.Parser;
import org.idstack.feature.configuration.BlackListConfig;
import org.idstack.feature.configuration.DocConfig;
import org.idstack.feature.configuration.DocumentConfig;
import org.idstack.feature.configuration.WhiteListConfig;
import org.idstack.feature.document.Document;
import org.idstack.feature.document.MetaData;
import org.idstack.feature.document.Validator;
import org.idstack.feature.response.SignedResponse;
import org.idstack.feature.sign.pdf.JsonPdfMapper;
import org.idstack.feature.sign.pdf.PdfCertifier;
import org.idstack.feature.verification.ExtractorVerifier;
import org.idstack.feature.verification.SignatureVerifier;
import org.idstack.validator.JsonSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    @Autowired
    private SignedResponse signedResponse;

    protected String signDocumentAutomatically(FeatureImpl feature, String json, MultipartFile pdf, String email, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String storeFilePath, String tmpFilePath, String pubFilePath) throws IOException {
        Document document = Parser.parseDocumentJson(json);
        DocumentConfig documentConfig = (DocumentConfig) feature.getConfiguration(configFilePath, Constant.Configuration.DOCUMENT_CONFIG_FILE_NAME);
        DocConfig docConfig = getDocConfig(documentConfig.getDocument(), document.getMetaData().getDocumentType());
        if (docConfig == null)
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.WARN_DOCUMENT_TYPE));

        JsonObject doc = new JsonParser().parse(json).getAsJsonObject();
        JsonObject metadataObject = doc.getAsJsonObject(Constant.JsonAttribute.META_DATA);
        MetaData metaData = new Gson().fromJson(metadataObject.toString(), MetaData.class);

        String uuid = UUID.randomUUID().toString();
        String pdfUrl = feature.storeDocuments(pdf.getBytes(), storeFilePath, configFilePath, pubFilePath, email, metaData.getDocumentType(), Constant.FileExtenstion.PDF, uuid, 1);
        feature.storeDocuments(doc.toString().getBytes(), storeFilePath, configFilePath, pubFilePath, email, metaData.getDocumentType(), Constant.FileExtenstion.JSON, uuid, 1);

        if (!docConfig.isAutomaticProcessable())
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.WARN_WILL_NOTIFY));

        return signDocument(feature, json, pdfUrl, document, docConfig, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType, tmpFilePath, pubFilePath);
    }

    protected String signDocumentManually(FeatureImpl feature, String json, String pdfUrl, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String storeFilePath, String tmpFilePath, String pubFilePath) throws IOException {
        Document document = Parser.parseDocumentJson(json);
        DocumentConfig documentConfig = (DocumentConfig) feature.getConfiguration(configFilePath, Constant.Configuration.DOCUMENT_CONFIG_FILE_NAME);
        DocConfig docConfig = getDocConfig(documentConfig.getDocument(), document.getMetaData().getDocumentType());
        return signDocument(feature, json, pdfUrl, document, docConfig, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType, tmpFilePath, pubFilePath);
    }

    private String signDocument(FeatureImpl feature, String json, String pdfUrl, Document document, DocConfig docConfig, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String tmpFilePath, String pubFilePath) {

        if (docConfig.isIssuerEqualExtractor())
            if (!document.getExtractor().getSignature().getUrl().equals(document.getMetaData().getIssuer().getUrl()))
                return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_EXTRACTOR_ISSUER));

        ArrayList<String> urlList = new ArrayList<>();
        urlList.add(document.getExtractor().getSignature().getUrl());
        for (Validator validator : document.getValidators()) {
            urlList.add(validator.getSignature().getUrl());
        }

        WhiteListConfig whiteListConfig = (WhiteListConfig) feature.getConfiguration(configFilePath, Constant.Configuration.WHITELIST_CONFIG_FILE_NAME);
        BlackListConfig blackListConfig = (BlackListConfig) feature.getConfiguration(configFilePath, Constant.Configuration.BLACKLIST_CONFIG_FILE_NAME);
        boolean isBlackListed = !Collections.disjoint(blackListConfig.getBlackList(), urlList);
        boolean isWhiteListed = !Collections.disjoint(whiteListConfig.getWhiteList(), urlList);

        if (isBlackListed)
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_BLACKLISTED));

        if (!docConfig.isContentSignable() && !isWhiteListed)
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_NOTHING_SIGNABLE));

        urlList.retainAll(whiteListConfig.getWhiteList());

        try {
            boolean isValidExtractor = extractorVerifier.verifyExtractorSignature(json, tmpFilePath);
            if (!isValidExtractor)
                return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_EXTRACTOR_SIGNATURE));

            ArrayList<Boolean> isValidValidators = signatureVerifier.verifyJson(json, tmpFilePath);
            if (isValidValidators.contains(false))
                return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VALIDATOR_SIGNATURE));

            String sigID = UUID.randomUUID().toString();
            String pdfPath = feature.parseUrlAsLocalFilePath(pdfUrl, pubFilePath);

            String hashInPdf = new JsonPdfMapper().getHashOfTheOriginalContent(pdfPath);
            if (hashInPdf == null)
                return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PDF_NOT_SIGNED));

            String hashInJson = document.getMetaData().getPdfHash();

            //TODO : Uncomment after modifying hashing mechanism
            if (!(hashInJson.equals(hashInPdf))) {
                //return "Pdf and the machine readable file are not not matching each other";
            }

            PdfCertifier pdfCertifier = new PdfCertifier(feature.getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType), feature.getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType), feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));

            boolean verifiedPdf = pdfCertifier.verifySignatures(pdfPath);
            if (!verifiedPdf)
                return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PDF_SIGNATURES));

            String signedPdfPath = tmpFilePath + Constant.SIGNED + File.separator;
            Files.createDirectories(Paths.get(signedPdfPath));

            signedPdfPath = pdfCertifier.signPdf(pdfPath, signedPdfPath, sigID);

            JsonSigner jsonSigner = new JsonSigner(feature.getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType),
                    feature.getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType),
                    feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));

            signedResponse.setJson(new Parser().parseDocumentJson(jsonSigner.signJson(json, docConfig.isContentSignable(), urlList)));
            signedResponse.setPdf(feature.parseLocalFilePathAsOnlineUrl(signedPdfPath, configFilePath));

            return new Gson().toJson(signedResponse);

        } catch (IOException | CMSException | CloneNotSupportedException | OperatorCreationException | GeneralSecurityException | DocumentException e) {
            throw new RuntimeException(e);
        }
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