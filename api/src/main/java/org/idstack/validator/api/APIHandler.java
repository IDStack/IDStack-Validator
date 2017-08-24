package org.idstack.validator.api;

import org.idstack.validator.feature.Constant;
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

    @RequestMapping(value = "/{version}/validator/sign", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public boolean signDocument(@PathVariable("version") String version, @RequestBody String json) {
        return true;
    }

    @RequestMapping(value = "/{version}/save_configurations", method = RequestMethod.POST)
    @ResponseBody
    public boolean saveConfigurationFile(@PathVariable("version") String version) {
        return router.saveConfigurationFile();
    }

    @RequestMapping(value = "/{version}/load_configurations", method = RequestMethod.POST)
    @ResponseBody
    public String loadConfigurationFile(@PathVariable("version") String version) {
        return router.loadConfigurationFile();
    }

    @RequestMapping(value = "/{version}/save_pub_certificate", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public boolean savePublicCertificate(@PathVariable("version") String version, @RequestParam(value = "file", required = true) final MultipartFile certificate) {
        return router.saveCertificate(Constant.GlobalAttribute.PUB_CERTIFICATE, certificate);
    }

    @RequestMapping(value = "/{version}/save_pvt_certificate", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public boolean savePrivateCertificate(@PathVariable("version") String version, @RequestParam(value = "file", required = true) final MultipartFile certificate) {
        return router.saveCertificate(Constant.GlobalAttribute.PVT_CERTIFICATE, certificate);
    }
}