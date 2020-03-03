package dk.magenta.dafosts.library;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSInteger;
import org.opensaml.xml.schema.XSString;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;

import static dk.magenta.dafosts.library.DafoTokenGenerator.ON_BEHALF_OF_CLAIM_URL;
import static dk.magenta.dafosts.library.DafoTokenGenerator.TOKEN_ID_CLAIM_URL;
import static dk.magenta.dafosts.library.DatabaseQueryManager.INVALID_TOKEN_ID;

/**
 * A wrapper that prefixes log messages with information about the request's remote address and the principal.
 */
public class LogRequestWrapper {
    private Logger logger;
    private HttpServletRequest request;
    private String userName;
    private String prefix;

    public LogRequestWrapper(Logger logger, HttpServletRequest request, String userName) {
        this.logger = logger;
        this.request = request;
        this.userName = userName;
        this.updatePrefix();
    }

    public LogRequestWrapper(Logger logger, HttpServletRequest request) {
        this(logger, request, null);
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
        updatePrefix();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        updatePrefix();
    }


    private void updatePrefix() {
        String remoteAddr = request.getHeader("X-Forwarded-For");
        if(remoteAddr == null) {
            remoteAddr = request.getRemoteAddr();
        }
        prefix = String.format(
                "[%s]@[%s]: ",
                userName == null ? "<unknown>" : userName,
                remoteAddr
        );
    }

    public static String getOnBehalfOfStringFromToken(Assertion assertion) {
        for(AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
            for(Attribute attribute : attributeStatement.getAttributes()) {
                if(attribute.getName().equals(ON_BEHALF_OF_CLAIM_URL)) {
                    for(XMLObject value : attribute.getAttributeValues()) {
                        if (value instanceof XSString) {
                            return ((XSString) value).getValue();
                        } else if (value instanceof XSAny) {
                            return ((XSAny) value).getTextContent();
                        }
                    }
                }
            }
        }
        return null;
    }

    public static int getTokenIdFromToken(Assertion assertion) {
        for(AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
            for(Attribute attribute : attributeStatement.getAttributes()) {
                if(attribute.getName().equals(TOKEN_ID_CLAIM_URL)) {
                    for(XMLObject value : attribute.getAttributeValues()) {
                        if (value instanceof XSInteger) {
                            return ((XSInteger) value).getValue();
                        } else {
                            return INVALID_TOKEN_ID;
                        }
                    }
                }
            }
        }
        return INVALID_TOKEN_ID;
    }

    public void logIssuedToken(Assertion assertion) {
        String onBehalfOf = getOnBehalfOfStringFromToken(assertion);
        info("Issuing token for "
                + assertion.getSubject().getNameID().getValue()
                + (onBehalfOf == null ? "" : String.format(" on behalf of %s", onBehalfOf))
                + " with IssueInstant "
                + assertion.getIssueInstant()
        );
    }

    public void trace(String msg) {
        logger.trace(prefix + msg);
    }

    public void debug(String msg) {
        logger.debug(prefix + msg);
    }

    public void info(String msg) {
        logger.info(prefix + msg);
    }

    public void warn(String msg) {
        logger.warn(prefix + msg);
    }

    public void error(String msg) {
        logger.error(prefix + msg);
    }

}
