import com.google.crypto.tink.*;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadFactory;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.proto.KeyTemplate;
import com.test4x.ss4j.common.TinkConf;

import java.io.File;
import java.security.GeneralSecurityException;

public class Test {


    public static void main(String[] args) throws Exception {

//        TinkConf.INSTANCE.createNewStreamKey();

//        Config.register(AeadConfig.TINK_1_1_0);
//        KeyTemplate keyTemplate = AeadKeyTemplates.AES128_GCM;
//        KeysetHandle keysetHandle = KeysetHandle.generateNew(keyTemplate);
//
//        String keysetFilename = "my_keyset.json";
//        CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(
//                new File(keysetFilename)));

//        // 2. Get the primitive.
//        Aead aead = AeadFactory.getPrimitive(keysetHandle);
//
//        // 3. Use the primitive to encrypt a plaintext,
//        byte[] bytes = "xxx".getBytes();
//        byte[] ciphertext = aead.encrypt("IamXXX".getBytes(), bytes);
//
//        // ... or to decrypt a ciphertext.
//        byte[] decrypted = aead.decrypt(ciphertext, bytes);
//        System.out.println( new String(decrypted));




    }

}