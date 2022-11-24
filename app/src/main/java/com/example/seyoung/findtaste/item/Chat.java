package com.example.seyoung.findtaste.item;

/**
 * Created by seyoung on 2017-12-05.
 * 메세지를 보낸 친구닉네임과 채팅 내용, 누가 보냈는지, 사진 경로, 시간 등 데이터를 담을 수 있는 클래스입니다.
 */

public class Chat {

    private String friend;      //친구 닉네임

    private String chat;        //채팅 내용

    private String who;         //누가 보냈는 지 판단해주는 스트링

    private String profile;     //사진 경로

    private String time;        //시간

    public Chat(String friend, String chat, String who, String profile,String time) {
        this.friend = friend;
        this.chat = chat;
        this.who = who;
        this.profile = profile;
        this.time = time;
    }

    public String getProfile() {
        return profile;
    }

    public String getFriend() {
        return friend;
    }

    public String getChat() {
        return chat;
    }

    public String getWho() {
        return who;
    }

    public String getTime() {return time;}
}
