package dk.magenta.datafordeler.core.fapi;

import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Deprecated
public class SoapHandler implements SOAPHandler<SOAPMessageContext> {

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        // Handle outgoing messages
        if ((boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
            try {
                SOAPEnvelope msg = context.getMessage().getSOAPPart().getEnvelope();
                msg.addNamespaceDeclaration("xs", "http://www.w3.org/2001/XMLSchema");
                msg.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
                NodeList valueTags = msg.getElementsByTagName("value");
                for (int i=0; i<valueTags.getLength(); i++) {
                    valueTags.item(i).getAttributes().removeNamedItem("xmlns:xs");
                    valueTags.item(i).getAttributes().removeNamedItem("xmlns:xsi");
                }
            } catch (SOAPException ex) {
                Logger.getLogger(SOAPHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }

    @Override
    public void close(MessageContext context) {
    }
}
