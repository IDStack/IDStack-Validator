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
import org.idstack.feature.configuration.DocConfig;
import org.idstack.feature.configuration.DocumentConfig;
import org.idstack.feature.configuration.list.BlackList;
import org.idstack.feature.configuration.list.WhiteList;
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
import java.nio.file.Path;
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
        String pdfUrl = feature.storeDocuments(pdf.getBytes(), storeFilePath, configFilePath, pubFilePath, email, metaData.getDocumentType(), Constant.FileExtenstion.PDF, uuid, 1);
        feature.storeDocuments(doc.toString().getBytes(), storeFilePath, configFilePath, pubFilePath, email, metaData.getDocumentType(), Constant.FileExtenstion.JSON, uuid, 1);

        if (!docConfig.isAutomaticProcessable())
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.INFO_WILL_NOTIFY));

        return signDocument(feature, json, pdfUrl, document, docConfig, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType, storeFilePath, tmpFilePath, pubFilePath, uuid);
    }

    protected String signDocumentManually(FeatureImpl feature, String json, String pdfUrl, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String storeFilePath, String tmpFilePath, String pubFilePath, String requestId) throws IOException {
        Document document;
        try {
            document = Parser.parseDocumentJson(json);
        } catch (Exception e) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_JSON_INVALID));
        }
        DocumentConfig documentConfig = (DocumentConfig) feature.getConfiguration(configFilePath, Constant.Configuration.DOCUMENT_CONFIG_FILE_NAME);
        DocConfig docConfig = getDocConfig(documentConfig.getDocument(), document.getMetaData().getDocumentType());

        return signDocument(feature, json, pdfUrl, document, docConfig, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType, storeFilePath, tmpFilePath, pubFilePath, requestId);
    }

    private String signDocument(FeatureImpl feature, String json, String pdfUrl, Document document, DocConfig docConfig, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String storeFilePath, String tmpFilePath, String pubFilePath, String requestId) {

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

            String sigID = UUID.randomUUID().toString();
            String pdfPath = feature.parseUrlAsLocalFilePath(pdfUrl, pubFilePath);

            if (!Files.exists(Paths.get(pdfPath)))
                return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_FILE_NOT_FOUND));

            String hashInPdf = new JsonPdfMapper().getHashOfTheOriginalContent(pdfPath);
            if (hashInPdf == null)
                return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PDF_NOT_SIGNED));

            String hashInJson = document.getMetaData().getPdf();

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

            Document validatedDocument = Parser.parseDocumentJson(jsonSigner.signJson(json, docConfig.isContentSignable(), urlList));
            feature.parseLocalFilePathAsOnlineUrl(signedPdfPath, configFilePath);

            Path jsonFilePath = Files.write(Paths.get(tmpFilePath).resolve(Paths.get(UUID.randomUUID().toString() + Constant.FileExtenstion.JSON)), new Gson().toJson(validatedDocument).getBytes());
            String finalJsonUrl = feature.parseLocalFilePathAsOnlineUrl(jsonFilePath.toString(), configFilePath);
            String finalPdfUrl = feature.parseLocalFilePathAsOnlineUrl(signedPdfPath, configFilePath);

            // This will send an email to owner with files
            String message = feature.populateEmailBody(requestId, validatedDocument.getMetaData().getDocumentType().toUpperCase(), finalJsonUrl, finalPdfUrl);
            feature.sendEmail(feature.getEmailByRequestId(storeFilePath, requestId), "IDStack Document Extraction", message);

            signedResponse.setJson(validatedDocument);
            signedResponse.setPdf(feature.parseLocalFilePathAsOnlineUrl(signedPdfPath, configFilePath));

            // This will add the request id into request configuration list
            feature.saveRequestConfiguration(configFilePath, requestId);

            return new Gson().toJson(signedResponse);

        } catch (IOException | CMSException | CloneNotSupportedException | OperatorCreationException | GeneralSecurityException | DocumentException e) {
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