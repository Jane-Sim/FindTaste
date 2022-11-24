package com.example.seyoung.findtaste.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by seyoung on 2017-12-02.
 * 원하는 리뷰의 댓글들을 서버에 받아와 담을 클래스입니다.
 * 리뷰작성자 이름과 맛집이름, 작성시간, 댓글작성자 이름, 사진 ,시간을 담습니다.
 */

public class FeedComment {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("writename")
    @Expose
    private String writename;
    @SerializedName("foodname")
    @Expose
    private String foodname;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("comment")
    @Expose
    private String comment;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("userpic")
    @Expose
    private String userpic;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("timestamp_user")
    @Expose
    private String timestamp_user;

    public String getTimestamp_user() {
        return timestamp_user;
    }

    public void setTimestamp_user(String timestamp_user) {
        this.timestamp_user = timestamp_user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }




    public String getUserpic() {
        return userpic;
    }

    public void setUserpic(String userpic) {
        this.userpic = userpic;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWritename() {
        return writename;
    }

    public void setWritename(String writename) {
        this.writename = writename;
    }

    public String getFoodname() {
        return foodname;
    }

    public void setFoodname(String foodname) {
        this.foodname = foodname;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
