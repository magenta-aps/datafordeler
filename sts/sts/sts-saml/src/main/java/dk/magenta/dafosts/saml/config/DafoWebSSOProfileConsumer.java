package dk.magenta.dafosts.saml.config;

import com.github.ulisesbocchio.spring.boot.security.saml.bean.override.DSLWebSSOProfileConsumerImpl;
import org.opensaml.common.SAMLException;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.context.SAMLMessageContext;

import java.util.List;

import static org.springframework.security.saml.util.SAMLUtil.isDateTimeSkewValid;

/**
 * Created by row on 07-05-2017.
 */
public class DafoWebSSOProfileConsumer extends DSLWebSSOProfileConsumerImpl {
    @Override
    public void verifyAssertion(Assertion assertion, AuthnRequest request, SAMLMessageContext context) throws AuthenticationException, SAMLException, SecurityException, ValidationException, DecryptionException {
        super.verifyAssertion(assertion, request, context);
    }

    @Override
    public void verifySubject(Subject subject, AuthnRequest request, SAMLMessageContext context) throws SAMLException, DecryptionException {
        super.verifySubject(subject, request, context);
    }

    @Override
    public void verifyAssertionSignature(Signature signature, SAMLMessageContext context) throws SAMLException, SecurityException, ValidationException {
        super.verifyAssertionSignature(signature, context);
    }

    @Override
    public void verifyAssertionConditions(Conditions conditions, SAMLMessageContext context, boolean audienceRequired) throws SAMLException {
        super.verifyAssertionConditions(conditions, context, audienceRequired);
    }

    @Override
    public void verifyAudience(SAMLMessageContext context, List<AudienceRestriction> audienceRestrictions) throws SAMLException {
        super.verifyAudience(context, audienceRestrictions);
    }

    @Override
    public void verifyConditions(SAMLMessageContext context, List<Condition> conditions) throws SAMLException {
        super.verifyConditions(context, conditions);
    }

    @Override
    public void verifyAuthenticationStatement(AuthnStatement auth, RequestedAuthnContext requestedAuthnContext, SAMLMessageContext context) throws AuthenticationException {
        super.verifyAuthenticationStatement(auth, requestedAuthnContext, context);
    }

    @Override
    public void verifyAuthnContext(RequestedAuthnContext requestedAuthnContext, AuthnContext receivedContext, SAMLMessageContext context) throws InsufficientAuthenticationException {
        super.verifyAuthnContext(requestedAuthnContext, receivedContext, context);
    }

    @Override
    public long getMaxAuthenticationAge() {
        // Allow tokens up to a day old
        return 60 * 60 * 24;
    }

    public void verifyPassiveAssertion(Assertion assertion, SAMLMessageContext context)
            throws AuthenticationException, SAMLException,
            org.opensaml.xml.security.SecurityException,
            ValidationException, DecryptionException {

        // Verify storage time skew
        if (!isDateTimeSkewValid(getResponseSkew(), getMaxAssertionTime(), assertion.getIssueInstant())) {
            throw new SAMLException("Assertion is too old to be used, value can be customized by setting maxAssertionTime value " + assertion.getIssueInstant());
        }

        // Verify validity of storage
        // Advice is ignored, core 574
        verifyIssuer(assertion.getIssuer(), context);
        verifyAssertionSignature(assertion.getSignature(), context);

        // Check subject
//        if (assertion.getSubject() != null) {
//            verifySubject(assertion.getSubject(), request, context);
//        } else {
//            throw new SAMLException("Assertion does not contain subject and is discarded");
//        }

        // Assertion with authentication statement must contain audience restriction
        if (assertion.getAuthnStatements().size() > 0) {
            verifyAssertionConditions(assertion.getConditions(), context, true);
            for (AuthnStatement statement : assertion.getAuthnStatements()) {
                    verifyAuthenticationStatement(statement, null, context);
            }
        } else {
            verifyAssertionConditions(assertion.getConditions(), context, false);
        }

    }


}
