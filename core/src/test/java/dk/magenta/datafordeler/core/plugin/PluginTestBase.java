package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.StringJoiner;

public abstract class PluginTestBase {

    @Autowired
    protected DemoPlugin plugin;


    protected String getPayload(String resourceName) throws IOException {
        return IOUtils.toString(PluginTestBase.class.getResourceAsStream(resourceName), StandardCharsets.UTF_8);
    }

    protected String envelopReference(String schema, String reference) throws IOException {
        String template = IOUtils.toString(PluginTestBase.class.getResourceAsStream("/referenceenvelope.json"), StandardCharsets.UTF_8);
        return template.replace("%{skema}", schema).replace("%{reference}", reference.replace("\"", "\\\""));
    }

    protected String hash(String input) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            return DatatypeConverter.printHexBinary(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String jsonList(List<String> jsonData, String listKey) {
        StringJoiner sj = new StringJoiner(",");
        for (String j : jsonData) {
            sj.add(j);
        }
        return "{\""+listKey+"\":["+sj.toString()+"]}";
    }

}
