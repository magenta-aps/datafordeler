package dk.magenta.dafosts.saml.controller;

import com.github.ulisesbocchio.spring.boot.security.saml.annotation.SAMLUser;
import dk.magenta.dafosts.library.DafoTokenGenerator;
import dk.magenta.dafosts.library.DatabaseQueryManager;
import dk.magenta.dafosts.library.LogRequestWrapper;
import dk.magenta.dafosts.saml.stereotypes.DafoSAMLUser;
import dk.magenta.dafosts.saml.users.DafoSAMLUserDetails;
import org.opensaml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static dk.magenta.dafosts.saml.controller.SSOProxyController.SSO_RETURN_URL;
import static dk.magenta.dafosts.saml.controller.SSOProxyController.SSO_TOKEN_RETURN_PARAM;

@Controller
@RequestMapping("/by_saml_sso")
public class SamlGetTokenController {
    private static final Logger logger = LoggerFactory.getLogger(SamlGetTokenController.class);

    @Autowired
    DafoTokenGenerator dafoTokenGenerator;

    /**
     * Generates a DAFO SAML2 token from the bootstrap token and passes it on to the requested service
     * @param user - The user identified by the SAML2 authentification
     * @return A ModelAndView for displaying the generated token
     * @throws Exception
     */
    @RequestMapping("/get_token_for_service")
    public ModelAndView get_token_for_service(
            @DafoSAMLUser DafoSAMLUserDetails user, HttpServletRequest request, HttpSession httpSession
    ) throws Exception {
        LogRequestWrapper logRequestWrapper = new LogRequestWrapper(logger, request, user.getUsername());

        ModelAndView getTokenView = new ModelAndView("by_saml_sso/get_token");

        Assertion assertion = dafoTokenGenerator.buildAssertion(user, request);
        logRequestWrapper.logIssuedToken(assertion);
        dafoTokenGenerator.signAssertion(assertion);

        getTokenView.addObject("userId", user.getUsername());
        getTokenView.addObject(
                "compressed_and_encoded_token",
                dafoTokenGenerator.saveGeneratedToken(assertion)
        );
        // If session contains info from sso_proxy landing page, add them to the model.
        String returnURL = (String)httpSession.getAttribute(SSO_RETURN_URL);
        if(returnURL != null) {
            httpSession.removeAttribute(SSO_RETURN_URL);
            getTokenView.addObject("returnURL", returnURL);
            String returnParam = (String)httpSession.getAttribute(SSO_TOKEN_RETURN_PARAM);
            if(returnParam == null) {
                httpSession.removeAttribute(SSO_TOKEN_RETURN_PARAM);
                returnParam = "token";
            }
            getTokenView.addObject("returnParam", returnParam);
        }

        return getTokenView;
    }


    @RequestMapping("/get_token")
    @ResponseBody
    public ResponseEntity<String> get_token(
            @DafoSAMLUser DafoSAMLUserDetails user, HttpServletRequest request
    ) throws Exception {
        LogRequestWrapper logRequestWrapper = new LogRequestWrapper(logger, request, user.getUsername());

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);

        Assertion assertion = dafoTokenGenerator.buildAssertion(user, request);
        logRequestWrapper.logIssuedToken(assertion);
        dafoTokenGenerator.signAssertion(assertion);

        return new ResponseEntity<String>(
                dafoTokenGenerator.saveGeneratedToken(assertion),
                httpHeaders,
                HttpStatus.OK
        );
    }
}
