package dk.magenta.dafosts.saml.controller;

import dk.magenta.dafosts.DafoTokenGenerator;
import dk.magenta.dafosts.DatabaseQueryManager;
import dk.magenta.dafosts.saml.users.DafoAssertionVerifier;
import dk.magenta.dafosts.users.DafoPasswordUserDetails;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml2.core.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

/**
 * Controller that allow users to passively get tokens from the STS.
 */
@Controller
public class PassiveGetTokenController {
    @Autowired
    DatabaseQueryManager databaseQueryManager;
    @Autowired
    DafoTokenGenerator dafoTokenGenerator;


    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public class InvalidCredentialsException extends Exception {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public class NoAccessException extends Exception {
        public NoAccessException(String message) {
            super(message);
        }
    }

    /**
     * Controller method that dispatches the request to the different getTokenBY* methods depending on which
     * request parameters were specified
     * @param username - optional username used for username/password login
     * @param password - optional password used for username/password login
     * @param bootstrap_token - optional bootstrap token from a trusted IdP
     * @return
     */
    @RequestMapping("/get_token_passive")
    @ResponseBody
    public ResponseEntity<String>  getTokenPassive(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String bootstrap_token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        if(StringUtils.isNotBlank(bootstrap_token)) {
            return getTokenByBootstrapToken(bootstrap_token, request, response);
        } else if(StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            return getTokenByUsernamePassword(username, password);
        } else {
            throw new IllegalArgumentException("No valid credentials given for passive STS");
        }
    }

    /**
     * Checks the given username and password and issues a token if they are valid
     * @param username
     * @param password
     * @return
     */
    public ResponseEntity<String> getTokenByUsernamePassword(String username, String password) throws Exception {
        DafoPasswordUserDetails user = databaseQueryManager.getDafoPasswordUserByUsername(username);

        if(user == null || !user.checkPassword(password)) {
            throw new InvalidCredentialsException("Failed to authenticate user");
        }

        if(!user.isActive()) {
            throw new NoAccessException("The specified user is not active");
        }

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(
                dafoTokenGenerator.deflateAndEncode(dafoTokenGenerator.getTokenXml(user)),
                httpHeaders,
                HttpStatus.OK
        );
    }

    @Autowired
    DafoAssertionVerifier dafoAssertionVerifier;

    public ResponseEntity<String> getTokenByBootstrapToken(
            String bootstrap_token,
            HttpServletRequest request,
            HttpServletResponse response)
            throws InvalidCredentialsException, NoAccessException, Exception {

        Assertion assertion = dafoAssertionVerifier.verifyAssertion(bootstrap_token, request, response);

        if(assertion == null) {
            throw new InvalidCredentialsException("Failed to authenticate user");
        }

        String username = assertion.getSubject().getNameID().getValue();

        // WSO2 will provide a tenant prefix before the actual username, which has to be removed
        if(username.indexOf('/') >= 0 && username.indexOf('@') >= 0 &&
                username.indexOf('/') < username.indexOf('@')) {
            username = username.substring(username.indexOf('/') + 1);
        }

        DafoPasswordUserDetails user = databaseQueryManager.getDafoPasswordUserByUsername(username);
        if(user == null) {
            throw new InvalidCredentialsException("User identified by bootstrap token not was not found");
        }

        if(!user.isActive()) {
            throw new NoAccessException("The specified user is not active");
        }

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(
                dafoTokenGenerator.deflateAndEncode(dafoTokenGenerator.getTokenXml(user)),
                httpHeaders,
                HttpStatus.OK
        );
    }
}
