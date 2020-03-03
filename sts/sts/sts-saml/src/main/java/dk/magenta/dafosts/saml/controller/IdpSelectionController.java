package dk.magenta.dafosts.saml.controller;

import dk.magenta.dafosts.saml.metadata.DafoCachingMetadataManager;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.saml.SAMLConstants;
import org.springframework.security.saml.SAMLDiscovery;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;

import static dk.magenta.dafosts.saml.controller.SSOProxyController.SSO_PRESELECTED_IDP;

@Controller
@RequestMapping("/idpselection")
@Slf4j
public class IdpSelectionController {

    @Autowired
    DafoCachingMetadataManager dafoCachingMetadataManager;

    @RequestMapping
    public ModelAndView idpSelection(
            HttpServletRequest request, HttpSession httpSession
    ) throws MetadataProviderException {

        if (comesFromDiscoveryFilter(request)) {
            ModelAndView idpSelection = new ModelAndView("idpselection");
            idpSelection.addObject(SAMLDiscovery.RETURN_URL, request.getAttribute(SAMLDiscovery.RETURN_URL));
            idpSelection.addObject(SAMLDiscovery.RETURN_PARAM, request.getAttribute(SAMLDiscovery.RETURN_PARAM));
            idpSelection.addObject("idpNameAliasMap", dafoCachingMetadataManager.getIdpProviderMap());
            String preSelected = (String)httpSession.getAttribute(SSO_PRESELECTED_IDP);
            if(preSelected != null) {
                httpSession.removeAttribute(SSO_PRESELECTED_IDP);
                idpSelection.addObject("preselected", preSelected);
            }
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
