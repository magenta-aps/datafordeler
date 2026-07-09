package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PituDafoUserDetails extends DafoUserDetails {

    public static String HEADER_SSL_CLIENT_SUBJECT_DN = "ssl-client-s-dn";
    public static String HEADER_SSL_CLIENT_ISSUER_DN = "ssl-client-i-dn";
    public static String HEADER_SSL_SERVER_SUBJECT_DN_OU = "ssl-server-s-dn-ou";
    public static String HEADER_SSL_CLIENT_VERIFY = "ssl-client-verify";
    public static String HEADER_PITU_CLIENT = "uxp-client";

    public static String PARAMETER_XROAD_INSTANCE = "xRoadInstance";
    public static String PARAMETER_MEMBERCLASS = "memberClass";
    public static String PARAMETER_MEMBERCODE = "memberCode";
    public static String PARAMETER_SUBSYSTEMCODE = "subsystemCode";
    public static String PARAMETER_SERVICECODE = "serviceCode";
    public static String PARAMETER_SERVICEVERSION = "serviceVersion";

    public static final String HEADER_SSL_CLIENT_CERT_INFO = "x-forwarded-tls-client-cert-info";

    private final HashMap<String, UserProfile> userProfiles = new HashMap<>();
    private final HashMap<String, ArrayList<UserProfile>> systemRoles = new HashMap<>();

    String serverSubject;
    String verify;

    private final String nameQualifier;
    private String onBehalfOf;
    private final String clientSubject;

    private final String xRoadInstance;
    private final String memberClass;
    private final String memberCode;
    private final String subsystemCode;
    private final String serviceCode;
    private final String serviceVersion;
    private final String pituClient;

    public PituDafoUserDetails(HttpServletRequest request) throws InvalidCertificateException {
        this(extractHeaders(request), request.getParameterMap());
    }


    private PituDafoUserDetails(Map<String, String> headers, Map<String, String[]> parameters) throws InvalidCertificateException {
        super();

        String clientSubject = headers.get(HEADER_SSL_CLIENT_SUBJECT_DN);
        String nameQualifier = headers.get(HEADER_SSL_CLIENT_ISSUER_DN);
        this.serverSubject = headers.get(HEADER_SSL_SERVER_SUBJECT_DN_OU);
        this.verify = headers.get(HEADER_SSL_CLIENT_VERIFY);
        this.pituClient = headers.get(HEADER_PITU_CLIENT);

        String sslClientCertInfo = headers.get(HEADER_SSL_CLIENT_CERT_INFO);
        System.out.println("headers: "+headers);
        System.out.println("sslClientCertInfo: "+sslClientCertInfo);
        if (sslClientCertInfo != null) {
            Map<String, String> parsedSSLClientCertInfo = PituDafoUserDetails.parseSSLClientCertInfo(sslClientCertInfo);
            clientSubject = parsedSSLClientCertInfo.get("Subject");
            nameQualifier = parsedSSLClientCertInfo.get("Issuer");
            if (clientSubject != null && nameQualifier != null) {
                this.verify = "SUCCESS";
            }
        }
        System.out.println("clientSubject: "+clientSubject);

        this.clientSubject = clientSubject;
        this.nameQualifier = nameQualifier;
        System.out.println("this.clientSubject: "+this.clientSubject);

        Map<String, String> parameterMap = firstParameter(parameters);

        if (this.clientSubject == null || this.clientSubject.isEmpty()) {
            throw new InvalidCertificateException("Missing certificate header for subject");
        }
        if (!"SUCCESS".equals(this.verify)) {
            throw new InvalidCertificateException("Certificate validation failed. Client verification status was: " + this.verify);
        }

        this.xRoadInstance = parameterMap.get(PARAMETER_XROAD_INSTANCE);
        this.memberClass = parameterMap.get(PARAMETER_MEMBERCLASS);
        this.memberCode = parameterMap.get(PARAMETER_MEMBERCODE);
        this.subsystemCode = parameterMap.get(PARAMETER_SUBSYSTEMCODE);
        this.serviceCode = parameterMap.get(PARAMETER_SERVICECODE);
        this.serviceVersion = parameterMap.get(PARAMETER_SERVICEVERSION);
    }

    private static Map<String, String> extractHeaders(HttpServletRequest request) {
        HashMap<String, String> headers = new HashMap<>();
        for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements(); ) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        return headers;
    }

    private static Map<String, String> firstParameter(Map<String, String[]> parameters) {
        HashMap<String, String> map = new HashMap<>();
        for (String name : parameters.keySet()) {
            String[] values = parameters.get(name);
            String value = null;
            if (values != null && values.length > 0) {
                value = values[0];
            }
            map.put(name, value);
        }
        return map;
    }

    @Override
    public String getNameQualifier() {
        return this.nameQualifier;
    }

    @Override
    public String getIdentity() {
        return String.format("%s:%s:%s:%s", this.xRoadInstance, this.memberClass, this.memberCode, this.subsystemCode);
    }

    @Override
    public String getOnBehalfOf() {
        return this.onBehalfOf;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean hasSystemRole(String role) {
        return true;
    }

    @Override
    public boolean hasUserProfile(String userProfileName) {
        return true;
    }

    @Override
    public Collection<String> getUserProfiles() {
        return userProfiles.keySet();
    }

    @Override
    public Collection<String> getSystemRoles() {
        return systemRoles.keySet();
    }

    @Override
    public Collection<UserProfile> getUserProfilesForRole(String role) {
        if (systemRoles.containsKey(role)) {
            return systemRoles.get(role);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public String toString() {
        return this.pituClient;
    }


    private static final Pattern certInfoPattern = Pattern.compile("^(\\w+)=\"([^\"]+)\"$");
    public static Map<String, String> parseSSLClientCertInfo(String sslClientCertInfo) {
        HashMap<String, String> out = new HashMap<>();
        if (sslClientCertInfo != null) {
            sslClientCertInfo = URLDecoder.decode(sslClientCertInfo, StandardCharsets.UTF_8);
            for (String part : sslClientCertInfo.split(";")) {
                Matcher m = certInfoPattern.matcher(part);
                if (m.find()) {
                    String key = m.group(1);
                    String value = m.group(2);
                    if (key != null && (key.equals("Subject") || key.equals("Issuer"))) {
                        out.put(key, value);
                    }
                }
            }
        }
        System.out.println("out: "+out);
        return out;
    }
}
