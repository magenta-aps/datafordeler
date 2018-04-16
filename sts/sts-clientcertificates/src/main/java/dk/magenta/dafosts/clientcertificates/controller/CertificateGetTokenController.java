package dk.magenta.dafosts.clientcertificates.controller;

import dk.magenta.dafosts.library.DafoTokenGenerator;
import dk.magenta.dafosts.clientcertificates.users.DafoCertificateUserDetailsImpl;
import dk.magenta.dafosts.library.DatabaseQueryManager;
import dk.magenta.dafosts.library.LogRequestWrapper;
import dk.magenta.dafosts.library.exceptions.InactiveAccessAccountException;
import org.opensaml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

import static dk.magenta.dafosts.library.DatabaseQueryManager.IDENTIFICATION_MODE_ON_BEHALF_OF;
import static dk.magenta.dafosts.library.DatabaseQueryManager.IDENTIFICATION_MODE_SINGLE_USER;

@Controller
public class CertificateGetTokenController {
    private static final Logger logger = LoggerFactory.getLogger(CertificateGetTokenController.class);

    @Autowired
    DatabaseQueryManager databaseQueryManager;

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
            @RequestParam(required = false) String on_behalf_of,
            HttpServletRequest request) throws Exception {
        LogRequestWrapper logWrapper = new LogRequestWrapper(logger, request, null);
        logWrapper.info("Incoming token request");
        PreAuthenticatedAuthenticationToken preAuthToken = (PreAuthenticatedAuthenticationToken)principal;
        DafoCertificateUserDetailsImpl userDetails = (DafoCertificateUserDetailsImpl)preAuthToken.getPrincipal();

        if(!databaseQueryManager.isAccessAccountActive(userDetails.getAccessAccountId())) {
            throw new InactiveAccessAccountException(
                    "User '" + userDetails.getUsername() + "' is not active"
            );
        }

        // We now know the user, add them to the log wrapper
        logWrapper.setUserName(principal.getName());

        int identificationMode = userDetails.getIdentificationMode();

        if(on_behalf_of == null || on_behalf_of.isEmpty()) {
            logWrapper.info("Requesting single-user token");
            if(identificationMode != IDENTIFICATION_MODE_SINGLE_USER) {
                logWrapper.info("Token request denied: Can only issue on-behalf-of tokens");
                throw new MissingOnBehalfOfException();
            }
            userDetails.setOnBehalfOf(null);
        } else {
            logWrapper.info("Requesting on-behalf-of token for " + on_behalf_of);
            if(identificationMode != IDENTIFICATION_MODE_ON_BEHALF_OF) {
                logWrapper.info("Token request denied: Can only issue single-user tokens");
                throw new OnBehalfOfNotAllowedException();
            }
            userDetails.setOnBehalfOf(on_behalf_of);
        }

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);


        Assertion assertion = dafoTokenGenerator.buildAssertion(userDetails, request);
        dafoTokenGenerator.signAssertion(assertion);

        logWrapper.logIssuedToken(assertion);
        dafoTokenGenerator.signAssertion(assertion);

        return new ResponseEntity<String>(
                dafoTokenGenerator.saveGeneratedToken(assertion),
                httpHeaders,
                HttpStatus.OK
        );
    }
}
