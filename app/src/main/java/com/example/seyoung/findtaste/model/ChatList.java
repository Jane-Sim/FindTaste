package com.example.seyoung.findtaste.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by seyoung on 2017-12-30.
 * 원하는 채팅방에 들어갔을 때 서버에 채팅한 데이터를 받아와서 추가시킬 클래스입니다.
 *  채팅방이름과 친구 아이디, 닉네임, 이미지, 현재 유저의 닉네임과 이미지, 방을 만든 시간, 메세지 내용과 작성시간을 받아옵니다.
 */

public class ChatList {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("chatroom")
    @Expose
    private String chatroom;
    @SerializedName("friend_name")
    @Expose
    private String friendName;
    @SerializedName("friend_image")
    @Expose
    private String friendImage;
    @SerializedName("user_name")
    @Expose
    private String user_name;
    @SerializedName("user_image")
    @Expose
    private String user_image;
    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("usernum")
    @Expose
    private String usernum;
    @SerializedName("roomowner")
    @Expose
    private String roomowner;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("statustime")
    @Expose
    private String statustime;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChatroom() {
        return chatroom;
    }

    public void setChatroom(String chatroom) {
        this.chatroom = chatroom;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendImage() {
        return friendImage;
    }

    public void setFriendImage(String friendImage) {
        this.friendImage = friendImage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsernum() {
        return usernum;
    }

    public void setUsernum(String usernum) {
        this.usernum = usernum;
    }

    public String getRoomowner() {
        return roomowner;
    }

    public void setRoomowner(String roomowner) {
        this.roomowner = roomowner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatustime() {
        return statustime;
    }

    public void setStatustime(String statustime) {
        this.statustime = statustime;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

}
