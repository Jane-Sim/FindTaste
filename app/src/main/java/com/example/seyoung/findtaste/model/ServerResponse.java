package com.example.seyoung.findtaste.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by seyoung on 2017-10-28.
 * 현재는 사용하지 않습니다.
 */

public class ServerResponse {

    // variable name should be same as in the json response from php
    @SerializedName("success")
    public  boolean success;
    @SerializedName("message")
    public  String message;

    public  String getMessage() {
        return message;
    }

   public boolean getSuccess() {
        return success;
    }

}