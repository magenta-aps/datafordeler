package dk.magenta.dafosts.saml.controller;

import dk.magenta.dafosts.saml.metadata.DafoCachingMetadataManager;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by row on 07-09-2017.
 */
@Controller
public class SSOProxyController {
    public final static String SSO_RETURN_URL = "ssoproxy_return_url";
    public final static String SSO_PRESELECTED_IDP = "ssoproxy_idp";
    public final static String SSO_TOKEN_RETURN_PARAM = "ssoproxy_returnparam";

    @Autowired
    DafoCachingMetadataManager metadataManager;

    /**
     * Proxy that will store return URL, preselected IdP and optional token return param in the session
     * and redirect to the get_token_for_service URL.
     * @param returnURL The URL of the service the resulting token should be sent to.
     * @param preselectedIdP An optional alias of a preselected IdP.
     * @param tokenReturnParam The HTTP parameter that will be used to send the token back to the specified URL.
     * @param httpSession The current HttpSesssion
     * @return "redirect:/by_saml_sso/get_token_for_service"
     */
    @RequestMapping("/sso_proxy")
    public String sso_proxy(
            @RequestParam(name="dafo_ssoproxy_url") String returnURL,
            @RequestParam(name="dafo_ssoproxy_idp", required = false) String preselectedIdP,
            @RequestParam(
                    name="dafo_ssoproxy_returnparam", required = false, defaultValue = "token"
            ) String tokenReturnParam,
            HttpSession httpSession
            ) throws MetadataProviderException {
        httpSession.setAttribute(SSO_RETURN_URL, returnURL);
        if(preselectedIdP != null) {
            Map<String, String> idpProviderMap = metadataManager.getIdpProviderMap();
            if(idpProviderMap.containsKey(preselectedIdP)) {
                httpSession.setAttribute(SSO_PRESELECTED_IDP, preselectedIdP);
            }
        }
        httpSession.setAttribute(SSO_TOKEN_RETURN_PARAM, tokenReturnParam);


        return "redirect:/by_saml_sso/get_token_for_service";
    }

}
