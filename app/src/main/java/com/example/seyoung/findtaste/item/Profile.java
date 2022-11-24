package com.example.seyoung.findtaste.item;

/**
 * Created by seyoung on 2017-11-29.
 * 채팅의 친구 목록의 사용자들의 사진 경로와 이름, 아이디, 메세지 내용, 시간, 닉네임을 담을 클래스입니다.
 */

public class Profile {

    private String url; //사진 경로
    private String name;//닉네임
    private String userid;//아이디
    private String msg; //메세지 내용
    private String time;//시간
    private String username;//유저 아이디

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserimage() {
        return userimage;
    }

    public void setUserimage(String userimage) {
        this.userimage = userimage;
    }

    private String userimage;

    public String getRoomname() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
    }

    private String roomname;
    public Profile(String url, String name, String msg, String userid) {
        this.url = url;
        this.name = name;
        this.msg = msg;
        this.userid = userid;
    }

    public Profile(String url, String name, String msg, String time, String userid,String roomname,String username,String userimage) {
        this.url = url;
        this.name = name;
        this.msg = msg;
        this.time = time;
        this.userid = userid;
        this.roomname = roomname;
        this.username= username;
        this.userimage = userimage;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getMsg() {
        return msg;
    }

    public String getTime() {
        return time;
    }

    public String getUserid() {
        return userid;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
