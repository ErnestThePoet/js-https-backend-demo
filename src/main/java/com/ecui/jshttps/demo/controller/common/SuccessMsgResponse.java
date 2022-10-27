package com.ecui.jshttps.demo.controller.common;

import lombok.Data;

@Data
public class SuccessMsgResponse {
    private Boolean success;
    private String msg;

    public SuccessMsgResponse(){
        this.success=true;
        this.msg="";
    }

    public SuccessMsgResponse(String msg){
        this.success=false;
        this.msg = msg;
    }
}
