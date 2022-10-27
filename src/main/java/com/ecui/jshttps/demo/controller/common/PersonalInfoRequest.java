package com.ecui.jshttps.demo.controller.common;

import lombok.Data;

import java.util.List;

@Data
public class PersonalInfoRequest {
    private String name;
    private Integer age;
    private List<String> interests;
}
