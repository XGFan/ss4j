package com.test4x.ss4j.common.aes;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

public class AesGCM {

    // AES-GCM parameters
    public static final int AES_KEY_SIZE = 128; // in bits
    public static final int GCM_NONCE_LENGTH = 12; // in bytes
    public static final int GCM_TAG_LENGTH = 16; // in bytes


    byte[] aad = "ss-subkey".getBytes();
    //byte[] nonce = new byte[GCM_NONCE_LENGTH];
    SecretKey key;


    public AesGCM(SecretKey secretKey) throws Exception {
        if (secretKey == null) {
            SecureRandom random = SecureRandom.getInstanceStrong();
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(AES_KEY_SIZE, random);
            this.key = keyGen.generateKey();
        } else {
            this.key = secretKey;
        }
    }


    public byte[] encrypt(byte[] plainText, byte[] nonce) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "SunJCE");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        cipher.updateAAD(aad);
        return cipher.doFinal(plainText);
    }

    public byte[] decrypt(byte[] cipherText, byte[] nonce) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "SunJCE");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        cipher.updateAAD(aad);
        return cipher.doFinal(cipherText);
    }


    public static void main(String[] args) throws GeneralSecurityException, IOException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE, random);
        final byte[] encoded = keyGen.generateKey().getEncoded();
        System.out.println(Arrays.toString(encoded));
        System.out.println(new String(encoded));

        final Path secret = Paths.get("secret");
        Files.write(secret, encoded);


        final byte[] bytes = Files.readAllBytes(secret);
        SecretKey originalKey = new SecretKeySpec(bytes, 0, bytes.length, "AES");
        System.out.println(Arrays.toString(originalKey.getEncoded()));
    }
}
