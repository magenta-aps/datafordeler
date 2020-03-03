package dk.magenta.dafosts.saml.users;

import com.github.ulisesbocchio.spring.boot.security.saml.bean.SAMLConfigurerBean;
import dk.magenta.dafosts.saml.config.DafoWebSSOProfileConsumer;
import dk.magenta.dafosts.saml.config.SamlWebSecurityConfig;
import dk.magenta.dafosts.saml.metadata.DafoCachingMetadataManager;
import dk.magenta.dafosts.saml.metadata.DafoMetadataProvider;
import org.opensaml.common.SAMLException;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@Component
public class DafoAssertionVerifier {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private long maxAuthenticationAge = 7200;


    @Autowired
    SamlWebSecurityConfig samlWebSecurityConfig;

    @Autowired
    SAMLConfigurerBean samlConfigurerBean;

    @Autowired
    DafoCachingMetadataManager metadataManager;

    public Assertion parseAssertion(String fromString) {
        try {
            byte[] decodedBytes = Base64.decode(fromString);

            if(decodedBytes == null){
                throw new MessageDecodingException("Unable to Base64 decode incoming message");
            }

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(decodedBytes);


            Document document;
            // If input starts with < it is probably raw XML and does not need to be inflated
            if(decodedBytes[0] == '<') {
                document = docBuilder.parse(bytesIn);
            } else {
                Inflater inflater = new Inflater(true);
                InflaterInputStream in = new InflaterInputStream(bytesIn, inflater);
                document = docBuilder.parse(in);
            }
            Element element = document.getDocumentElement();

            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);

            XMLObject result = unmarshaller.unmarshall(element);
            if(result instanceof Assertion) {
                return (Assertion) result;
            } else if(result instanceof Response) {
                Response response = (Response) result;
                return response.getAssertions().get(0);
            }
        }
        catch (IOException|ParserConfigurationException|SAXException|UnmarshallingException|
                MessageDecodingException e) {
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
            context = samlConfigurerBean.serviceProvider().getSharedObject(
                    SAMLContextProvider.class
            ).getLocalEntity(request, response);
        }
        catch(MetadataProviderException e) {
            e.printStackTrace();
            return null;
        }

        String remoteEntityID = assertion.getIssuer().getValue();
        try {
            DafoMetadataProvider dafoMetadataProvider = metadataManager.getDafoMetadataProvider(remoteEntityID);
            context.setPeerEntityMetadata(dafoMetadataProvider.getEntityDescriptor(remoteEntityID));
            context.setPeerExtendedMetadata(metadataManager.getExtendedMetadata(remoteEntityID));
        }
        catch(MetadataProviderException e) {
            e.printStackTrace();
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
}
