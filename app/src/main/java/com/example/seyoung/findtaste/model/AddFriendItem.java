package com.example.seyoung.findtaste.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by seyoung on 2017-12-23.
 * 서버에서 해당 유저가 검색한 친구목록을 받아왔을 때 데이터를 넣을 클래스입니다.
 * 친구 아이디와 닉네임, 사진과 추가를 했는 지 안했는 지 유무를 받아옵니다.
 */

public class AddFriendItem  {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("friend")
    @Expose
    private String friend;
    @SerializedName("friendname")
    @Expose
    private String friendname;
    @SerializedName("friend_image")
    @Expose
    private String friend_image;
    @SerializedName("fbadd")
    @Expose
    private Integer fbadd;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }

    public String getFriendImage() {
        return friend_image;
    }

    public void setFriendImage(String friendImage) {
        this.friend_image = friendImage;
    }

    public Integer getFbadd() {
        return fbadd;
    }

    public void setFbadd(Integer fbadd) {
        this.fbadd = fbadd;
    }
    public String getFriendname() {
        return friendname;
    }

    public void setFriendname(String friendname) {
        this.friendname = friendname;
    }
}