package dk.magenta.dafosts.controller;

/**
 * @author Ulises Bocchio
 */

import com.github.ulisesbocchio.spring.boot.security.saml.annotation.SAMLUser;
import com.github.ulisesbocchio.spring.boot.security.saml.user.SAMLUserDetails;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.Writer;

@Controller
public class GetTokenController {

    public SAMLObjectBuilder getObjectBuilder(QName qName) {
        return (SAMLObjectBuilder) Configuration.getBuilderFactory().getBuilder(qName);
    }


    public NameID buildNameId(SAMLUserDetails user) throws Exception {
        SAMLObjectBuilder builder = getObjectBuilder(NameID.DEFAULT_ELEMENT_NAME);

        NameID nameId = (NameID) builder.buildObject();
        nameId.setValue(user.getUsername());
        // TODO: This should uniquely identify the original IdP.
        nameId.setNameQualifier("NameIdQualifier");
        nameId.setFormat(NameID.UNSPECIFIED);

        return nameId;
    }

    public SubjectConfirmationData buildSubjectConfirmationData(DateTime now) {
        // Create the SubjectConfirmation
        SAMLObjectBuilder builder = getObjectBuilder(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);

        SubjectConfirmationData confirmationMethod = (SubjectConfirmationData) builder.buildObject();
        confirmationMethod.setNotBefore(now);
        confirmationMethod.setNotOnOrAfter(now.plusMinutes(10));

        return confirmationMethod;
    }

    public SubjectConfirmation buildSubjectConfirmation(DateTime now) {
        SAMLObjectBuilder builder = getObjectBuilder(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        SubjectConfirmation subjectConfirmation = (SubjectConfirmation) builder.buildObject();
        subjectConfirmation.setSubjectConfirmationData(buildSubjectConfirmationData(now));

        return subjectConfirmation;
    }

    public Subject buildSubject(SAMLUserDetails user, DateTime now) throws Exception {
        SAMLObjectBuilder subjectBuilder = getObjectBuilder(Subject.DEFAULT_ELEMENT_NAME);

        Subject subject = (Subject) subjectBuilder.buildObject();

        subject.setNameID(buildNameId(user));
        subject.getSubjectConfirmations().add(buildSubjectConfirmation(now));

        return subject;
    }

    public AuthnStatement buildAuthnStatement(DateTime now) throws Exception {
        SAMLObjectBuilder authStatementBuilder = getObjectBuilder(AuthnStatement.DEFAULT_ELEMENT_NAME);
        AuthnStatement authnStatement = (AuthnStatement) authStatementBuilder.buildObject();
        authnStatement.setAuthnInstant(now);

        authnStatement.setAuthnContext(buildAuthnContext());
        return authnStatement;
    }

    public AuthnContext buildAuthnContext() throws Exception {
        SAMLObjectBuilder authContextBuilder = getObjectBuilder(AuthnContext.DEFAULT_ELEMENT_NAME);
        AuthnContext authnContext = (AuthnContext) authContextBuilder.buildObject();
        authnContext.setAuthnContextClassRef(buildAuthnContextClassRef());

        return authnContext;
    }


    public AuthnContextClassRef buildAuthnContextClassRef() {
        SAMLObjectBuilder authContextClassRefBuilder = getObjectBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        AuthnContextClassRef authnContextClassRef = (AuthnContextClassRef) authContextClassRefBuilder.buildObject();
        // TODO: Copy this from the original token?
        authnContextClassRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified");

        return authnContextClassRef;
    }

    public AttributeStatement buildAttributeStatement(SAMLUserDetails user) {
        SAMLObjectBuilder attrStatementBuilder = getObjectBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
        AttributeStatement attrStatement = (AttributeStatement) attrStatementBuilder.buildObject();

        // Create the attribute statement
        /*
        Map attributes = input.getAttributes();
        if(attributes != null){
            Set keySet = attributes.keySet();
            for (String key : keySet)
            {
                Attribute attrFirstName = buildStringAttribute(key, attributes.get(key), getSAMLBuilder());
                attrStatement.getAttributes().add(attrFirstName);
            }
        }
        */

        return attrStatement;
    }

    public Conditions buildConditions(DateTime now) {
        SAMLObjectBuilder conditionsBuilder = getObjectBuilder(Conditions.DEFAULT_ELEMENT_NAME);
        Conditions conditions = (Conditions) conditionsBuilder.buildObject();

        conditions.setNotBefore(now);
        conditions.setNotOnOrAfter(now.plusMinutes(10));

        conditions.getConditions().add(buildAudienceRestrictionCondition());

        return conditions;

    }

    public Condition buildAudienceRestrictionCondition() {
        SAMLObjectBuilder builder = getObjectBuilder(AudienceRestriction.DEFAULT_ELEMENT_NAME);
        AudienceRestriction condition = (AudienceRestriction) builder.buildObject();
        condition.getAudiences().add(buildAudience());

        return condition;
    }

    public Audience buildAudience() {
        SAMLObjectBuilder builder = getObjectBuilder(Audience.DEFAULT_ELEMENT_NAME);
        Audience audience = (Audience) builder.buildObject();

        audience.setAudienceURI("TODO:audience-URI");

        return audience;
    }

    public Issuer buildIssuser() {
        SAMLObjectBuilder issuerBuilder = getObjectBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        Issuer issuer = (Issuer) issuerBuilder.buildObject();
        issuer.setValue("Dafo-STS");

        return issuer;
    }

    public Assertion buildAssertion(SAMLUserDetails user) throws Exception {

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

    public String getTokenXml(SAMLUserDetails user) throws Exception {
        Assertion assertion = buildAssertion(user);

        MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(assertion);

        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(marshaller.marshall(assertion)), new StreamResult(out));


        return out.toString(); // XMLHelper.nodeToString();
    }


    @RequestMapping("/get_token")
    public ModelAndView get_token(@SAMLUser SAMLUserDetails user) throws Exception {

        ModelAndView getTokenView = new ModelAndView("get_token");
        getTokenView.addObject("userId", user.getUsername());
        getTokenView.addObject("samlAttributes", user.getAttributes());
        getTokenView.addObject("token_xml", getTokenXml(user));

        // TODO: Sign token
        // TODO: Look into SessionIndex on AuthnStatement - might have to be copied from original token

        return getTokenView;
    }

}
