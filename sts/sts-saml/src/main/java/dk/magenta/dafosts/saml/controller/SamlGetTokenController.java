package dk.magenta.dafosts.saml.controller;

import com.github.ulisesbocchio.spring.boot.security.saml.annotation.SAMLUser;
import dk.magenta.dafosts.library.DafoTokenGenerator;
import dk.magenta.dafosts.saml.stereotypes.DafoSAMLUser;
import dk.magenta.dafosts.saml.users.DafoSAMLUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/by_saml_sso")
public class SamlGetTokenController {

    @Autowired
    DafoTokenGenerator dafoTokenGenerator;

    /**
     * Generates a DAFO SAML2 token from the bootstrap token and passes it on to the requested service
     * @param user - The user identified by the SAML2 authentification
     * @return A ModelAndView for displaying the generated token
     * @throws Exception
     */
    @RequestMapping("/get_token_for_service")
    public ModelAndView get_token_for_service(@SAMLUser DafoSAMLUserDetails user) throws Exception {

        ModelAndView getTokenView = new ModelAndView("by_saml_sso/get_token");
        String xmlString = dafoTokenGenerator.getTokenXml(user);
        getTokenView.addObject("userId", user.getUsername());
        getTokenView.addObject("compressed_and_encoded_token", dafoTokenGenerator.deflateAndEncode(xmlString));

        return getTokenView;
    }


    @RequestMapping("/get_token")
    @ResponseBody
    public ResponseEntity<String> get_token(@DafoSAMLUser DafoSAMLUserDetails user) throws Exception {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<String>(
                dafoTokenGenerator.deflateAndEncode(dafoTokenGenerator.getTokenXml(user)),
                httpHeaders,
                HttpStatus.OK
        );
    }
}
