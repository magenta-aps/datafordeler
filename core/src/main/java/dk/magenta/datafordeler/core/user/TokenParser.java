package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Handles parsing of incoming SAML tokens.
 */
@Component
public class TokenParser {
    public Assertion parseAssertion(String fromString) throws InvalidTokenException {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(fromString);
            if (decodedBytes == null) {
                throw new MessageDecodingException("Unable to Base64 decode incoming message");
            }

            ByteArrayInputStream bytesIn = new ByteArrayInputStream(decodedBytes);
            InflaterInputStream in = new InflaterInputStream(bytesIn, new Inflater(true));

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();

            Document document = docBuilder.parse(in);
            Element element = document.getDocumentElement();

            UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);

            return (Assertion) unmarshaller.unmarshall(element);
        } catch (IOException | ParserConfigurationException | SAXException | UnmarshallingException |
                 MessageDecodingException e) {
            throw new InvalidTokenException("Could not parse authorization token", e);
        }
    }
}
