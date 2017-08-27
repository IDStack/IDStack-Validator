package org.idstack.validator.api;

import org.idstack.validator.feature.Constant;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author Chanaka Lakmal
 * @date 24/4/2017
 * @since 1.0
 */

@RestController
public class APIHandler {

    Router router = new Router();

    @RequestMapping("/")
    public void root(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.sendRedirect("http://idstack.one/validator");
    }

    @RequestMapping(value = "/{version}/validator/sign", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String signDocument(@PathVariable("version") String version, @RequestBody String json) {
        return router.signDocument(json);
    }

    @RequestMapping(value = "/{version}/saveconfig/basic", method = RequestMethod.POST)
    @ResponseBody
    public boolean saveBasicConfiguration(@PathVariable("version") String version, @RequestHeader("Organization") String org, @RequestHeader("Email") String email) {
        return router.saveBasicConfiguration(org, email);
    }

    @RequestMapping(value = "/{version}/saveconfig/document", method = RequestMethod.POST)
    @ResponseBody
    public boolean saveDocumentConfiguration(@PathVariable("version") String version, @RequestHeader("Document") Map<String, String> configurations) {
        return router.saveDocumentConfiguration(configurations);
    }

    @RequestMapping(value = "/{version}/saveconfig/whitelist", method = RequestMethod.POST)
    @ResponseBody
    public boolean saveWhiteListConfiguration(@PathVariable("version") String version, @RequestHeader("WhiteList") Map<String, String> configurations) {
        return router.saveWhiteListConfiguration(configurations);
    }

    @RequestMapping(value = "/{version}/saveconfig/blacklist", method = RequestMethod.POST)
    @ResponseBody
    public boolean saveBlackListConfiguration(@PathVariable("version") String version, @RequestHeader("BlackList") Map<String, String> configurations) {
        return router.saveBlackListConfiguration(configurations);
    }

    @RequestMapping(value = "/{version}/getconfig/{type}/{property}", method = RequestMethod.GET)
    @ResponseBody
    public Object getConfigurationFile(@PathVariable("version") String version, @PathVariable("type") String type, @PathVariable("property") String property) {
        return router.getConfiguration(type, property);
    }

    @RequestMapping(value = "/{version}/savepubcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public boolean savePublicCertificate(@PathVariable("version") String version, @RequestParam(value = "cert") final MultipartFile certificate) {
        return router.saveCertificate(Constant.GlobalAttribute.PUB_CERTIFICATE, certificate, null);
    }

    @RequestMapping(value = "/{version}/getpubcert/{uuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public FileSystemResource getPublicCertificate(@PathVariable("version") String version, @PathVariable("uuid") String uuid) {
        return router.getPublicCertificate(uuid);
    }

    @RequestMapping(value = "/{version}/savepvtcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public boolean savePrivateCertificate(@PathVariable("version") String version, @RequestParam(value = "cert") final MultipartFile certificate, @RequestHeader("Password") String password) {
        return router.saveCertificate(Constant.GlobalAttribute.PVT_CERTIFICATE, certificate, password);
    }
}