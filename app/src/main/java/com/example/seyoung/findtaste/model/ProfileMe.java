package com.example.seyoung.findtaste.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by seyoung on 2017-12-16.
 * 현재 사용자의 아이디와 닉네임, 사진을 서버에서 받아와 담을 클래스입니다.
 * 마이페이지에서 사용합니다.
 */

public class ProfileMe {
    @SerializedName("userid")
    @Expose
    private String userid;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("profilepic")
    @Expose
    private String profilepic;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

}
