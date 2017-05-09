package dk.magenta.dafosts.saml.users;

import com.github.ulisesbocchio.spring.boot.security.saml.bean.SAMLConfigurerBean;
import dk.magenta.dafosts.saml.config.DafoWebSSOProfileConsumer;
import dk.magenta.dafosts.saml.config.SamlWebSecurityConfig;
import org.opensaml.common.SAMLException;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.Configuration;
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
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@Component
public class DafoAssertionVerifier {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private long maxAuthenticationAge = 7200;


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


            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
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
}
