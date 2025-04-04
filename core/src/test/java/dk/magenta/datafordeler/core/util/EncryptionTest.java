package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;

@ContextConfiguration(classes = Application.class)
public class EncryptionTest {

    @Test
    public void testEncryptDecrypt() throws Exception {
        File keyFile = File.createTempFile("encryptiontest", ".json");
        try {
            keyFile.delete();
            String plaintext = "Very secret text";
            byte[] ciphertext = Encryption.encrypt(keyFile, plaintext);
            Assertions.assertEquals(plaintext, Encryption.decrypt(keyFile, ciphertext));
        } finally {
            keyFile.delete();
        }
    }
}
