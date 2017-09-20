package org.idstack.validator.api;

import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
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

    @RequestMapping(value = {"/", "/{version}", "/{version}/{apikey}"})
    public void root(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.sendRedirect("http://docs.idstack.apiary.io/");
    }

    @RequestMapping(value = "/{version}/{apikey}/saveconfig/{type}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("type") String type, @RequestBody String json) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        switch (type) {
            case Constant.Configuration.BASIC_CONFIG:
                return FeatureImpl.getFactory().saveBasicConfiguration(router.configFilePath, json);
            case Constant.Configuration.DOCUMENT_CONFIG:
                return FeatureImpl.getFactory().saveDocumentConfiguration(router.configFilePath, json);
            case Constant.Configuration.WHITELIST_CONFIG:
                return FeatureImpl.getFactory().saveWhiteListConfiguration(router.configFilePath, json);
            case Constant.Configuration.BLACKLIST_CONFIG:
                return FeatureImpl.getFactory().saveBlackListConfiguration(router.configFilePath, json);
            default:
                return Constant.Status.STATUS_ERROR_PARAMETER;
        }
    }

    @RequestMapping(value = {"/{version}/{apikey}/getconfig/{type}/{property}", "/{version}/{apikey}/getconfig/{type}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getConfigurationFile(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("type") String type, @PathVariable("property") Optional<String> property) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return FeatureImpl.getFactory().getConfigurationAsJson(router.configFilePath, router.getConfigFileName(type), property);
    }

    @RequestMapping(value = "/{version}/{apikey}/savepubcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String savePublicCertificate(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "cert") final MultipartFile certificate) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return FeatureImpl.getFactory().savePublicCertificate(certificate, router.configFilePath, router.pubCertFilePath, router.pubCertType);
    }

    @RequestMapping(value = "/{version}/getpubcert", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String getPublicCertificate(@PathVariable("version") String version) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        return FeatureImpl.getFactory().getPublicCertificateURL(router.configFilePath, router.pubCertFilePath, router.pubCertType);
    }

    @RequestMapping(value = "/{version}/{apikey}/savepvtcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String savePrivateCertificate(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "cert") final MultipartFile certificate, @RequestParam(value = "password") String password) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return FeatureImpl.getFactory().savePrivateCertificate(certificate, password, router.configFilePath, router.pvtCertFilePath, router.pvtCertType, router.pvtCertPasswordType);
    }

    //Access by the owner
    //TODO : return both signed MR + signed PDF
    @RequestMapping(value = "/{version}/sign", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String signDocumentAutomatically(@PathVariable("version") String version, @RequestParam(value = "json") String json, @RequestParam(value = "pdf") final MultipartFile pdf, @RequestParam(value = "email") String email) throws IOException {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        return router.signDocumentAutomatically(json, pdf, email);
    }

    //TODO : return both signed MR + signed PDF
    @RequestMapping(value = "/{version}/{apikey}/sign", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String signDocumentManually(@PathVariable("version") String version, @RequestParam(value = "json") String json, @RequestParam(value = "pdf") final MultipartFile pdf) throws IOException {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        return router.signDocumentManually(json, pdf);
    }

    //TODO : pass this to lambda function
    @RequestMapping(value = "/{version}/{apikey}/getdoctypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getDocumentTypeList(@PathVariable("version") String version, @PathVariable("apikey") String apikey) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return FeatureImpl.getFactory().getDocumentTypes();
    }

    @RequestMapping(value = "/{version}/{apikey}/getdocstore", method = RequestMethod.GET)
    @ResponseBody
    public String getStoredDocuments(@PathVariable("version") String version) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        return FeatureImpl.getFactory().getDocumentStore(router.storeFilePath);
    }
}