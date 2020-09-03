package dk.magenta.datafordeler.core.util;

import com.google.crypto.tink.*;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadFactory;
import com.google.crypto.tink.aead.AeadKeyTemplates;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public abstract class Encryption {

    static {
        try {
            AeadConfig.register();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    private static KeysetHandle getKeysetHandle(File keyFile) throws GeneralSecurityException, IOException {
        if (keyFile.exists()) {
            return CleartextKeysetHandle.read(JsonKeysetReader.withFile(keyFile));
        }
        KeysetHandle handle = KeysetHandle.generateNew(AeadKeyTemplates.AES128_GCM);
        CleartextKeysetHandle.write(handle, JsonKeysetWriter.withFile(keyFile));
        return handle;
    }

    public static byte[] encrypt(File keyFile, String plaintext) throws GeneralSecurityException, IOException {
        if (plaintext == null) {
            return null;
        }
        KeysetHandle handle = getKeysetHandle(keyFile);
        Aead aead = AeadFactory.getPrimitive(handle);
        return aead.encrypt(plaintext.getBytes(), new byte[0]);
    }

    public static String decrypt(File keyFile, byte[] ciphertext) throws GeneralSecurityException, IOException {
        if (ciphertext == null) {
            return null;
        }
        KeysetHandle handle = getKeysetHandle(keyFile);
        Aead aead = AeadFactory.getPrimitive(handle);
        byte[] plaintext = aead.decrypt(ciphertext, new byte[0]);
        return new String(plaintext);
    }

}
