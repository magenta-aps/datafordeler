package dk.magenta.dafosts.saml.users;

import com.github.ulisesbocchio.spring.boot.security.saml.bean.SAMLConfigurerBean;
import com.github.ulisesbocchio.spring.boot.security.saml.bean.override.DSLWebSSOProfileConsumerImpl;
import dk.magenta.dafosts.saml.config.DafoWebSSOProfileConsumer;
import dk.magenta.dafosts.saml.config.SamlWebSecurityConfig;
import org.opensaml.common.SAMLException;
import org.opensaml.common.binding.decoding.BasicURLComparator;
import org.opensaml.common.binding.decoding.URIComparator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.impl.EntityDescriptorImpl;
import org.opensaml.saml2.metadata.impl.RoleDescriptorImpl;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.x509.BasicX509CredentialNameEvaluator;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.signature.impl.PKIXSignatureTrustEngine;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@Component
public class DafoAssertionVerifier {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private long maxAuthenticationAge = 7200;

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    @Autowired
    SamlWebSecurityConfig samlWebSecurityConfig;

    @Autowired
    SAMLConfigurerBean samlConfigurerBean;

    MetadataManager metadataManager;

    MetadataManager getMetadataManager() {
        if(metadataManager == null) {
            metadataManager = samlConfigurerBean.serviceProvider().getSharedObject(MetadataManager.class);
        }
        return metadataManager;
    }

    public Assertion parseAssertion(String fromString) {
        try {
            Inflater inflater = new Inflater(true);
            byte[] data = Base64.decode(fromString);
            inflater.setInput(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
            InputStream in = new ByteArrayInputStream(outputStream.toByteArray());


            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();

            Document document = docBuilder.parse(in);
            Element element = document.getDocumentElement();

            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);

            return (Assertion) unmarshaller.unmarshall(element);
        }
        catch (IOException|ParserConfigurationException|SAXException|UnmarshallingException|DataFormatException e) {
            e.printStackTrace();
        }

        return null;
    }


    public Assertion verifyAssertion(String incommingAssertingString, HttpServletRequest request, HttpServletResponse response) {
        Assertion assertion = parseAssertion(incommingAssertingString);
        if(assertion == null) {
            return assertion;
        }
        SAMLMessageContext context;
        try {
            context = samlConfigurerBean.serviceProvider().getSharedObject(SAMLContextProvider.class).getLocalEntity(request, response);
        }
        catch(MetadataProviderException e) {
            e.printStackTrace();
            return null;
        }
        MetadataManager metadataManager = getMetadataManager();

        // TODO: This should handle multiple IdPs and identify the correct one
        // Seems that WSO2 will provide another NameID for passive tokens, so have to configure some sort of
        // alias setup.
        for(String idpName : metadataManager.getIDPEntityNames()) {
            try {
                context.setPeerEntityMetadata(metadataManager.getEntityDescriptor(idpName));
                context.setPeerExtendedMetadata(metadataManager.getExtendedMetadata(idpName));
                break;
            }
            catch(MetadataProviderException e) {
                e.printStackTrace();
            }
        }

        try {
            DafoWebSSOProfileConsumer consumer = samlWebSecurityConfig.getDafoWebSSOProfileConsumer();
            consumer.verifyPassiveAssertion(assertion, context);
            return assertion;
        }
        catch(SAMLException|SecurityException|ValidationException|DecryptionException e) {
            e.printStackTrace();
            return null;
        }
    }


//
//    protected void verifyAssertion(Assertion assertion, SAMLMessageContext context)
//            throws AuthenticationException, SAMLException, org.opensaml.xml.security.SecurityException,
//            ValidationException, DecryptionException {
//
//        // Verify storage time skew
//        if (!isDateTimeSkewValid(
//                dslWebSSOProfileConsumer.getResponseSkew(),
//                dslWebSSOProfileConsumer.getMaxAssertionTime(),
//                assertion.getIssueInstant())) {
//            throw new SAMLException(
//                    "Assertion is too old to be used, value can be customized by setting maxAssertionTime value " +
//                            assertion.getIssueInstant()
//            );
//        }
//
//        // Verify validity of storage
//        // Advice is ignored, core 574
//        verifyIssuer(assertion.getIssuer(), context);
//        verifyAssertionSignature(assertion.getSignature(), context);
//
//        // Check subject
//        if (assertion.getSubject() != null) {
//            verifySubject(assertion.getSubject(), context);
//        } else {
//            throw new SAMLException("Assertion does not contain subject and is discarded");
//        }
//
//        // Assertion with authentication statement must contain audience restriction
//        if (assertion.getAuthnStatements().size() > 0) {
//            verifyAssertionConditions(assertion.getConditions(), context, true);
//            for (AuthnStatement statement : assertion.getAuthnStatements()) {
//                    verifyAuthenticationStatement(statement, null, context);
//            }
//        } else {
//            verifyAssertionConditions(assertion.getConditions(), context, false);
//        }
//
//    }
//
//
//    protected void verifyIssuer(Issuer issuer, SAMLMessageContext context) throws SAMLException {
//        // Validate format of issuer
//        if (issuer.getFormat() != null && !issuer.getFormat().equals(NameIDType.ENTITY)) {
//            throw new SAMLException("Issuer invalidated by issuer type " + issuer.getFormat());
//        }
//        // Validate that issuer is expected peer entity
//        if (!context.getPeerEntityMetadata().getEntityID().equals(issuer.getValue())) {
//            throw new SAMLException("Issuer invalidated by issuer value " + issuer.getValue());
//        }
//    }
//
//
//    /**
//     * Verifies validity of Subject element, only bearer confirmation is validated.
//     *
//     * @param subject subject to validate
//     * @param context context
//     * @throws SAMLException       error validating the object
//     * @throws DecryptionException in case the NameID can't be decrypted
//     */
//    protected void verifySubject(Subject subject, SAMLMessageContext context) throws SAMLException, DecryptionException {
//
//        for (SubjectConfirmation confirmation : subject.getSubjectConfirmations()) {
//
//            if (SubjectConfirmation.METHOD_BEARER.equals(confirmation.getMethod())) {
//
//                log.debug("Processing Bearer subject confirmation");
//                SubjectConfirmationData data = confirmation.getSubjectConfirmationData();
//
//                // Bearer must have confirmation saml-profiles-2.0-os 554
//                if (data == null) {
//                    log.debug("Bearer SubjectConfirmation invalidated by missing confirmation data");
//                    continue;
//                }
//
//                // Not before forbidden by saml-profiles-2.0-os 558
//                if (data.getNotBefore() != null) {
//                    log.debug("Bearer SubjectConfirmation invalidated by not before which is forbidden");
//                    continue;
//                }
//
//                // Required by saml-profiles-2.0-os 556
//                if (data.getNotOnOrAfter() == null) {
//                    log.debug("Bearer SubjectConfirmation invalidated by missing notOnOrAfter");
//                    continue;
//                }
//
//                // Validate not on or after
//                if (data.getNotOnOrAfter().plusSeconds(dslWebSSOProfileConsumer.getResponseSkew()).isBeforeNow()) {
//                    log.debug("Bearer SubjectConfirmation invalidated by notOnOrAfter");
//                    continue;
//                }
//
//                // Validate recipient
//                if (data.getRecipient() == null) {
//                    log.debug("Bearer SubjectConfirmation invalidated by missing recipient");
//                    continue;
//                } else {
//                    try {
//                        verifyEndpoint(context.getLocalEntityEndpoint(), data.getRecipient());
//                    } catch (SAMLException e) {
//                        log.debug("Bearer SubjectConfirmation invalidated by recipient assertion consumer URL, found {}", data.getRecipient());
//                        continue;
//                    }
//                }
//
//                // Was the subject confirmed by this confirmation data? If so let's store the subject in the context.
//                NameID nameID;
//                if (subject.getEncryptedID() != null) {
//                    Assert.notNull(context.getLocalDecrypter(), "Can't decrypt NameID, no decrypter is set in the context");
//                    nameID = (NameID) context.getLocalDecrypter().decrypt(subject.getEncryptedID());
//                } else {
//                    nameID = subject.getNameID();
//                }
//                context.setSubjectNameIdentifier(nameID);
//                return;
//
//            }
//
//        }
//
//        throw new SAMLException("Assertion invalidated by subject confirmation - can't be confirmed by the bearer method");
//
//    }
//
//
//    protected URIComparator uriComparator = new BasicURLComparator();
//
//    /**
//     * Verifies that the destination URL intended in the message matches with the endpoint address. The URL message
//     * was ultimately received doesn't need to necessarily match the one defined in the metadata (in case of e.g. reverse-proxying
//     * of messages).
//     *
//     * @param endpoint endpoint the message was received at
//     * @param destination URL of the endpoint the message was intended to be sent to by the peer or null when not included
//     * @throws SAMLException in case endpoint doesn't match
//     */
//    protected void verifyEndpoint(Endpoint endpoint, String destination) throws SAMLException {
//        // Verify that destination in the response matches one of the available endpoints
//        if (destination != null) {
//            if (uriComparator.compare(destination, endpoint.getLocation())) {
//                // Expected
//            } else if (uriComparator.compare(destination, endpoint.getResponseLocation())) {
//                // Expected
//            } else {
//                throw new SAMLException("Intended destination " + destination + " doesn't match any of the endpoint URLs on endpoint " + endpoint.getLocation() + " for profile " + getProfileIdentifier());
//            }
//        }
//    }
//
//    /**
//     * Verifies signature of the assertion. In case signature is not present and SP required signatures in metadata
//     * the exception is thrown.
//     *
//     * @param signature signature to verify
//     * @param context   context
//     * @throws SAMLException       signature missing although required
//     * @throws org.opensaml.xml.security.SecurityException
//     *                             signature can't be validated
//     * @throws ValidationException signature is malformed
//     */
//    protected void verifyAssertionSignature(Signature signature, SAMLMessageContext context)
//            throws SAMLException, org.opensaml.xml.security.SecurityException, ValidationException {
//        SPSSODescriptor roleMetadata = (SPSSODescriptor) context.getLocalEntityRoleMetadata();
//        boolean wantSigned = roleMetadata.getWantAssertionsSigned();
//        if (signature != null) {
//            verifySignature(signature, context.getPeerEntityMetadata().getEntityID(), context.getLocalTrustEngine());
//        } else if (wantSigned) {
//            if (!context.isInboundSAMLMessageAuthenticated()) {
//                throw new SAMLException("Metadata includes wantAssertionSigned, but neither Response nor included Assertion is signed");
//            }
//        }
//    }
//
//    protected void verifyAssertionConditions(
//            Conditions conditions, SAMLMessageContext context, boolean audienceRequired) throws SAMLException {
//
//        // Verify that audience is present when required
//        if (audienceRequired && (conditions == null || conditions.getAudienceRestrictions().size() == 0)) {
//            throw new SAMLException("Assertion invalidated by missing Audience Restriction");
//        }
//
//        // If no conditions are implied, storage is deemed valid
//        if (conditions == null) {
//            return;
//        }
//
//        if (conditions.getNotBefore() != null) {
//            if (conditions.getNotBefore().minusSeconds(getResponseSkew()).isAfterNow()) {
//                throw new SAMLException("Assertion is not yet valid, invalidated by condition notBefore " + conditions.getNotBefore());
//            }
//        }
//        if (conditions.getNotOnOrAfter() != null) {
//            if (conditions.getNotOnOrAfter().plusSeconds(getResponseSkew()).isBeforeNow()) {
//                throw new SAMLException("Assertion is no longer valid, invalidated by condition notOnOrAfter " + conditions.getNotOnOrAfter());
//            }
//        }
//
//        List<Condition> notUnderstoodConditions = new LinkedList<Condition>();
//
//        for (Condition condition : conditions.getConditions()) {
//
//            QName conditionQName = condition.getElementQName();
//
//            if (conditionQName.equals(AudienceRestriction.DEFAULT_ELEMENT_NAME)) {
//
//                verifyAudience(context, conditions.getAudienceRestrictions());
//
//            } else if (conditionQName.equals(OneTimeUse.DEFAULT_ELEMENT_NAME)) {
//
//                throw new SAMLException("System cannot honor OneTimeUse condition of the Assertion for WebSSO");
//
//            } else if (conditionQName.equals(ProxyRestriction.DEFAULT_ELEMENT_NAME)) {
//
//                ProxyRestriction restriction = (ProxyRestriction) condition;
//                log.debug("Honoring ProxyRestriction with count {}, system does not issue assertions to 3rd parties", restriction.getProxyCount());
//
//            } else {
//
//                log.debug("Condition {} is not understood", condition);
//                notUnderstoodConditions.add(condition);
//
//            }
//
//        }
//
//        // Check not understood conditions
//        verifyConditions(notUnderstoodConditions);
//
//    }
//
//
//    /**
//     * Verifies that authentication statement is valid. Checks the authInstant and sessionNotOnOrAfter fields.
//     *
//     * @param auth                  statement to check
//     * @param requestedAuthnContext original requested context can be null for unsolicited messages or when no context was requested
//     * @param context               message context
//     * @throws AuthenticationException in case the statement is invalid
//     */
//    protected void verifyAuthenticationStatement(
//            AuthnStatement auth, RequestedAuthnContext requestedAuthnContext, SAMLMessageContext context
//    ) throws AuthenticationException {
//
//        // Validate that user wasn't authenticated too long time ago
//        if (!isDateTimeSkewValid(getResponseSkew(), getMaxAuthenticationAge(), auth.getAuthnInstant())) {
//            throw new CredentialsExpiredException("Authentication statement is too old to be used with value " + auth.getAuthnInstant());
//        }
//
//        // Validate users session is still valid
//        if (auth.getSessionNotOnOrAfter() != null && auth.getSessionNotOnOrAfter().isBeforeNow()) {
//            throw new CredentialsExpiredException("Authentication session is not valid on or after " + auth.getSessionNotOnOrAfter());
//        }
//    }
//
//
//    /**
//     * Method verifies audience restrictions of the assertion. Multiple audience restrictions are treated as
//     * a logical AND and local entity must be present in all of them. Multiple audiences within one restrictions
//     * for a logical OR.
//     *
//     * @param context context
//     * @param audienceRestrictions audience restrictions to verify
//     * @throws SAMLException in case local entity doesn't match the audience restrictions
//     */
//    protected void verifyAudience(SAMLMessageContext context, List<AudienceRestriction> audienceRestrictions) throws SAMLException {
//
//        // Multiple AudienceRestrictions form a logical "AND" (saml-core, 922-925)
//        audience:
//        for (AudienceRestriction rest : audienceRestrictions) {
//            if (rest.getAudiences().size() == 0) {
//                throw new SAMLException("No audit audience specified for the assertion");
//            }
//            for (Audience aud : rest.getAudiences()) {
//                // Multiple Audiences within one AudienceRestriction form a logical "OR" (saml-core, 922-925)
//                if (context.getLocalEntityId().equals(aud.getAudienceURI())) {
//                    continue audience;
//                }
//            }
//            throw new SAMLException("Local entity is not the intended audience of the assertion in at least " +
//                    "one AudienceRestriction");
//        }
//
//    }
//
//    /**
//     * Verifies conditions of the assertion which were are not understood. By default system fails in case any
//     * non-understood condition is present.
//     *
//     * @param conditions conditions which were not understood
//     * @throws SAMLException in case conditions are not empty
//     */
//    protected void verifyConditions(List<Condition> conditions) throws SAMLException {
//        if (conditions != null && conditions.size() > 0) {
//            throw new SAMLException("Assertion contains conditions which are not understood");
//        }
//    }
//
}
