package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import jakarta.annotation.PostConstruct;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.resolver.Criterion;
import net.shibboleth.shared.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

/**
 * Verifies a DAFO token according to expiration, issuer, signature and audience restriction.
 */
@Component
@EnableConfigurationProperties(TokenConfigProperties.class)
@PropertySource("classpath:/application.properties")
public class TokenVerifier {

    @Autowired
    private MetadataResolver metadataResolver;

    @Autowired
    private ExplicitKeySignatureTrustEngine trustEngine;

    @Autowired
    private TokenConfigProperties config;

    EntityDescriptor entityDescriptor;

    private String cachedIssuerCert;

    private String getCachedIssuerCert() throws InvalidTokenException {
        if (cachedIssuerCert == null) {
            try {
                cachedIssuerCert = this.entityDescriptor.getIDPSSODescriptor(
                                "urn:oasis:names:tc:SAML:2.0:protocol"
                        ).getKeyDescriptors().get(0).getKeyInfo().getX509Datas().get(0).getX509Certificates()
                        .get(0).getValue().replaceAll("\\s+", "");
            } catch (Exception e) {
                throw new InvalidTokenException(
                        "Could not get signature certificate from token: " + e.getMessage(), e
                );
            }
        }
        return cachedIssuerCert;
    }

    public TokenVerifier() {
    }

    @PostConstruct
    public void init() throws dk.magenta.datafordeler.core.exception.ConfigurationException, ResolverException {

        CriteriaSet criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion("Dafo-STS"));

        this.entityDescriptor = this.metadataResolver.resolveSingle(criteriaSet);
        if (this.entityDescriptor.getEntityID() == null) {
            throw new dk.magenta.datafordeler.core.exception.ConfigurationException("Entity descriptor id is null");
        }
    }

    public void verifyIssuer(Issuer issuer) throws InvalidTokenException {
        // Validate format of issuer
        if (issuer.getFormat() != null && !issuer.getFormat().equals(NameIDType.ENTITY)) {
            throw new InvalidTokenException("Wrong issuer type: " + issuer.getFormat());
        }
        // Validate that issuer is expected peer entity
        if (!Objects.equals(issuer.getValue(), this.entityDescriptor.getEntityID())) {
            throw new InvalidTokenException("Invalid issuer: " + issuer.getValue());
        }
    }

    public void verifySignatureAndTrust(Signature signature) throws InvalidTokenException {
        // Verify that the certificate used to sign the token is the one we associate with our
        // trusted issuer.
        String signatureCert;
        try {
            signatureCert = signature.getKeyInfo().getX509Datas().get(0).getX509Certificates()
                    .get(0).getValue().replaceAll("\\s+", "");
        } catch (Exception e) {
            throw new InvalidTokenException(
                    "Could not get signature certificate from token: " + e.getMessage(), e
            );
        }
        if (!signatureCert.equals(getCachedIssuerCert())) {
            throw new InvalidTokenException("Untrusted certificate used to sign token");
        }


        // Validate the actual signing is correct
        SAMLSignatureProfileValidator validator = new SAMLSignatureProfileValidator();

        try {
            validator.validate(signature);
        } catch (SignatureException e) {
            throw new InvalidTokenException("Invalid token signature: " + e.getMessage(), e);
        }

        // Check list of necessary criteria that ensures that it is the signature we want and
        // not just any syntactically correct signature.
        CriteriaSet criteriaSet = new CriteriaSet();
        String expectedEntityId = this.entityDescriptor.getEntityID();
        System.out.println("expectedEntityId: " + expectedEntityId);
        criteriaSet.add(new EntityIdCriterion(expectedEntityId));

        System.out.println("role: "+IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        criteriaSet.add(new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));

        System.out.println("Usage: "+UsageType.SIGNING);
        criteriaSet.add(new UsageCriterion(UsageType.SIGNING));

        for (Criterion c : criteriaSet) {
            CriteriaSet s = new CriteriaSet();
            s.add(c);
            try {
                System.out.println(c.getClass().getSimpleName()+": "+trustEngine.validate(signature, s));
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            }
        }

        boolean criteriaAreValid;
        try {
            criteriaAreValid = trustEngine.validate(signature, criteriaSet);
        } catch (org.opensaml.security.SecurityException e) {
            throw new InvalidTokenException(
                    "Security exception while validating token signature: " + e.getMessage(), e
            );
        }
        if (!criteriaAreValid) {
            throw new InvalidTokenException("Signature is not trusted or invalid");
        }
    }

    public void verifyTokenAge(Instant issueInstant) throws InvalidTokenException {
        long reference = System.currentTimeMillis();
        int skewInSec = config.getTimeSkewInSeconds();
        long forwardInterval = config.getMaxAssertionTimeInSeconds();

        if (issueInstant.isAfter(Instant.ofEpochMilli(reference + (skewInSec * 1000L)))) {
            throw new InvalidTokenException("Token is issued in the future");
        }

        // If issueInstant is before the current time minus lifetime of token minus skew it is too
        // old.
        if (issueInstant.isBefore(Instant.ofEpochMilli(reference - ((skewInSec + forwardInterval) * 1000L)))) {
            throw new InvalidTokenException("Token is older than " + forwardInterval + " seconds");
        }
    }

    public boolean checkNotBefore(Instant time) {
        return !time.minusSeconds(config.getTimeSkewInSeconds()).isAfter(Instant.now());
    }

    public boolean checkNotOnOrafter(Instant time) {
        return !time.plusSeconds(config.getTimeSkewInSeconds()).isBefore(Instant.now());
    }

    public void verifySubject(Subject subject) throws InvalidTokenException {
        // TODO: Full BEARER validation? Would require recipient in the token
        if (subject == null) {
            throw new InvalidTokenException("No subject specified in token");
        }
        if (subject.getNameID() == null) {
            throw new InvalidTokenException("No NameID specified in token subject");
        }
        // We expect there to be a single SubjectConfirmationData so we fetch that
        SubjectConfirmationData subjectConfirmationData;
        try {
            subjectConfirmationData = subject.getSubjectConfirmations().get(0)
                    .getSubjectConfirmationData();
        } catch (Exception e) {
            throw new InvalidTokenException(
                    "Unable to get SubjectConfirmationData from token: " + e.getMessage(), e
            );
        }
        // Check the timestamps on the SubjectConfirmationData
        Instant notBefore = subjectConfirmationData.getNotBefore();
        if (notBefore != null && !checkNotBefore(notBefore)) {
            throw new InvalidTokenException("Failed NotBefore constraint on SubjectConfirmationData");
        }
        Instant notOnOrAfter = subjectConfirmationData.getNotOnOrAfter();
        if (notOnOrAfter == null) {
            throw new InvalidTokenException("NotOnOrAfter not specified for SubjectConfirmationData");
        } else {
            if (!checkNotOnOrafter(notOnOrAfter)) {
                throw new InvalidTokenException(
                        "Failed NotOnOrAfter constraint on SubjectConfirmationData"
                );
            }
        }
    }


    public void verifyConditions(Conditions conditions) throws InvalidTokenException {
        Instant notBefore = conditions.getNotBefore();
        if (notBefore == null) {
            throw new InvalidTokenException("NotBefore not defined on Conditions");
        } else {
            if (!checkNotBefore(notBefore)) {
                throw new InvalidTokenException("Failed NotBefore contraint on Conditions");
            }
        }
        Instant notOnOrAfter = conditions.getNotOnOrAfter();
        if (notOnOrAfter == null) {
            throw new InvalidTokenException("NotOnOrAfter not defined on Conditions");
        } else {
            if (!checkNotOnOrafter(notOnOrAfter)) {
                throw new InvalidTokenException("Failed NotOnOrAfter constraint on Conditions");
            }
        }

        AudienceRestriction audienceRestriction;
        try {
            audienceRestriction = conditions.getAudienceRestrictions().get(0);
        } catch (Exception e) {
            throw new InvalidTokenException("No AudienceRestriction in token");
        }
        boolean found = false;
        for (Audience audience : audienceRestriction.getAudiences()) {
            if (Objects.equals(audience.getURI(), config.getAudienceURI())) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new InvalidTokenException(
                    "Expected AudienceURI, " + config.getAudienceURI() + ", was not found in the token"
            );
        }
    }

    public void verifyAssertion(Assertion assertion) throws InvalidTokenException {
        verifyTokenAge(assertion.getIssueInstant());
        verifyIssuer(assertion.getIssuer());
        verifySubject(assertion.getSubject());
        verifyConditions(assertion.getConditions());
        verifySignatureAndTrust(assertion.getSignature());
    }

}
