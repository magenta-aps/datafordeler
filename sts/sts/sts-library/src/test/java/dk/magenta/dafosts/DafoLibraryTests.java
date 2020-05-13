package dk.magenta.dafosts;

import dk.magenta.dafosts.library.DafoTokenGenerator;
import dk.magenta.dafosts.library.SharedConfig;
import dk.magenta.dafosts.library.TokenGeneratorProperties;
import dk.magenta.dafosts.library.users.DafoUserData;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static dk.magenta.dafosts.library.DatabaseQueryManager.INVALID_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

public class DafoLibraryTests {

    @Test
    public void testTokenGeneratorProperties() {
        TokenGeneratorProperties properties = new TokenGeneratorProperties();

        assertThat(properties.getPrivateKeyDerLocation()).isNotEmpty();
        assertThat(properties.getPublicKeyPemLocation()).isNotEmpty();
    }

    @Test
    public void testSharedMvcConfig() {
        SharedConfig sharedConfig = new SharedConfig();
        MockViewControllerRegistry viewControllerRegistry = new MockViewControllerRegistry();
        sharedConfig.addViewControllers(viewControllerRegistry);
        assertThat(viewControllerRegistry.getMappings().contains("/")).isTrue();
    }

    @Test
    public void testTokenGenerator() throws Exception {
        // Boostrap the Opensaml Library to use default configuration
        org.opensaml.DefaultBootstrap.bootstrap();

        DafoTokenGenerator generator = new DafoTokenGenerator(new TokenGeneratorProperties());

        generator.buildAssertion(new DafoUserData() {
            @Override
            public String getUsername() {
                return "testuser";
            }

            @Override
            public Collection<String> getUserProfiles() {
                return Arrays.asList(new String[] {"UserProfile1", "UserProfile2"});
            }

            @Override
            public int getAccessAccountId() {
                return 42;
            }

            @Override
            public String getOnBehalfOf() {
                return null;
            }

            @Override
            public String getNameQualifier() {
                return "<none>";
            }
        }, null);
    }

}
