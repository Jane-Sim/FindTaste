package com.example.seyoung.findtaste.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by seyoung on 2017-11-24.
 * 사용자들의 리뷰데이터를 서버에서 가져와 현재 클래스에 담습니다.
 * 작성자 닉네임과 사진, 리뷰 내용과 작성시간, 맛집 이름과 좋아요 갯수와 댓글 갯수,
 * 평점과 유저 아이디와 리뷰에 넣은 사진들을 담습니다.
 * 작성자 아이디와 현재 유저의 아이디가 같으면 리뷰를 수정, 삭제할 수 있도록 해줍니다.
 */

public class FeedItem implements Serializable {
    @SerializedName("id")
    public int id;
    @SerializedName("username")             //사용자의 닉네임이다
    private String username;
    @SerializedName("status")
    private String status;
    @SerializedName("profilepic")
    private String profilepic;
    @SerializedName("timestamp")
    private String timestamp;
    @SerializedName("foodname")
    private String foodname;
    @SerializedName("good")
    private String good;
    @SerializedName("comment")
    private String comment;
    @SerializedName("rating")
    private int rating;
    @SerializedName("username2")                //사용자의아이디
    private String username2;
    @SerializedName("reviewpic")
    @Expose
    private List<Reviewpic> reviewpic = null;
    @SerializedName("likey")
    private String likey;



    public String getLikey() {
        return likey;
    }

    public void setLikey(String likey) {
        this.likey = likey;
    }




    public List<Reviewpic> getReviewpic() {
        return reviewpic;
    }

    public void setReviewpic(List<Reviewpic> reviewpic) {
        this.reviewpic = reviewpic;
    }

    public void setReviewpic(ArrayList<Reviewpic> list3) {
        this.reviewpic = list3;
    }




    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getFoodname() {
        return foodname;
    }

    public void setFoodname(String foodname) {
        this.foodname = foodname;
    }

    public String getGood() {
        return good;
    }

    public void setGood(String good) {
        this.good = good;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getUsername2() {
        return username2;
    }

    public void setUsername2(String username2) {
        this.username2 = username2;
    }

    @Override
    public String toString() {
        return "Feed{" +
                "id='" + id +'\''+
                ", username='" + username +'\''+
                ", status='" + status + '\'' +
                ", profilepic='" + profilepic + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", foodname='" + foodname + '\'' +
                ", good='" + good +'\'' +
                ", comment='" + comment +'\'' +
                ", reviewpic='" +   reviewpic +
                ", Username2='" + username2 +'\'' +
                ", rating='" + rating+ '}';
    }


}
