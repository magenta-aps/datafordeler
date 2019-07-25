package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.core.util.MockInternalServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensaml.saml2.core.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages DAFO users that are created from incoming SAML tokens.
 * This default implementation is not database backed and will not look up additional details
 * from a users associanted UserProfiles.
 */
public class DafoUserManager {

    Logger logger = LogManager.getLogger(DafoUserManager.class.getCanonicalName());

    @Autowired
    private TokenParser tokenParser;
    @Autowired
    private TokenVerifier tokenVerifier;

    @Value("${pitu.sdn.whitelist:}")
    private String[] pituSDNWhitelistCsep;

    private HashSet<String> pituSDNWhitelist = new HashSet<>();

    @Value("${pitu.idn.whitelist:}")
    private String[] pituIDNWhitelistCsep;

    private HashSet<String> pituIDNWhitelist = new HashSet<>();

    @Value("${ip.whitelist:}")
    private String[] ipWhitelistCsep;

    private HashSet<String> ipWhitelist = new HashSet<>();

    public Set<String> getIpWhitelist() {
        return this.ipWhitelist;
    }

    @PostConstruct
    public void init() {
        this.pituSDNWhitelist.addAll(Arrays.asList(this.pituSDNWhitelistCsep));
        this.pituIDNWhitelist.addAll(Arrays.asList(this.pituIDNWhitelistCsep));
        this.ipWhitelist.addAll(Arrays.asList(this.ipWhitelistCsep));
    }

    /**
     * Parses and verifies a string containing a deflated and base64 encoded SAML token.
     *
     * @param tokendata - A deflated and base64 encoded SAML token
     * @return A verified Assertion object
     * @throws InvalidTokenException
     */
    public Assertion parseAndVerifyToken(String tokendata) throws InvalidTokenException {
        Assertion samlAssertion = tokenParser.parseAssertion(tokendata);
        tokenVerifier.verifyAssertion(samlAssertion);
        return samlAssertion;
    }


    /**
     * Creates a DafoUserDetails object from an incoming request. If there is a SAML token on the
     * request that token will parsed and verified. If not an AnonymousDafoUserDetails object will
     * be returned.
     *
     * @param request - a HttpServletRequest
     * @return A DafoUserDetails object
     * @throws InvalidTokenException
     */
    public DafoUserDetails getUserFromRequest(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        return this.getUserFromRequest(request, false);
    }

    public DafoUserDetails getUserFromRequest(HttpServletRequest request, boolean samlOnly)
            throws InvalidTokenException, AccessDeniedException, InvalidCertificateException {
        if (request instanceof MockInternalServletRequest) {
            return ((MockInternalServletRequest) request).getUserDetails();
        }
        if (!this.getIpWhitelist().contains(request.getRemoteAddr())) {
            throw new AccessDeniedException("Client IP rejected");
        }
        // If an authorization header starting with "SAML " is provided, use it to create a
        // SAML token based user.
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.indexOf("SAML ") == 0) {
            LoggerHelper loggerHelper = new LoggerHelper(logger, request);
            loggerHelper.info("Authorizing with SAML token");

            SamlDafoUserDetails userDetails;
            try {
                userDetails = getSamlUserDetailsFromToken(authHeader.substring(5));
            } catch (InvalidTokenException e) {
                loggerHelper.info("Token verification failed: " + e.getMessage());
                throw (e);
            }

            loggerHelper.setUser(userDetails);
            loggerHelper.info(String.format(
                    "User authorized with SAML token %s issued at %s",
                    userDetails.getIdentity(),
                    userDetails.getIssueInstant()
            ));

            return userDetails;
        }

        if (!samlOnly) {
            // If an SSL_CLIENT_S_DN header is provided, create a clientcertificate-based user
            String sslClientSubjectDN = request.getHeader(PituDafoUserDetails.HEADER_SSL_CLIENT_SUBJECT_DN);
            String sslClientIssuerDN = request.getHeader(PituDafoUserDetails.HEADER_SSL_CLIENT_ISSUER_DN);
            if (sslClientSubjectDN != null && sslClientIssuerDN != null) {
                if (!this.pituSDNWhitelist.contains(sslClientSubjectDN)) {
                    throw new InvalidCertificateException(PituDafoUserDetails.HEADER_SSL_CLIENT_SUBJECT_DN + " \"" + sslClientSubjectDN + "\" is not whitelisted");
                }
                if (!this.pituIDNWhitelist.contains(sslClientIssuerDN)) {
                    throw new InvalidCertificateException(PituDafoUserDetails.HEADER_SSL_CLIENT_ISSUER_DN + " \"" + sslClientIssuerDN + "\" is not whitelisted");
                }
                return new PituDafoUserDetails(request);
            }
        }

        // Fall back to an anonymous user
        return this.getFallbackUser();
    }

    public DafoUserDetails getFallbackUser() {
        return new AnonymousDafoUserDetails();
    }

    public SamlDafoUserDetails getSamlUserDetailsFromToken(String tokenData)
            throws InvalidTokenException {
        Assertion samlAssertion = parseAndVerifyToken(tokenData);
        SamlDafoUserDetails samlDafoUserDetails = new SamlDafoUserDetails(samlAssertion);
        this.addUserProfilesToSamlUser(samlDafoUserDetails);

        return samlDafoUserDetails;
    }

    /**
     * Populates a SamlDafoUserDetails object with UserProfiles by translating the UserProfile
     * names gotten from the original SAML token to UserProfile objects.
     *
     * @param samlDafoUserDetails
     */
    public void addUserProfilesToSamlUser(SamlDafoUserDetails samlDafoUserDetails) {
        for (String profileName : samlDafoUserDetails.getAssertionUserProfileNames()) {
            samlDafoUserDetails.addUserProfile(new UserProfile(profileName));
        }
    }
}
