package com.ecui.jshttps.demo.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class CryptoHelper {
    private byte[] aesOperate(int opMode,byte[] data,byte[] key,byte[] iv){
        Cipher aesCipher;

        try {
            aesCipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }

        SecretKeySpec secretKeySpec=new SecretKeySpec(key,"AES");
        IvParameterSpec ivParameterSpec=new IvParameterSpec(iv);

        try {
            aesCipher.init(opMode,secretKeySpec,ivParameterSpec);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return null;
        }

        try {
            return aesCipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String aesDecrypt(byte[] cipher,byte[] key,byte[] iv){
        byte[] decryptedBytes=this.aesOperate(
                Cipher.DECRYPT_MODE,
                cipher,
                key,
                iv
        );

        if(decryptedBytes==null){
            return null;
        }
        else{
            return new String(decryptedBytes).trim();
        }
    }

    public String aesEncrypt(byte[] plainText,byte[] key,byte[] iv){
        byte[] decryptedBytes=this.aesOperate(
                Cipher.ENCRYPT_MODE,
                plainText,
                key,
                iv
        );

        if(decryptedBytes==null){
            return null;
        }
        else{
            return Base64.getEncoder().encodeToString(decryptedBytes);
        }
    }

    public String rsaDecrypt(byte[] cipher,byte[] privateKey){
        Cipher aesCipher;
        KeyFactory keyFactory;

        try {
            aesCipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
            keyFactory=KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }

        PKCS8EncodedKeySpec pkcs8EncodedKeySpec=new PKCS8EncodedKeySpec(privateKey);
        PrivateKey key;

        try {
            key = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }

        try {
            aesCipher.init(Cipher.DECRYPT_MODE,key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }

        try {
            return new String(aesCipher.doFinal(cipher)).trim();
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
