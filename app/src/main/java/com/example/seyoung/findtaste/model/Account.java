package com.example.seyoung.findtaste.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by seyoung on 2017-10-13.
 * 쓰려고 했지만 현재 사용하지 않는 크래스
 */

public class Account implements Serializable {
    @SerializedName("username")
    private String userName;
    @SerializedName("password")
    private String password;
    @SerializedName("email")
    private String email;
    @SerializedName("image")
    private String image;

    public Account() {
    }

    public Account(String userName, String password, String email, String image) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.image = image;
    }

    public Account(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }


//당신이 직접 추가 한 게터 세터
}