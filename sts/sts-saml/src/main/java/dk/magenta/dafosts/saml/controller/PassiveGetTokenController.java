package dk.magenta.dafosts.saml.controller;

import dk.magenta.dafosts.library.DafoTokenGenerator;
import dk.magenta.dafosts.library.DatabaseQueryManager;
import dk.magenta.dafosts.library.LogRequestWrapper;
import dk.magenta.dafosts.saml.users.DafoAssertionVerifier;
import dk.magenta.dafosts.library.users.DafoPasswordUserDetails;
import org.apache.commons.lang.StringUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller that allow users to passively get tokens from the STS.
 */
@Controller
public class PassiveGetTokenController {
    private static final Logger logger = LoggerFactory.getLogger(PassiveGetTokenController.class);

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
        LogRequestWrapper logWrapper = new LogRequestWrapper(logger, request);

        logWrapper.info("Incoming token request with querystring: [" + request.getQueryString() + "]");

        if(StringUtils.isNotBlank(bootstrap_token)) {
            return getTokenByBootstrapToken(bootstrap_token, request, response, logWrapper);
        } else if(StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            return getTokenByUsernamePassword(username, password, logWrapper);
        } else {
            logWrapper.info("No known authentication method found in request data");
            throw new IllegalArgumentException("No valid credentials given for passive STS");
        }
    }

    /**
     * Checks the given username and password and issues a token if they are valid
     * @param username
     * @param password
     * @return
     */
    public ResponseEntity<String> getTokenByUsernamePassword(
            String username, String password, LogRequestWrapper logWrapper) throws Exception {

        logWrapper.setUserName(username);
        logWrapper.info("Performing password authentication");

        DafoPasswordUserDetails user = databaseQueryManager.getDafoPasswordUserByUsername(username);

        if(user == null || !user.checkPassword(password)) {
            logWrapper.info("Failed to authenticate user");
            throw new InvalidCredentialsException("Failed to authenticate user");
        }

        if(!user.isActive()) {
            logWrapper.info("User is not active, denying access");
            throw new NoAccessException("The specified user is not active");
        }

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);

        Assertion assertion = dafoTokenGenerator.buildAssertion(user);

        logWrapper.logIssuedToken(assertion);
        dafoTokenGenerator.signAssertion(assertion);

        return new ResponseEntity<>(
                dafoTokenGenerator.deflateAndEncode(dafoTokenGenerator.getTokenXml(assertion)),
                httpHeaders,
                HttpStatus.OK
        );
    }

    @Autowired
    DafoAssertionVerifier dafoAssertionVerifier;

    public ResponseEntity<String> getTokenByBootstrapToken(
            String bootstrap_token,
            HttpServletRequest request,
            HttpServletResponse response,
            LogRequestWrapper logRequestWrapper)
            throws InvalidCredentialsException, NoAccessException, Exception {

        logRequestWrapper.info("Trying authentication using bootstrap token");
        Assertion assertion = dafoAssertionVerifier.verifyAssertion(bootstrap_token, request, response);

        if(assertion == null) {
            logRequestWrapper.info("Invalid bootstrap token");
            throw new InvalidCredentialsException("Failed to authenticate user");
        }

        String username = assertion.getSubject().getNameID().getValue();

        // WSO2 will provide a tenant prefix before the actual username, which has to be removed
        if(username.indexOf('/') >= 0 && username.indexOf('@') >= 0 &&
                username.indexOf('/') < username.indexOf('@')) {
            username = username.substring(username.indexOf('/') + 1);
        }

        logRequestWrapper.setUserName(username);
        logRequestWrapper.info("Got valid bootstrap token for " + username);

        DafoPasswordUserDetails user = databaseQueryManager.getDafoPasswordUserByUsername(username);
        if(user == null) {
            logRequestWrapper.info("Unknown bootstrap user, denying access");
            throw new InvalidCredentialsException("User identified by bootstrap token was not found");
        }

        if(!user.isActive()) {
            logRequestWrapper.info("User is not active, denying access");
            throw new NoAccessException("The specified user is not active");
        }

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);

        Assertion outgoingToken = dafoTokenGenerator.buildAssertion(user);

        logRequestWrapper.logIssuedToken(outgoingToken);
        dafoTokenGenerator.signAssertion(outgoingToken);

        return new ResponseEntity<>(
                dafoTokenGenerator.deflateAndEncode(dafoTokenGenerator.getTokenXml(outgoingToken)),
                httpHeaders,
                HttpStatus.OK
        );
    }
}
