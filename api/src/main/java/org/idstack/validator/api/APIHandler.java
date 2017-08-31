package org.idstack.validator.api;

import org.idstack.feature.Constant;
import org.springframework.core.io.FileSystemResource;
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

    Router router = new Router();

    @RequestMapping("/")
    public void root(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.sendRedirect("http://idstack.one/validator");
    }

    @RequestMapping(value = "/{version}/sign", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String signDocument(@PathVariable("version") String version, @RequestBody String json) {
        return router.signDocument(json);
    }

    @RequestMapping(value = "/{version}/saveconfig/basic", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveBasicConfiguration(@PathVariable("version") String version, @RequestBody String json) {
        return router.saveBasicConfiguration(json);
    }

    @RequestMapping(value = "/{version}/saveconfig/document", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveDocumentConfiguration(@PathVariable("version") String version, @RequestBody String json) {
        return router.saveDocumentConfiguration(json);
    }

    @RequestMapping(value = "/{version}/saveconfig/whitelist", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveWhiteListConfiguration(@PathVariable("version") String version, @RequestBody String json) {
        return router.saveWhiteListConfiguration(json);
    }

    @RequestMapping(value = "/{version}/saveconfig/blacklist", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveBlackListConfiguration(@PathVariable("version") String version, @RequestBody String json) {
        return router.saveBlackListConfiguration(json);
    }

    @RequestMapping(value = "/{version}/getconfig/{type}/{property}", method = RequestMethod.GET)
    @ResponseBody
    public Object getConfigurationFile(@PathVariable("version") String version, @PathVariable("type") String type, @PathVariable("property") String property) {
        return router.getConfiguration(type, property);
    }

    @RequestMapping(value = "/{version}/savepubcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String savePublicCertificate(@PathVariable("version") String version, @RequestParam(value = "cert") final MultipartFile certificate) {
        return router.saveCertificate(Constant.GlobalAttribute.PUB_CERTIFICATE, certificate, null);
    }

    @RequestMapping(value = "/{version}/getpubcert/{uuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public FileSystemResource getPublicCertificate(@PathVariable("version") String version, @PathVariable("uuid") String uuid) {
        return router.getPublicCertificate(uuid);
    }

    @RequestMapping(value = "/{version}/savepvtcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String savePrivateCertificate(@PathVariable("version") String version, @RequestParam(value = "cert") final MultipartFile certificate, @RequestParam(value = "password") String password) {
        return router.saveCertificate(Constant.GlobalAttribute.PVT_CERTIFICATE, certificate, password);
    }
}