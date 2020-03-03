package dk.magenta.dafosts.library;

import dk.magenta.dafosts.library.users.DafoUserData;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.ResponseMarshaller;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.NameIDFormat;
import org.opensaml.saml2.metadata.impl.EntityDescriptorBuilder;
import org.opensaml.saml2.metadata.impl.IDPSSODescriptorBuilder;
import org.opensaml.saml2.metadata.impl.KeyDescriptorBuilder;
import org.opensaml.saml2.metadata.impl.NameIDFormatBuilder;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSInteger;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.signature.impl.KeyInfoBuilder;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.signature.impl.X509CertificateBuilder;
import org.opensaml.xml.signature.impl.X509DataBuilder;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.io.*;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import static dk.magenta.dafosts.library.DatabaseQueryManager.INVALID_TOKEN_ID;

@EnableConfigurationProperties(TokenGeneratorProperties.class)
public class DafoTokenGenerator {

    public static String USERPROFILE_CLAIM_URL = "https://data.gl/claims/userprofile";
    public static String ON_BEHALF_OF_CLAIM_URL = "https://data.gl/claims/on-behalf-of";
    public static String TOKEN_ID_CLAIM_URL = "https://data.gl/claims/token-id";

    @Autowired
    DatabaseQueryManager databaseQueryManager;

    TokenGeneratorProperties properties;

    BasicX509Credential signingCredential;

    // TODO: Externalize this to a @ConfiguratonProperties class?
    @Value("${dafo.sts.issuer-entity-id:Dafo-STS}")
    private String issuerEntityID = "Dafo-STS";
    @Value("${dafo.sts.audience-url:https://data.gl/}")
    private String audienceURL = "https://data.gl/";

    public DafoTokenGenerator(TokenGeneratorProperties properties) throws Exception {
        this.properties = properties;
        this.signingCredential = new BasicX509Credential();
        CertificateFactory fact = CertificateFactory.getInstance("X.509");

        InputStream is = getInputStreamFromConfigLocation(properties.getPublicKeyPemLocation());
        X509Certificate cert = (X509Certificate) fact.generateCertificate(is);
        this.signingCredential.setEntityCertificate(cert);

        InputStream privateKeyInputStream = getInputStreamFromConfigLocation(properties.getPrivateKeyDerLocation());
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = privateKeyInputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer.toByteArray());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey pk = keyFactory.generatePrivate(keySpec);
        this.signingCredential.setPrivateKey(pk);
    }

    public static InputStream getInputStreamFromConfigLocation(String location) throws IOException {
        if(location.startsWith("classpath:")) {
            String publicKeyPath = location.substring(location.indexOf(":") + 1);
            ClassPathResource publibKeyResource = new ClassPathResource(publicKeyPath);
            return publibKeyResource.getInputStream();
        } else {
            if(location.startsWith("file:")) {
                location = location.substring(location.indexOf(":") + 1);
            }
            return new FileInputStream(location);
        }

    }

    /**
     * Shortcut method for generating SAML Object builders.
     * @param qName - Specifies the type of builder to get, typically specified as SamlClass.DEFAULT_ELEMENT_NAME
     * @param <T> - The type you want the builder returned as
     * @return A builder of type T
     */
    public <T>  T getObjectBuilder(QName qName) {
        // TODO: Replace all SAMLObjectBuilders with stongly typed builders generated by this generic method.
        return (T) Configuration.getBuilderFactory().getBuilder(qName);
    }


    /**
     * Builds the NameId part of a token.
     * @param user - The user identified by the bootstrap SAML token
     * @return A populated SAML NameID object
     * @throws Exception
     */
    public NameID buildNameId(DafoUserData user) throws Exception {
        SAMLObjectBuilder builder = getObjectBuilder(NameID.DEFAULT_ELEMENT_NAME);

        NameID nameId = (NameID) builder.buildObject();
        nameId.setValue(user.getUsername());
        if(user.getNameQualifier() != null) {
            nameId.setNameQualifier(user.getNameQualifier());
        }
        nameId.setFormat(NameID.UNSPECIFIED);

        return nameId;
    }

    /**
     * Builds the SubjectConfirmationData part of a token.
     * @param now - The time at which the token is generated
     * @return A populated SubjectConfirmationData object
     */
    public SubjectConfirmationData buildSubjectConfirmationData(DateTime now) {
        // Create the SubjectConfirmation
        SAMLObjectBuilder builder = getObjectBuilder(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);

        SubjectConfirmationData confirmationMethod = (SubjectConfirmationData) builder.buildObject();
        // NotBefore is not allowed for BEARER tokens
        // confirmationMethod.setNotBefore(now);
        confirmationMethod.setNotOnOrAfter(now.plusSeconds(properties.getTokenLifetimeInSeconds()));

        return confirmationMethod;
    }

    /**
     * Builds the SubjectConfirmation part of a token.
     * @param now - The time at which the token is generated
     * @return A populated SubjectConfirmation object
     */
    public SubjectConfirmation buildSubjectConfirmation(DateTime now) {
        SAMLObjectBuilder builder = getObjectBuilder(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        SubjectConfirmation subjectConfirmation = (SubjectConfirmation) builder.buildObject();
        subjectConfirmation.setSubjectConfirmationData(buildSubjectConfirmationData(now));

        return subjectConfirmation;
    }

    /**
     * Builds the Subject part of a token
     * @param user - The user identified by the bootstrap token
     * @param now - The time at which the token is generated
     * @return A populated Subject object
     * @throws Exception
     */
    public Subject buildSubject(DafoUserData user, DateTime now) throws Exception {
        SAMLObjectBuilder subjectBuilder = getObjectBuilder(Subject.DEFAULT_ELEMENT_NAME);

        Subject subject = (Subject) subjectBuilder.buildObject();

        subject.setNameID(buildNameId(user));
        subject.getSubjectConfirmations().add(buildSubjectConfirmation(now));

        return subject;
    }

    /**
     * Builds the AuthnStatement part of a token.
     * @param now - The time at which the token is generated
     * @return A populated AuthnStatement object
     * @throws Exception
     */
    public AuthnStatement buildAuthnStatement(DateTime now) throws Exception {
        SAMLObjectBuilder authStatementBuilder = getObjectBuilder(AuthnStatement.DEFAULT_ELEMENT_NAME);
        AuthnStatement authnStatement = (AuthnStatement) authStatementBuilder.buildObject();
        authnStatement.setAuthnInstant(now);

        authnStatement.setAuthnContext(buildAuthnContext());
        return authnStatement;
    }

    /**
     * Builds the AuthnContext part of a token.
     * @return A populated AuthnContext object
     * @throws Exception
     */
    public AuthnContext buildAuthnContext() throws Exception {
        SAMLObjectBuilder authContextBuilder = getObjectBuilder(AuthnContext.DEFAULT_ELEMENT_NAME);
        AuthnContext authnContext = (AuthnContext) authContextBuilder.buildObject();
        authnContext.setAuthnContextClassRef(buildAuthnContextClassRef());

        return authnContext;
    }


    /**
     * Builds the AuthnContextClassRef part of a token.
     * @return A populated AuthnContextClassRef object
     */
    public AuthnContextClassRef buildAuthnContextClassRef() {
        SAMLObjectBuilder authContextClassRefBuilder = getObjectBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        AuthnContextClassRef authnContextClassRef = (AuthnContextClassRef) authContextClassRefBuilder.buildObject();
        // DAFO tokens will always contain an unspecified identity, since we can identify subjects via many
        // different methods.
        authnContextClassRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified");

        return authnContextClassRef;
    }

    /**
     * Builds a representation of an Attribute for a token with a single value.
     * @param name - The claim name
     * @param value - The value of the claim
     * @return A populated Attribute object.
     */
    public Attribute buildStringAttribute(String name, String value) {
        ArrayList<String> values = new ArrayList<>();
        values.add(value);
        return buildStringAttribute(name, values);
    }

    /**
     * Builds a representation of an Attribute for a token with one or more values.
     * @param name - The claim name for the token
     * @param values - A list of values
     * @return A populated Attribute object.
     */
    public Attribute buildStringAttribute(String name, Collection<String> values) {
        SAMLObjectBuilder attrBuilder = getObjectBuilder(Attribute.DEFAULT_ELEMENT_NAME);
        Attribute attr = (Attribute) attrBuilder.buildObject();
        attr.setName(name);

        // Set custom Attributes
        for (String value : values) {
            XMLObjectBuilder stringBuilder = Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
            XSString attrValue = (XSString) stringBuilder.buildObject(
                    AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME
            );
            attrValue.setValue(value);
            attr.getAttributeValues().add(attrValue);
        }

        return attr;
    }

    public Attribute buildIntAttribute(String name, int value) {
        SAMLObjectBuilder attrBuilder = getObjectBuilder(Attribute.DEFAULT_ELEMENT_NAME);
        Attribute attr = (Attribute) attrBuilder.buildObject();
        attr.setName(name);
        XMLObjectBuilder valueBuilder = Configuration.getBuilderFactory().getBuilder(XSInteger.TYPE_NAME);
        XSInteger attrValue = (XSInteger) valueBuilder.buildObject(
                AttributeValue.DEFAULT_ELEMENT_NAME, XSInteger.TYPE_NAME
        );
        attrValue.setValue(value);
        attr.getAttributeValues().add(attrValue);

        return attr;
    }

    /**
     * Builds the AttributeStatement part of a token
     * @param user - The user identified by the bootstrap token.
     * @return A populated AttributeStatement object
     */
    public AttributeStatement buildAttributeStatement(int tokenId, DafoUserData user) {
        SAMLObjectBuilder attrStatementBuilder = getObjectBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
        AttributeStatement attrStatement = (AttributeStatement) attrStatementBuilder.buildObject();

        Attribute tokenIdAttribute = buildIntAttribute(TOKEN_ID_CLAIM_URL, tokenId);
        attrStatement.getAttributes().add(tokenIdAttribute);

        if(user != null) {
            Collection<String> userProfiles = user.getUserProfiles();
            if(userProfiles.size() > 0) {
                Attribute attrUserProfiles = buildStringAttribute(USERPROFILE_CLAIM_URL, userProfiles);
                attrStatement.getAttributes().add(attrUserProfiles);
            }
        }

        // Set on-behalf-of claim if the token is issued on behalf of a user behind a common login
        if(user.getOnBehalfOf() != null && !user.getOnBehalfOf().isEmpty()) {
            Attribute attrOnBehalfOf = buildStringAttribute(ON_BEHALF_OF_CLAIM_URL, user.getOnBehalfOf());
            attrStatement.getAttributes().add(attrOnBehalfOf);
        }

        return attrStatement;
    }

    /**
     * Builds the Conditions part of a token.
     * @param now - The time at which the token is generated
     * @return A populated Conditions object
     */
    public Conditions buildConditions(DateTime now) {
        SAMLObjectBuilder conditionsBuilder = getObjectBuilder(Conditions.DEFAULT_ELEMENT_NAME);
        Conditions conditions = (Conditions) conditionsBuilder.buildObject();

        conditions.setNotBefore(now);
        conditions.setNotOnOrAfter(now.plusSeconds(properties.getTokenLifetimeInSeconds()));

        conditions.getConditions().add(buildAudienceRestrictionCondition());

        return conditions;

    }

    /**
     * Builds an AudienceRestriction condition for a token.
     * @return A populated AudienceRestriction object
     */
    public AudienceRestriction buildAudienceRestrictionCondition() {
        SAMLObjectBuilder builder = getObjectBuilder(AudienceRestriction.DEFAULT_ELEMENT_NAME);
        AudienceRestriction condition = (AudienceRestriction) builder.buildObject();
        condition.getAudiences().add(buildAudience());

        return condition;
    }

    /**
     * Builds an Audience part of a token
     * @return A populated Audience object
     */
    public Audience buildAudience() {
        SAMLObjectBuilder builder = getObjectBuilder(Audience.DEFAULT_ELEMENT_NAME);
        Audience audience = (Audience) builder.buildObject();

        audience.setAudienceURI(audienceURL);

        return audience;
    }

    /**
     * Builds the Issuer part of a token
     * @return A populated Issuer object
     */
    public Issuer buildIssuser() {
        SAMLObjectBuilder issuerBuilder = getObjectBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        Issuer issuer = (Issuer) issuerBuilder.buildObject();
        issuer.setValue(issuerEntityID);

        return issuer;
    }

    /**
     * Builds a SAML2 Assertion
     * @param user - The user identified by the bootstrap token
     * @return A populated Assertion object
     * @throws Exception
     */
    public Assertion buildAssertion(DafoUserData user, HttpServletRequest request) throws Exception {
        int tokenId = INVALID_TOKEN_ID;
        if(request != null) {
            tokenId = databaseQueryManager.registerPendingToken(user, request);
        }

        SAMLObjectBuilder assertionBuilder = getObjectBuilder(Assertion.DEFAULT_ELEMENT_NAME);
        Assertion assertion = (Assertion) assertionBuilder.buildObject();
        assertion.setIssuer(buildIssuser());
        DateTime now = DateTime.now();
        assertion.setIssueInstant(now);
        assertion.setVersion(SAMLVersion.VERSION_20);

        assertion.getAuthnStatements().add(buildAuthnStatement(now));
        assertion.getAttributeStatements().add(buildAttributeStatement(tokenId, user));

        assertion.setSubject(buildSubject(user, now));

        assertion.setConditions(buildConditions(now));

        return assertion;
    }

    /**
     * Converts the given token to a deflated and encoded string, stores the generated token
     * in the database and return thes deflated and encoded string.
     * @param assertion - The Assertion to save
     * @return A String with deflated and encoded token data
     * @throws Exception
     */
    public String saveGeneratedToken(Assertion assertion) throws Exception {
        String deflatedAndEncodedToken = deflateAndEncode(getTokenXml(assertion));
        databaseQueryManager.updateRegisteredToken(assertion, deflatedAndEncodedToken);

        return deflatedAndEncodedToken;
    }

    /**
     * Signs the specified Assertion
     * @param assertion - The Assertion to sign
     * @throws Exception
     */
    public void signAssertion(Assertion assertion) throws Exception {
        SignatureBuilder builder = (SignatureBuilder) Configuration.getBuilderFactory().getBuilder(
                Signature.DEFAULT_ELEMENT_NAME
        );
        Signature signature = builder.buildObject(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        signature.setCanonicalizationAlgorithm("http://www.w3.org/2001/10/xml-exc-c14n#");
        signature.setSigningCredential(signingCredential);
        SecurityHelper.prepareSignatureParams(signature, signingCredential, null, null);
        assertion.setSignature(signature);
        Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);
    }


    /**
     * Convert an assertion to an XML string.
     * @param assertion the assertion to output as string
     * @return
     * @throws Exception
     */
    public String getTokenXml(Assertion assertion) throws Exception {
        ResponseMarshaller marshaller = new ResponseMarshaller();
        return XMLHelper.nodeToString(marshaller.marshall(assertion));
    }


    /**
     * Deflates and Base64-encodes the input string
     * @param input - The string to be deflated and Base64-encoded
     * @return - The deflated and encoded string
     * @throws Exception
     */
    public String deflateAndEncode(String input) throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        DeflaterOutputStream deflaterStream = new DeflaterOutputStream(bytesOut, deflater);
        deflaterStream.write(input.getBytes("UTF-8"));
        deflaterStream.finish();
        return Base64.encodeBytes(bytesOut.toByteArray(), Base64.DONT_BREAK_LINES);
    }

    /**
     * Base64 decodes and inflates the input string
     * @param input - A base64 and deflated string
     * @return decoded and inflated string
     * @throws MessageDecodingException
     */
    public String decodeAndInflate(String input) throws MessageDecodingException {
        byte[] decodedBytes = Base64.decode(input);
        if(decodedBytes == null){
            throw new MessageDecodingException("Unable to Base64 decode incoming message");
        }

        try {
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(decodedBytes);
            InflaterInputStream inflater = new InflaterInputStream(bytesIn, new Inflater(true));

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inflater.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();

            return new String(buffer.toByteArray(), "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
            throw new MessageDecodingException("Unable to Base64 decode and inflate SAML message", e);
        }
    }

    public String getMetadataXmlString() throws MarshallingException, CertificateEncodingException {
        EntityDescriptor entityDescriptor = buildMetadata();
        MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(entityDescriptor);
        return XMLHelper.nodeToString(marshaller.marshall(entityDescriptor));
    }

    public EntityDescriptor buildMetadata() throws CertificateEncodingException {
        EntityDescriptorBuilder entityDescriptorBuilder = getObjectBuilder(EntityDescriptor.DEFAULT_ELEMENT_NAME);
        EntityDescriptor entityDescriptor = entityDescriptorBuilder.buildObject();
        entityDescriptor.setEntityID(issuerEntityID);

        IDPSSODescriptorBuilder idpssoDescriptorBuilder = getObjectBuilder(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        IDPSSODescriptor idpssoDescriptor = idpssoDescriptorBuilder.buildObject();
        idpssoDescriptor.addSupportedProtocol("urn:oasis:names:tc:SAML:2.0:protocol");
        DateTime now = new DateTime();
        // TODO: Make this configurable?
        idpssoDescriptor.setValidUntil(now.plusMinutes(60 * 24));
        entityDescriptor.getRoleDescriptors().add(idpssoDescriptor);

        NameIDFormatBuilder nameIdFormatBuilder = getObjectBuilder(NameIDFormat.DEFAULT_ELEMENT_NAME);
        NameIDFormat nameIDFormat = nameIdFormatBuilder.buildObject();
        nameIDFormat.setFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
        idpssoDescriptor.getNameIDFormats().add(nameIDFormat);
        NameIDFormat nameIDFormat2 = nameIdFormatBuilder.buildObject();
        nameIDFormat2.setFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
        idpssoDescriptor.getNameIDFormats().add(nameIDFormat2);

        KeyDescriptorBuilder keyDescriptorBuilder = getObjectBuilder(KeyDescriptor.DEFAULT_ELEMENT_NAME);
        KeyDescriptor keyDescriptor = keyDescriptorBuilder.buildObject();
        keyDescriptor.setUse(UsageType.SIGNING);
        idpssoDescriptor.getKeyDescriptors().add(keyDescriptor);

        KeyInfoBuilder keyInfoBuilder = getObjectBuilder(KeyInfo.DEFAULT_ELEMENT_NAME);
        KeyInfo keyInfo = keyInfoBuilder.buildObject();
        keyDescriptor.setKeyInfo(keyInfo);

        X509DataBuilder x509Databuilder = getObjectBuilder(X509Data.DEFAULT_ELEMENT_NAME);
        X509Data x509Data = x509Databuilder.buildObject();
        keyInfo.getX509Datas().add(x509Data);

        X509CertificateBuilder x509CertificateBuilder = getObjectBuilder(
                org.opensaml.xml.signature.X509Certificate.DEFAULT_ELEMENT_NAME
        );
        org.opensaml.xml.signature.X509Certificate x509Certificate = x509CertificateBuilder.buildObject();

        x509Certificate.setValue(
                Base64.encodeBytes(signingCredential.getEntityCertificate().getEncoded())
        );

        x509Data.getX509Certificates().add(x509Certificate);

        return entityDescriptor;
    }


}
