package com.example.seyoung.findtaste.model;

/**
 * Created by seyoung on 2017-10-20.
 * 맛집을 등록할 때 이미지들과 해당 리뷰들의 이미지 경로들을 가져옵니다.
 */

import com.google.gson.annotations.SerializedName;


//@org.parceler.Parcel
public class Tastepath {                    // 맛집의 정보를 저장하고 불러들일 곳.
    @SerializedName("id")
    private String id;
    @SerializedName("path")
    private String path;
    @SerializedName("name")
    private String name;
    @SerializedName("fname")
    private String fname;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }



    @Override
    public String toString() {
        return "taste{" +
                "id='" + id +'\''+
                ", path='" + path +'\''+
                ", name='" + name +'\''+
                ", fname='" + fname + '}';
    }

}
