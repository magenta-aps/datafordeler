package dk.magenta.dafosts.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.saml.SAMLConstants;
import org.springframework.security.saml.SAMLDiscovery;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@Controller
@RequestMapping("/idpselection")
@Slf4j
public class IdpSelectionController {

    @RequestMapping
    public ModelAndView idpSelection(HttpServletRequest request) {

        if (comesFromDiscoveryFilter(request)) {
            ModelAndView idpSelection = new ModelAndView("idpselection");
            idpSelection.addObject(SAMLDiscovery.RETURN_URL, request.getAttribute(SAMLDiscovery.RETURN_URL));
            idpSelection.addObject(SAMLDiscovery.RETURN_PARAM, request.getAttribute(SAMLDiscovery.RETURN_PARAM));
            idpSelection.addObject(
                    "idpNameAliasMap",
                    Collections.singletonMap(
                            "Datafordeler-IdP", "Dafo IdP"
                    )
            );
            return idpSelection;
        }
        throw new AuthenticationServiceException("SP Discovery flow not detected");
    }

    private boolean comesFromDiscoveryFilter(HttpServletRequest request) {
        return request.getAttribute(SAMLConstants.LOCAL_ENTITY_ID) != null &&
                request.getAttribute(SAMLDiscovery.RETURN_URL) != null &&
                request.getAttribute(SAMLDiscovery.RETURN_PARAM) != null;
    }
}
