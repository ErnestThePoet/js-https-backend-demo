package com.ecui.jshttps.demo.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HttpCryptoHelper {
    private final CryptoHelper cryptoHelper;
    private final ObjectMapper objectMapper;
    private byte[] aesIvBytes;
    private byte[] aesKeyBytes;

    public HttpCryptoHelper(){
        this.cryptoHelper=new CryptoHelper();
        this.objectMapper=new ObjectMapper();
        this.objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    


    private String getPureRsaPrivateKeyBase64(String fullKey){
        return fullKey.replace("BEGIN PRIVATE KEY","")
                .replace("END PRIVATE KEY","")
                .replace("-","")
                .replace("\r","")
                .replace("\n","");
    }

    /**
     * Decrypt js-https cipher data received from browser.
     *
     * @param   bodyCipher
     *          the bodyCipher property in request data object
     *
     * @param   ksCipher
     *          the ksCipher property in request data object
     *
     * @param   ivCipher
     *          the ivCipher property in request data object
     *
     * @param   rsaPrivateKey
     *          RSA pem encoded private key string corresponding to the public key
     *          used at frontend in js-https.
     *          The header {@code -----BEGIN PRIVATE KEY-----}
     *          and footer {@code -----END PRIVATE KEY-----}
     *          are optional.
     *          The line separators at the end of each line are also optional.
     *
     * @param   type
     *          a TypeReference instance for the decrypted object type.
     *          i.e, if the decrypted JSON string maps to a {@code MyClass} type,
     *          then pass {@code new TypeReference<MyClass>() {}} as this parameter.
     *
     * @return  An object with the given {@code type} type, or {@code null} if
     * errors encountered in decryption or JSON deserialization process.
     */
    public <T> T decryptRequestBody(
            String bodyCipher,
            String ksCipher,
            String ivCipher,
            String rsaPrivateKey,
            TypeReference<T> type
    ){
        var bodyCipherBytes= Base64.getDecoder().decode(bodyCipher);
        var ksCipherBytes= Base64.getDecoder().decode(ksCipher);
        var ivCipherBytes= Base64.getDecoder().decode(ivCipher);

        String aesKey=this.cryptoHelper.rsaDecrypt(
                ksCipherBytes,
                Base64.getDecoder().decode(
                        this.getPureRsaPrivateKeyBase64(rsaPrivateKey)
                                .getBytes(StandardCharsets.UTF_8)));

        if(aesKey==null){
            return null;
        }

        String aesIv=this.cryptoHelper.rsaDecrypt(
                ivCipherBytes,
                Base64.getDecoder().decode(
                        this.getPureRsaPrivateKeyBase64(rsaPrivateKey)
                                .getBytes(StandardCharsets.UTF_8)));

        if(aesIv==null){
            return null;
        }

        // save AES key and IV for encryption
        this.aesKeyBytes=aesKey.getBytes(StandardCharsets.UTF_8);
        this.aesIvBytes=aesIv.getBytes(StandardCharsets.UTF_8);

        String decryptedString=this.cryptoHelper.aesDecrypt(
                bodyCipherBytes,
                this.aesKeyBytes,
                this.aesIvBytes);

        try {
            return this.objectMapper.readValue(decryptedString,type);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println(decryptedString);
            return null;
        }
    }

    /**
     * Encrypt response data. Note that you must call {@code decryptRequestBody} first before
     * calling this method to acquire the symmetric key.
     *
     * @param   responseObj
     *          The response object
     *
     * @return  Base64-encoded response ciphertext encrypted with the same symmetric key
     * used in js-https request encryption, or {@code null} if errors encountered
     * during the encryption progress.
     */
    public String encryptResponseBody(Object responseObj){
        if(this.aesKeyBytes==null||this.aesIvBytes==null){
            throw new RuntimeException(
                    "A call to HttpCryptoHelper.decryptRequestBody must be " +
                            "performed prior to response encryption");
        }

        try {
            return this.cryptoHelper.aesEncrypt(
                    objectMapper.writeValueAsBytes(responseObj),
                    this.aesKeyBytes,
                    this.aesIvBytes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
