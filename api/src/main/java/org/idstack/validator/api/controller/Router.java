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

import java.io.IOException;
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

    @Autowired
    private SignedResponse signedResponse;

    protected String signDocumentAutomatically(FeatureImpl feature, String json, MultipartFile pdf, String email, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String storeFilePath, String tmpFilePath, String pubFilePath) throws IOException {
        Document document = Parser.parseDocumentJson(json);
        String documentConfig = (String) feature.getConfiguration(configFilePath, Constant.Configuration.DOCUMENT_CONFIG_FILE_NAME, Optional.of(document.getMetaData().getDocumentType()));
        if (documentConfig == null)
            return "Cannot process document type : " + document.getMetaData().getDocumentType();

        JsonObject doc = new JsonParser().parse(json).getAsJsonObject();
        JsonObject metadataObject = doc.getAsJsonObject(Constant.JsonAttribute.META_DATA);
        MetaData metaData = new Gson().fromJson(metadataObject.toString(), MetaData.class);
        String pdfUrl = feature.storeDocuments(pdf.getBytes(), storeFilePath, configFilePath, pubFilePath, email, metaData.getDocumentType(), Constant.FileExtenstion.JSON, UUID.randomUUID().toString(), 1);

        boolean isAutomaticProcessable = Boolean.parseBoolean(documentConfig.split(",")[0]);
        if (!isAutomaticProcessable)
            return "Wait";

        return signDocument(feature, json, pdfUrl, document, documentConfig, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType, tmpFilePath, pubFilePath);
    }

    protected String signDocumentManually(FeatureImpl feature, String json, String pdfUrl, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String tmpFilePath, String pubFilePath) throws IOException {
        Document document = Parser.parseDocumentJson(json);
        String documentConfig = (String) feature.getConfiguration(configFilePath, Constant.Configuration.DOCUMENT_CONFIG_FILE_NAME, Optional.of(document.getMetaData().getDocumentType()));
        return signDocument(feature, json, pdfUrl, document, documentConfig, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType, tmpFilePath, pubFilePath);
    }

    private String signDocument(FeatureImpl feature, String json, String pdfUrl, Document document, String documentConfig, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String tmpFilePath, String pubFilePath) {

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
            boolean isValidExtractor = extractorVerifier.verifyExtractorSignature(json, tmpFilePath);
            if (!isValidExtractor)
                return "Extractor's signature is not valid";
            ArrayList<Boolean> isValidValidators = signatureVerifier.verifyJson(json, tmpFilePath);
            if (isValidValidators.contains(false))
                return "One or more validator signatures are not valid";

            String sigID = UUID.randomUUID().toString();
            String localPdfUrl = feature.parseUrlAsLocalFile(pdfUrl, pubFilePath);

            String hashInPdf = new JsonPdfMapper().getHashOfTheOriginalContent(localPdfUrl);
            String hashInJson = document.getMetaData().getPdfHash();

            if (!(hashInJson.equals(hashInPdf))) {
                return "Pdf and the machine readable file are not not matching each other";
            }

            PdfCertifier pdfCertifier = new PdfCertifier(feature.getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType), feature.getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType), feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));

            boolean verifiedPdf = pdfCertifier.verifySignatures(localPdfUrl);
            if (!verifiedPdf) {
                return "One or more signatures in the Pdf are invalid";
            }
            pdfCertifier.signPdf(localPdfUrl, sigID);

            JsonSigner jsonSigner = new JsonSigner(feature.getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType),
                    feature.getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType),
                    feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));

            //TODO : return signed response object with both pdf + json

            signedResponse.setJson(jsonSigner.signJson(json, isContentSignable, urlList));
            signedResponse.setPdf("signed_"+localPdfUrl);

            return new Gson().toJson(signedResponse);

        } catch (IOException | CMSException | CloneNotSupportedException | OperatorCreationException | GeneralSecurityException | DocumentException e) {
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
