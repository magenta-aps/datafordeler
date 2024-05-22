package dk.magenta.datafordeler.core.user;

import org.opensaml.Configuration;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.Resource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.*;
import java.net.URISyntaxException;

@org.springframework.context.annotation.Configuration
public class SamlMetadataConfiguration {

    @Autowired
    TokenConfigProperties config;

    @Bean
    public MetadataProvider samlMetadataProvider()
            throws ResourceException, MetadataProviderException, URISyntaxException, IOException {
        String path = config.getIssuerMetadataPath();
        File metadataFile;
        if (path == null) {
            // Copy the classpath resource sts metadata to a temporary file
            Resource resource = new ClasspathResource(
                    "/dk/magenta/datafordeler/core/user/sts_metadata.xml"
            );
            metadataFile = File.createTempFile("sts_metadata", "xml");
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = resource.getInputStream();
                outputStream = new FileOutputStream(metadataFile);

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            metadataFile = new File(path);
        }
        FilesystemMetadataProvider provider = new FilesystemMetadataProvider(metadataFile);
        provider.setRequireValidMetadata(false);
        provider.setParserPool(new BasicParserPool());
        provider.initialize();
        return provider;
    }

    @Bean
    public ExplicitKeySignatureTrustEngine trustEngine(MetadataProvider metadataProvider) {
        MetadataProvider mdProvider = metadataProvider;
        MetadataCredentialResolver mdCredResolver = new MetadataCredentialResolver(mdProvider);
        KeyInfoCredentialResolver keyInfoCredResolver =
                Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver();
        return new ExplicitKeySignatureTrustEngine(mdCredResolver, keyInfoCredResolver);
    }


}
