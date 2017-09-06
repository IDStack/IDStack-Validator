package org.idstack.validator.api;

import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Chanaka Lakmal
 * @date 24/4/2017
 * @since 1.0
 */

@RestController
public class APIHandler {

    @Autowired
    Router router;

    @RequestMapping("/")
    public void root(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.sendRedirect("http://idstack.one/validator");
    }

    @RequestMapping(value = "/{version}/{apikey}/saveconfig/basic", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveBasicConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestBody String json) {
        if (!FeatureImpl.getFactory().validateRequest(version, router.apiKey, apikey))
            return Constant.Status.ERROR_REQUEST;
        return FeatureImpl.getFactory().saveBasicConfiguration(router.configFilePath, json);
    }

    @RequestMapping(value = "/{version}/{apikey}/saveconfig/document", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveDocumentConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestBody String json) {
        if (!FeatureImpl.getFactory().validateRequest(version, router.apiKey, apikey))
            return Constant.Status.ERROR_REQUEST;
        return FeatureImpl.getFactory().saveDocumentConfiguration(router.configFilePath, json);
    }

    @RequestMapping(value = "/{version}/{apikey}/saveconfig/whitelist", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveWhiteListConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestBody String json) {
        if (!FeatureImpl.getFactory().validateRequest(version, router.apiKey, apikey))
            return Constant.Status.ERROR_REQUEST;
        return FeatureImpl.getFactory().saveWhiteListConfiguration(router.configFilePath, json);
    }

    @RequestMapping(value = "/{version}/{apikey}/saveconfig/blacklist", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveBlackListConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestBody String json) {
        if (!FeatureImpl.getFactory().validateRequest(version, router.apiKey, apikey))
            return Constant.Status.ERROR_REQUEST;
        return FeatureImpl.getFactory().saveBlackListConfiguration(router.configFilePath, json);
    }

    @RequestMapping(value = "/{version}/getconfig/{type}/{property}", method = RequestMethod.GET)
    @ResponseBody
    public Object getConfigurationFile(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("type") String type, @PathVariable("property") String property) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.ERROR_REQUEST;
        return FeatureImpl.getFactory().getConfiguration(router.configFilePath, router.getConfigFileName(type), property);
    }

    @RequestMapping(value = "/{version}/{apikey}/savepubcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String savePublicCertificate(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "cert") final MultipartFile certificate) {
        if (!FeatureImpl.getFactory().validateRequest(version, router.apiKey, apikey))
            return Constant.Status.ERROR_REQUEST;
        return FeatureImpl.getFactory().savePublicCertificate(certificate, router.configFilePath, router.pubCertFilePath, router.pubCertType);
    }

    @RequestMapping(value = "/{version}/getpubcert", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public String getPublicCertificate(@PathVariable("version") String version) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.ERROR_REQUEST;
        return FeatureImpl.getFactory().getPublicCertificateURL(router.configFilePath, router.pubCertFilePath, router.pubCertType);
    }

    @RequestMapping(value = "/{version}/{apikey}/savepvtcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String savePrivateCertificate(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "cert") final MultipartFile certificate, @RequestParam(value = "password") String password) {
        if (!FeatureImpl.getFactory().validateRequest(version, router.apiKey, apikey))
            return Constant.Status.ERROR_REQUEST;
        return FeatureImpl.getFactory().savePrivateCertificate(certificate, password, router.configFilePath, router.pvtCertFilePath, router.pvtCertType, router.pvtCertPasswordType);
    }

    @RequestMapping(value = "/{version}/sign", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String signDocument(@PathVariable("version") String version, @RequestBody String json) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.ERROR_REQUEST;
        return router.signDocument(json);
    }
}