package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.Application;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class EncryptionTest {

    @Test
    public void testEncryptDecrypt() throws Exception {
        File keyFile = File.createTempFile("encryptiontest", ".json");
        try {
            keyFile.delete();
            String plaintext = "Very secret text";
            byte[] ciphertext = Encryption.encrypt(keyFile, plaintext);
            Assert.assertEquals(plaintext, Encryption.decrypt(keyFile, ciphertext));
        } finally {
            keyFile.delete();
        }
    }
}
