package com.ecui.jshttps.demo.controller;

import com.ecui.jshttps.demo.controller.common.EncryptedRequest;
import com.ecui.jshttps.demo.controller.common.PersonalInfoRequest;
import com.ecui.jshttps.demo.controller.common.SuccessMsgResponse;
import com.ecui.jshttps.demo.utils.HttpCryptoHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping(path = "/api")
public class DemoController {
    @PostMapping(path = "/demo")
    public String doJsHttpsTest(@RequestBody EncryptedRequest encryptedRequest) {
        HttpCryptoHelper helper = new HttpCryptoHelper();

        // fetch your RSA private key corresponding to the public key used
        // at frontend in js-https
        String privateKey;
        try {
            privateKey = Files.readString(Path.of("./keys/private.pem"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // decrypt and get the actual request object
        PersonalInfoRequest request = helper.decryptRequestBody(
                encryptedRequest.getBodyCipher(),
                encryptedRequest.getKsCipher(),
                encryptedRequest.getIvCipher(),
                privateKey,
                new TypeReference<>() {
                }
        );

        if(request==null){
            System.out.println("Decryption failed and returned null");
            return helper.encryptResponseBody(
                    new SuccessMsgResponse("Server decryption failed"));
        }

        System.out.println(request.getName());
        System.out.println(request.getAge());
        System.out.println(request.getInterests());

        // Do something in your business service...

        // Pass the response object and return the cipher string directly
        return helper.encryptResponseBody(new SuccessMsgResponse());
    }
}
