package com.ecui.jshttps.demo.controller.common;

import lombok.Data;

@Data
public class EncryptedRequest {
    private String bodyCipher;
    private String ksCipher;
    private String ivCipher;
}
