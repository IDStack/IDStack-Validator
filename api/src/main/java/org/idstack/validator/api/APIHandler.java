package org.idstack.validator.api;

import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Chanaka Lakmal
 * @date 24/4/2017
 * @since 1.0
 */

@RestController
public class APIHandler {

    @Autowired
    Router router;

    @Autowired
    private FeatureImpl feature;

    private FileInputStream inputStream;
    private String apiKey;
    private String configFilePath;
    private String pvtCertFilePath;
    private String pvtCertType;
    private String pvtCertPasswordType;
    private String pubCertFilePath;
    private String pubCertType;
    private String storeFilePath;

    @PostConstruct
    void init() throws FileNotFoundException {
        inputStream = new FileInputStream(getClass().getClassLoader().getResource(Constant.Configuration.SYSTEM_PROPERTIES_FILE_NAME).getFile());
        apiKey = feature.getProperty(inputStream, Constant.Configuration.API_KEY);
        configFilePath = feature.getProperty(inputStream, Constant.Configuration.CONFIG_FILE_PATH);
        pvtCertFilePath = feature.getProperty(inputStream, Constant.Configuration.PVT_CERTIFICATE_FILE_PATH);
        pvtCertType = feature.getProperty(inputStream, Constant.Configuration.PVT_CERTIFICATE_TYPE);
        pvtCertPasswordType = feature.getProperty(inputStream, Constant.Configuration.PVT_CERTIFICATE_PASSWORD_TYPE);
        pubCertFilePath = feature.getProperty(inputStream, Constant.Configuration.PUB_CERTIFICATE_FILE_PATH);
        pubCertType = feature.getProperty(inputStream, Constant.Configuration.PUB_CERTIFICATE_TYPE);
        storeFilePath = feature.getProperty(inputStream, Constant.Configuration.STORE_FILE_PATH);
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
    @RequestMapping(value = "/{version}/{apikey}/saveconfig/{type}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("type") String type, @RequestBody String json) {
        if (!feature.validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!feature.validateRequest(apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        switch (type) {
            case Constant.Configuration.BASIC_CONFIG:
                return feature.saveBasicConfiguration(configFilePath, json);
            case Constant.Configuration.DOCUMENT_CONFIG:
                return feature.saveDocumentConfiguration(configFilePath, json);
            case Constant.Configuration.WHITELIST_CONFIG:
                return feature.saveWhiteListConfiguration(configFilePath, json);
            case Constant.Configuration.BLACKLIST_CONFIG:
                return feature.saveBlackListConfiguration(configFilePath, json);
            default:
                return Constant.Status.STATUS_ERROR_PARAMETER;
        }
    }

    /**
     * Returned the saved configurations. If property is mentioned this returns only the mentioned property from the given type otherwise everything
     *
     * @param version  api version
     * @param apikey   api key
     * @param type     type of configuration [basic, document, whitelist, blacklist]
     * @param property property of configuration
     * @return configuration as json
     */
    @RequestMapping(value = {"/{version}/{apikey}/getconfig/{type}/{property}", "/{version}/{apikey}/getconfig/{type}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("type") String type, @PathVariable("property") Optional<String> property) {
        if (!feature.validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!feature.validateRequest(apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return feature.getConfigurationAsJson(configFilePath, router.getConfigFileName(type), property);
    }

    /**
     * Save public certificate of the validator
     *
     * @param version     api version
     * @param apikey      api key
     * @param certificate public certificate file
     * @return status of saving
     */
    @RequestMapping(value = "/{version}/{apikey}/savepubcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String savePublicCertificate(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "cert") final MultipartFile certificate) {
        if (!feature.validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!feature.validateRequest(apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return feature.savePublicCertificate(certificate, configFilePath, pubCertFilePath, pubCertType);
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
            return Constant.Status.STATUS_ERROR_VERSION;
        return feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType);
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
    @RequestMapping(value = "/{version}/{apikey}/savepvtcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String savePrivateCertificate(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "cert") final MultipartFile certificate, @RequestParam(value = "password") String password) {
        if (!feature.validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!feature.validateRequest(apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return feature.savePrivateCertificate(certificate, password, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType);
    }

    //Access by the owner

    /**
     * Automatically sign the received json document + pdf document and return the signed documents
     *
     * @param version api version
     * @param json    json document
     * @param pdf     pdf document
     * @param email   email of the sender
     * @return signed json + pdf documents
     * @throws IOException if file cannot be converted into bytes
     */
    //TODO : return both signed MR + signed PDF
    @RequestMapping(value = "/{version}/sign", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String signDocumentAutomatically(@PathVariable("version") String version, @RequestParam(value = "json") String json, @RequestParam(value = "pdf") final MultipartFile pdf, @RequestParam(value = "email") String email) throws IOException {
        if (!feature.validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        return router.signDocumentAutomatically(feature, json, pdf, email, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType, storeFilePath);
    }

    /**
     * Sign the received json document + pdf document by the validator and return the signed documents
     *
     * @param version api version
     * @param json    json document
     * @param pdf     pdf document
     * @return signed json + pdf documents
     * @throws IOException if file cannot be converted into bytes
     */
    //TODO : return both signed MR + signed PDF
    @RequestMapping(value = "/{version}/{apikey}/sign", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String signDocumentManually(@PathVariable("version") String version, @RequestParam(value = "json") String json, @RequestParam(value = "pdf") final MultipartFile pdf) throws IOException {
        if (!feature.validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        return router.signDocumentManually(feature, json, pdf, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType);
    }

    /**
     * Get the document types that protocol facilitates
     *
     * @param version api version
     * @param apikey  api key
     * @return document types
     */
    //TODO : pass this to lambda function
    @RequestMapping(value = "/{version}/{apikey}/getdoctypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getDocumentTypeList(@PathVariable("version") String version, @PathVariable("apikey") String apikey) {
        if (!feature.validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!feature.validateRequest(apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return feature.getDocumentTypes();
    }

    /**
     * Get the stored documents in the configured store path
     *
     * @param version api version
     * @param apikey  api key
     * @return document list
     */
    @RequestMapping(value = "/{version}/{apikey}/getdocstore", method = RequestMethod.GET)
    @ResponseBody
    public String getStoredDocuments(@PathVariable("version") String version, @PathVariable("apikey") String apikey) {
        if (!feature.validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!feature.validateRequest(apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return feature.getDocumentStore(storeFilePath);
    }
}