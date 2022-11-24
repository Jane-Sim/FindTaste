package com.example.seyoung.findtaste.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by seyoung on 2017-11-24.
 * 사용자가 작성한 리뷰의 사진경로들을 서버에서 가져와
 * 현재 클래스에 담습니다.
 */

public class Reviewpic {

    @SerializedName("path")
    @Expose
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}