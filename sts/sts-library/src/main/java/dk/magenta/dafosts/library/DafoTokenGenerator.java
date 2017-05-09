package dk.magenta.dafosts.library;

import dk.magenta.dafosts.library.users.DafoUserData;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.ResponseMarshaller;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@EnableConfigurationProperties(TokenGeneratorProperties.class)
public class DafoTokenGenerator {

    TokenGeneratorProperties properties;

    BasicX509Credential signingCredential;

    public DafoTokenGenerator(TokenGeneratorProperties properties) throws Exception {
        this.properties = properties;
        this.signingCredential = new BasicX509Credential();
        CertificateFactory fact = CertificateFactory.getInstance("X.509");

        String publicKeyLocation = properties.getPublicKeyPemLocation();
        String publicKeyPath = publicKeyLocation.substring(publicKeyLocation.indexOf(":") + 1);
        ClassPathResource publibKeyResource = new ClassPathResource(publicKeyPath);
        InputStream is = publibKeyResource.getInputStream();
        X509Certificate cert = (X509Certificate) fact.generateCertificate(is);
        this.signingCredential.setEntityCertificate(cert);

        String privateKeyLocation = properties.getPrivateKeyDerLocation();
        String privateKeyPath = privateKeyLocation.substring(privateKeyLocation.indexOf(":") + 1);
        ClassPathResource privateKeyResource = new ClassPathResource(privateKeyPath);
        InputStream privateKeyInputStream = privateKeyResource.getInputStream();
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

    /**
     * Shortcut method for accessing SamlObjectBuilder
     * @param qName - Specifies the type of builder to get, typically specified as SamlClass.DEFAULT_ELEMENT_NAME
     * @return
     */
    public SAMLObjectBuilder getObjectBuilder(QName qName) {
        return (SAMLObjectBuilder) Configuration.getBuilderFactory().getBuilder(qName);
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
        // TODO: This should uniquely identify the original IdP.
        nameId.setNameQualifier("NameIdQualifier");
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
        confirmationMethod.setNotBefore(now);
        confirmationMethod.setNotOnOrAfter(now.plusMinutes(10));

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
        // TODO: Copy this from the original token?
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

    /**
     * Builds the AttributeStatement part of a token
     * @param user - The user identified by the bootstrap token.
     * @return A populated AttributeStatement object
     */
    public AttributeStatement buildAttributeStatement(DafoUserData user) {
        SAMLObjectBuilder attrStatementBuilder = getObjectBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
        AttributeStatement attrStatement = (AttributeStatement) attrStatementBuilder.buildObject();


        Attribute attrUserProfiles = buildStringAttribute(
                "https://data.gl/claims/userprofile", user.getUserProfiles()
        );
        attrStatement.getAttributes().add(attrUserProfiles);

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
        conditions.setNotOnOrAfter(now.plusMinutes(10));

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

        audience.setAudienceURI("TODO:audience-URI");

        return audience;
    }

    /**
     * Builds the Issuer part of a token
     * @return A populated Issuer object
     */
    public Issuer buildIssuser() {
        SAMLObjectBuilder issuerBuilder = getObjectBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        Issuer issuer = (Issuer) issuerBuilder.buildObject();
        issuer.setValue("Dafo-STS");

        return issuer;
    }

    /**
     * Builds a SAML2 Assertion
     * @param user - The user identified by the bootstrap token
     * @return A populated Assertion object
     * @throws Exception
     */
    public Assertion buildAssertion(DafoUserData user) throws Exception {

        SAMLObjectBuilder assertionBuilder = getObjectBuilder(Assertion.DEFAULT_ELEMENT_NAME);
        Assertion assertion = (Assertion) assertionBuilder.buildObject();
        assertion.setIssuer(buildIssuser());
        DateTime now = DateTime.now();
        assertion.setIssueInstant(now);
        assertion.setVersion(SAMLVersion.VERSION_20);

        assertion.getAuthnStatements().add(buildAuthnStatement(now));
        assertion.getAttributeStatements().add(buildAttributeStatement(user));

        assertion.setSubject(buildSubject(user, now));

        assertion.setConditions(buildConditions(now));

        return assertion;
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
     * Generates the XML for a signed SAML2 Assertion
     * @param user - The user identified by the bootstrap token
     * @return A valid signed SAML2 assertion as an XML string
     * @throws Exception
     */
    public String getTokenXml(DafoUserData user) throws Exception {
        Assertion assertion = buildAssertion(user);
        signAssertion(assertion);

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


}
