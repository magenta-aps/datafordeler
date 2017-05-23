package dk.magenta.dafosts.clientcertificates.controller;

import dk.magenta.dafosts.library.DafoTokenGenerator;
import dk.magenta.dafosts.clientcertificates.users.DafoCertificateUserDetailsImpl;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.security.Principal;

import static dk.magenta.dafosts.library.DatabaseQueryManager.IDENTIFICATION_MODE_ON_BEHALF_OF;
import static dk.magenta.dafosts.library.DatabaseQueryManager.IDENTIFICATION_MODE_SINGLE_USER;

@Controller
public class CertificateGetTokenController {

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public class MissingOnBehalfOfException extends Exception {
        public MissingOnBehalfOfException() {
            super("You must specify the user you wish to issue a token on behalf of");
        }
    }
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public class OnBehalfOfNotAllowedException extends Exception {
        public OnBehalfOfNotAllowedException() {
            super("You are not allowed to issue tokens on behalf of other users");
        }
    }


    @Autowired
    DafoTokenGenerator dafoTokenGenerator;

    @RequestMapping("/get_token")
    @ResponseBody
    @PreAuthorize("hasAuthority('ROLE_CERTIFICATE_USER')")
    public ResponseEntity<String> get_token(
            Principal principal,
            @RequestParam(required = false) String on_behalf_of) throws Exception {
        PreAuthenticatedAuthenticationToken preAuthToken = (PreAuthenticatedAuthenticationToken)principal;
        DafoCertificateUserDetailsImpl userDetails = (DafoCertificateUserDetailsImpl)preAuthToken.getPrincipal();

        int identificationMode = userDetails.getIdentificationMode();

        if(on_behalf_of == null || on_behalf_of.isEmpty()) {
            if(identificationMode != IDENTIFICATION_MODE_SINGLE_USER) {
                throw new MissingOnBehalfOfException();
            }
            userDetails.setOnBehalfOf(null);
        } else {
            if(identificationMode != IDENTIFICATION_MODE_ON_BEHALF_OF) {
                throw new OnBehalfOfNotAllowedException();
            }
            userDetails.setOnBehalfOf(on_behalf_of);
        }

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<String>(
                dafoTokenGenerator.deflateAndEncode(dafoTokenGenerator.getTokenXml(userDetails)),
                httpHeaders,
                HttpStatus.OK
        );
    }
}
