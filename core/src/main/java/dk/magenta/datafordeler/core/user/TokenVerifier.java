package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.security.trust.TrustEngine;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Verifies a DAFO token according to expiration, issuer, signature and audience restriction.
 */
@Component
@EnableConfigurationProperties(TokenConfigProperties.class)
@PropertySource("classpath:/application.properties")
public class TokenVerifier {

    @Autowired
    private MetadataProvider metadataProvider;
    @Autowired
    private TrustEngine trustEngine;
    @Autowired
    private TokenConfigProperties config;

    private String cachedIssuerCert;

    private String getCachedIssuerCert() throws InvalidTokenException {
        if (cachedIssuerCert == null) {
            try {
                cachedIssuerCert = getIssuerEntityDescriptor().getIDPSSODescriptor(
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

    public TokenVerifier() throws ConfigurationException {
        // Initialize OpenSAML
        org.opensaml.DefaultBootstrap.bootstrap();
    }

    public EntityDescriptor getIssuerEntityDescriptor() throws InvalidTokenException {
        try {
            return (EntityDescriptor) metadataProvider.getMetadata();
        } catch (MetadataProviderException e) {
            throw new InvalidTokenException("Unable to fetch issuer metadata: " + e.getMessage(), e);
        }
    }

    public void verifyIssuer(Issuer issuer) throws InvalidTokenException {
        // Validate format of issuer
        if (issuer.getFormat() != null && !issuer.getFormat().equals(NameIDType.ENTITY)) {
            throw new InvalidTokenException("Wrong issuer type: " + issuer.getFormat());
        }
        // Validate that issuer is expected peer entity
        if (!issuer.getValue().equals(getIssuerEntityDescriptor().getEntityID())) {
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
        criteriaSet.add(new EntityIdCriterion(getIssuerEntityDescriptor().getEntityID()));
        criteriaSet.add(new MetadataCriterion(
                IDPSSODescriptor.DEFAULT_ELEMENT_NAME, SAMLConstants.SAML20P_NS
        ));
        criteriaSet.add(new UsageCriterion(UsageType.SIGNING));

        boolean criteriaAreValid;
        try {
            criteriaAreValid = trustEngine.validate(signature, criteriaSet);
        } catch (SecurityException e) {
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
            if (audience.getAudienceURI().equals(config.getAudienceURI())) {
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
