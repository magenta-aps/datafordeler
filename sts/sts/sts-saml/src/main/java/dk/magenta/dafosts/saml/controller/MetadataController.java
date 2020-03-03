package dk.magenta.dafosts.saml.controller;

import dk.magenta.dafosts.library.DafoTokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller that allows clients to fetch metadata for the STS.
 */
@Controller
public class MetadataController {
    @Autowired
    private DafoTokenGenerator dafoTokenGenerator;

    @RequestMapping(value = "/get_metadata", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String getTokenPassive() throws Exception {
        return dafoTokenGenerator.getMetadataXmlString();
    }
}
