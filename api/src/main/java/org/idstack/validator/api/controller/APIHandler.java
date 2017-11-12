package org.idstack.validator.api.controller;

import com.google.gson.Gson;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Chanaka Lakmal
 * @date 24/4/2017
 * @since 1.0
 */

@RestController
public class APIHandler {

    @Autowired
    private Router router;

    @Autowired
    private FeatureImpl feature;

    @Value(value = "classpath:" + Constant.Configuration.SYSTEM_PROPERTIES_FILE_NAME)
    private Resource resource;

    private String apiKey;
    private String configFilePath;
    private String pvtCertFilePath;
    private String pvtCertType;
    private String pvtCertPasswordType;
    private String pubCertFilePath;
    private String pubCertType;
    private String storeFilePath;
    private String tmpFilePath;
    private String pubFilePath;

    @PostConstruct
    void init() throws IOException {
        apiKey = feature.getProperty(resource.getInputStream(), Constant.Configuration.API_KEY);
        configFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.CONFIG_FILE_PATH);
        pvtCertFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.PVT_CERTIFICATE_FILE_PATH);
        pvtCertType = feature.getProperty(resource.getInputStream(), Constant.Configuration.PVT_CERTIFICATE_TYPE);
        pvtCertPasswordType = feature.getProperty(resource.getInputStream(), Constant.Configuration.PVT_CERTIFICATE_PASSWORD_TYPE);
        pubCertFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.PUB_CERTIFICATE_FILE_PATH);
        pubCertType = feature.getProperty(resource.getInputStream(), Constant.Configuration.PUB_CERTIFICATE_TYPE);
        storeFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.STORE_FILE_PATH);
        tmpFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.TEMP_FILE_PATH);
        pubFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.PUB_FILE_PATH);
    }

    @RequestMapping(value = {"/", "/{version}", "/{version}/{apikey}"})
    public void root(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.sendRedirect("http://docs.idstack.apiary.io/");
    }

    /**
     * Save the configurations received at the configured URL at idstack.properties file
     *
     * @param version api version
     * @param apikey  api key
     * @param type    type of configuration [basic, document, whitelist, blacklist]
     * @param json    json of configuration
     * @return status of saving
     */
    @RequestMapping(value = "/{version}/{apikey}/saveconfig/{type}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("type") String type, @RequestBody String json) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        switch (type) {
            case Constant.Configuration.BASIC_CONFIG:
                return feature.saveBasicConfiguration(configFilePath, json);
            case Constant.Configuration.DOCUMENT_CONFIG:
                return feature.saveDocumentConfiguration(configFilePath, json);
            case Constant.Configuration.WHITELIST_CONFIG:
                return feature.saveWhiteListConfiguration(configFilePath, json, tmpFilePath);
            case Constant.Configuration.BLACKLIST_CONFIG:
                return feature.saveBlackListConfiguration(configFilePath, json, tmpFilePath);
            default:
                return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PARAMETER));
        }
    }

    /**
     * Return the saved configurations for the given type
     *
     * @param version api version
     * @param apikey  api key
     * @param type    type of configuration [basic, document, whitelist, blacklist]
     * @return configuration as json
     */
    @RequestMapping(value = {"/{version}/{apikey}/getconfig/{type}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("type") String type) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return new Gson().toJson(feature.getConfiguration(configFilePath, router.getConfigFileName(type)));
    }

    /**
     * Save public certificate of the validator
     *
     * @param version     api version
     * @param apikey      api key
     * @param certificate public certificate file
     * @return status of saving
     */
    @RequestMapping(value = "/{version}/{apikey}/savepubcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String savePublicCertificate(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "cert") final MultipartFile certificate) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return feature.savePublicCertificate(certificate, configFilePath, pubCertFilePath, pubCertType);
    }

    /**
     * Save private certificate of the validator
     *
     * @param version     api version
     * @param apikey      api key
     * @param certificate private certificate
     * @param password    password of private certificate
     * @return status of saving
     */
    @RequestMapping(value = "/{version}/{apikey}/savepvtcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String savePrivateCertificate(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "cert") final MultipartFile certificate, @RequestParam(value = "password") String password) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        if (password.isEmpty())
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PARAMETER_NULL));
        return feature.savePrivateCertificate(certificate, password, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType);
    }

    /**
     * Sign the received json document + pdf document by the validator and return the signed documents
     *
     * @param version   api version
     * @param apikey    api key
     * @param requestId request id
     * @return signed json document
     * @throws IOException if file cannot be converted into bytes
     */
    @RequestMapping(value = "/{version}/{apikey}/sign/request", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String signDocumentManuallyByRequest(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "request_id") String requestId) throws IOException {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        if (requestId.isEmpty())
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PARAMETER_NULL));
        String json = feature.getDocumentByRequestId(storeFilePath, requestId);
        return router.signDocumentManually(feature, json, Optional.of(requestId), configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType, storeFilePath, tmpFilePath, pubFilePath).replaceAll(pubFilePath, File.separator);
    }

    /**
     * Sign the received json document + pdf document by the validator and return the signed documents
     *
     * @param version api version
     * @param apikey  api key
     * @param json    json document
     * @return signed json document
     * @throws IOException if file cannot be converted into bytes
     */
    @RequestMapping(value = "/{version}/{apikey}/sign", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String signDocumentManually(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "json") String json) throws IOException {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        if (json.isEmpty())
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PARAMETER_NULL));
        return router.signDocumentManually(feature, json, Optional.empty(), configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType, storeFilePath, tmpFilePath, pubFilePath).replaceAll(pubFilePath, File.separator);
    }

    /**
     * Get the stored documents in the configured store path by request id
     *
     * @param version api version
     * @param apikey  api key
     * @param jsonUrl json url
     * @return document
     */
    @RequestMapping(value = "/{version}/{apikey}/getdoc/url", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getStoredDocument(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "json_url") String jsonUrl) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        if (jsonUrl.isEmpty())
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PARAMETER_NULL));
        return feature.getDocumentByUrl(storeFilePath, pubFilePath, jsonUrl, tmpFilePath);
    }

    /**
     * Get the document types that protocol facilitates
     *
     * @param version api version
     * @param apikey  api key
     * @return document types
     */
    @RequestMapping(value = "/{version}/{apikey}/getdoctypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getDocumentTypeList(@PathVariable("version") String version, @PathVariable("apikey") String apikey) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return feature.getDocumentTypes();
    }

    /**
     * Get the stored documents in the configured store path
     *
     * @param version api version
     * @param apikey  api key
     * @return document list
     */
    @RequestMapping(value = "/{version}/{apikey}/getdocstore", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getStoredDocuments(@PathVariable("version") String version, @PathVariable("apikey") String apikey) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return feature.getDocumentStore(storeFilePath, configFilePath, false).replaceAll(pubFilePath, File.separator);
    }

    @RequestMapping(value = "/{version}/{apikey}/save", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveDocument(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "pdf") final MultipartFile pdf) throws IOException {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return router.saveDocument(feature, pdf, configFilePath, tmpFilePath);
    }

    @RequestMapping(value = "/{version}/{apikey}/cleardocstore", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String clearDocStore(@PathVariable("version") String version, @PathVariable("apikey") String apikey) throws IOException {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return feature.clearDocStore(configFilePath, storeFilePath);
    }

    //*************************************************** PUBLIC API ***************************************************

    /**
     * Automatically sign the received json document and return the signed json document
     *
     * @param version api version
     * @param json    json document
     * @param email   email of the sender
     * @return signed json + pdf documents
     * @throws IOException if file cannot be converted into bytes
     */
    @RequestMapping(value = "/{version}/sign", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String signDocumentAutomatically(@PathVariable("version") String version, @RequestParam(value = "json") String json, @RequestParam(value = "email") String email) throws IOException {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (json.isEmpty() || email.isEmpty())
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PARAMETER_NULL));
        return router.signDocumentAutomatically(feature, json, email, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType, storeFilePath, tmpFilePath, pubFilePath).replaceAll(pubFilePath, File.separator);
    }

    /**
     * Get public certificate of the validator
     *
     * @param version api version
     * @return URL of the public certificate
     */
    @RequestMapping(value = "/{version}/getpubcert", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String getPublicCertificate(@PathVariable("version") String version) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        return feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType).replaceAll(pubFilePath, File.separator);
    }
}