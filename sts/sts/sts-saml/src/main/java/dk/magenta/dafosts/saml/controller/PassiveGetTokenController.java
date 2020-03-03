package dk.magenta.dafosts.saml.controller;

import com.sun.jersey.core.util.Base64;
import dk.magenta.dafosts.library.DafoTokenGenerator;
import dk.magenta.dafosts.library.DatabaseQueryManager;
import dk.magenta.dafosts.library.LogRequestWrapper;
import dk.magenta.dafosts.library.exceptions.InactiveAccessAccountException;
import dk.magenta.dafosts.library.users.DafoPasswordUserDetails;
import dk.magenta.dafosts.library.users.DafoUserData;
import dk.magenta.dafosts.saml.DafoStsBySamlConfiguration;
import dk.magenta.dafosts.saml.metadata.DafoCachingMetadataManager;
import dk.magenta.dafosts.saml.users.DafoAssertionVerifier;
import dk.magenta.dafosts.saml.users.DafoSAMLUserDetails;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller that allow users to passively get tokens from the STS.
 */
@Controller
public class PassiveGetTokenController {
    private static final Logger logger = LoggerFactory.getLogger(PassiveGetTokenController.class);

    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    @Autowired
    DafoStsBySamlConfiguration config;
    @Autowired
    DatabaseQueryManager databaseQueryManager;
    @Autowired
    DafoTokenGenerator dafoTokenGenerator;
    @Autowired
    DafoCachingMetadataManager dafoCachingMetadataManager;

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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public class BadAuthorizationRequest extends Exception {
        public BadAuthorizationRequest(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Controller method that dispatches the request to the different getTokenBY* methods depending on which
     * request parameters were specified
     *
     * @param username        - optional username used for username/password login
     * @param password        - optional password used for username/password login
     * @param bootstrap_token - optional bootstrap token from a trusted IdP
     * @return
     */
    @RequestMapping("/get_token_passive")
    @ResponseBody
    public ResponseEntity<String> getTokenPassive(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String bootstrap_token,
            @RequestParam(required = false) String basic_auth,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        LogRequestWrapper logWrapper = new LogRequestWrapper(logger, request);

        logWrapper.info("Incoming token request");

        // Check if user requested basic auth
        if (basic_auth != null && !basic_auth.toLowerCase().matches("^(|no|0|false)$")) {
            return getTokenByBasicAuth(request, logWrapper);
        } else if (StringUtils.isNotBlank(bootstrap_token)) {
            return getTokenByBootstrapToken(bootstrap_token, request, response, logWrapper);
        } else if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            return getTokenByUsernamePassword(username, password, logWrapper);
        } else {
            // Check for implicit usage of basic auth
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Basic ")) {
                return getTokenByBasicAuth(request, logWrapper);
            }
        }

        logWrapper.info("No known authentication method found in request data");
        throw new IllegalArgumentException("No valid credentials given for passive STS");
    }

    /**
     * Generates a token response from a DafoPasswordUserDetails object.
     * @param user The DafoPasswordUserDetails user to generate a token for
     * @param logWrapper LogRequestWrapper to use for logging the issued token
     * @return A ResponseEntity<String> containing the deflated and Base64-encoded token
     * @throws Exception
     */
    private ResponseEntity<String> generateTokenResponseFromUser(
            DafoUserData user, LogRequestWrapper logWrapper
    ) throws Exception {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);

        Assertion assertion = dafoTokenGenerator.buildAssertion(user, logWrapper.getRequest());
        logWrapper.logIssuedToken(assertion);
        dafoTokenGenerator.signAssertion(assertion);

        return new ResponseEntity<>(
                dafoTokenGenerator.saveGeneratedToken(assertion),
                httpHeaders,
                HttpStatus.OK
        );

    }

    /**
     * Tries to issue a token using HTTP Basic Authentification
     * @param request The incoming HttpServletRequest
     * @param logWrapper LogRequestWrapper used to log the login process
     * @return <ul>
     *     <li>A ResponseEntity<String> containing the deflated and Base64-encoded token if a user can be correctly
     *         identified by the credentials in the request.</li>
     *     <li>A HTTP 401 Unautorized response if no credentials or wrong credentials were supplied.</li>
     * </ul>
     * @throws <ul>
     *     <li>BadAuthorizationRequest if the Authorization header is formatted wrong,
     *     resulting in HTTP 400 BAD REQUEST</li>
     *     <li>NoAccessException if the identified user is not active, resulting in HTTP 403 FORBIDDEN</li>
     *     <li>Any Exception caused by problems when generating the user's token.</li>
     * </ul>
     */
    private ResponseEntity<String> getTokenByBasicAuth(HttpServletRequest request, LogRequestWrapper logWrapper)
            throws Exception {

        logWrapper.info("Performing Basic Authorization login");

        String username = "";
        String password = "";
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Basic ")) {
                String authInfo = authHeader.substring(6);
                String authOctets = Base64.base64Decode(authInfo);
                String authString = new String(authOctets.getBytes(), UTF8_CHARSET);
                String[] usernamePasswordPair = authString.split(":");
                username = usernamePasswordPair[0];
                password = usernamePasswordPair[1];
                logWrapper.setUserName(username);
            }
        } catch (Exception e) {
            logWrapper.warn(
                    "Error occured when trying to get token using Basic Authorisation: " + e.getMessage()
            );
            throw new BadAuthorizationRequest(
                    "Error occured during Basic Authorization: " + e.getMessage(), e
            );
        }

        DafoPasswordUserDetails user = databaseQueryManager.getDafoPasswordUserByUsername(username);
        if (user != null && user.checkPassword(password)) {
            if (!user.isActive()) {
                logWrapper.info("User is not active, denying access");
                throw new NoAccessException("The specified user is not active");
            } else {
                return generateTokenResponseFromUser(user, logWrapper);
            }
        } else {
            logWrapper.info("Basic Authentication failed");
        }


        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(
                "WWW-Authenticate",
                "Basic realm=\"DAFO Sts Token Service\", charset=\"UTF-8\""
        );
        return new ResponseEntity<>(
                "HTTP 401 Unauthorized",
                httpHeaders,
                HttpStatus.UNAUTHORIZED
        );
    }

    /**
     * Checks the given username and password and issues a token if they are valid
     *
     * @param username
     * @param password
     * @return
     */
    public ResponseEntity<String> getTokenByUsernamePassword(
            String username, String password, LogRequestWrapper logWrapper) throws Exception {

        logWrapper.setUserName(username);
        logWrapper.info("Performing password authentication");

        DafoPasswordUserDetails user = databaseQueryManager.getDafoPasswordUserByUsername(username);

        if (user == null || !user.checkPassword(password)) {
            logWrapper.info("Failed to authenticate user");
            throw new InvalidCredentialsException("Failed to authenticate user");
        }

        if (!user.isActive()) {
            logWrapper.info("User is not active, denying access");
            throw new NoAccessException("The specified user is not active");
        }

        return generateTokenResponseFromUser(user, logWrapper);
    }

    @Autowired
    DafoAssertionVerifier dafoAssertionVerifier;

    public ResponseEntity<String> getTokenByBootstrapToken(
            String bootstrap_token,
            HttpServletRequest request,
            HttpServletResponse response,
            LogRequestWrapper logRequestWrapper)
            throws Exception {

        logRequestWrapper.info("Trying authentication using bootstrap token");

        // Make sure we have up-to-date information about IdPs
        dafoCachingMetadataManager.updateDafoMetadataProviders();

        Assertion bootstrapAssertion = dafoAssertionVerifier.verifyAssertion(bootstrap_token, request, response);

        if (bootstrapAssertion == null) {
            logRequestWrapper.info("Invalid bootstrap token");
            throw new InvalidCredentialsException("Failed to authenticate user");
        }

        List<Attribute> bootstrapAttributes = new ArrayList<>();
        for(AttributeStatement stmt : bootstrapAssertion.getAttributeStatements()) {
            bootstrapAttributes.addAll(stmt.getAttributes());
        }

        // Create a SAML credential from the assertion
        SAMLCredential samlCredential = new SAMLCredential(
                bootstrapAssertion.getSubject().getNameID(),
                bootstrapAssertion,
                bootstrapAssertion.getIssuer().getValue(),
                bootstrapAttributes,
                request.getRequestURL().toString()
        );

        // Create a user from the bootstrapped credential
        DafoSAMLUserDetails user = new DafoSAMLUserDetails(samlCredential, dafoCachingMetadataManager);

        if (user == null) {
            logRequestWrapper.info("Unknown bootstrap user, denying access");
            throw new InvalidCredentialsException("User identified by bootstrap token was not found");
        }

        if(!databaseQueryManager.isAccessAccountActive(user.getAccessAccountId())) {
            throw new InactiveAccessAccountException(
                    "User '" + user.getUsername() + "' is not active"
            );
        }

        String username = samlCredential.getNameID().getValue();
        logRequestWrapper.setUserName(username);
        logRequestWrapper.info(
                "Got valid bootstrap token for " + username + " from " + samlCredential.getRemoteEntityID()
        );

        return generateTokenResponseFromUser(user, logRequestWrapper);
    }
}
