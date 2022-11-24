package com.example.seyoung.findtaste.model;

/**
 * Created by seyoung on 2017-10-20.
 * 맛집의 상세정보를 서버에서 받아와 담을 클래스입니다.
 * 맛집의 이름과 주소, 상세정보, 이미지, 위도 경도, 현재 사용자의 즐겨찾기 유무를 담습니다.
 */

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;


//@org.parceler.Parcel
public class Tasteitem implements Comparable<Tasteitem>{

    @SerializedName("id")
    public int id;
    @SerializedName("image_name")
    private String image_name;
    @SerializedName("image_path")
    private String image_path;
    @SerializedName("food_name")
    private String food_name;
    @SerializedName("food_address")
    private String food_address;
    @SerializedName("food_number")
    private String food_number;
    @SerializedName("food_memo")
    private String food_memo;
    @SerializedName("user_name")
    private String user_name;
    @SerializedName("lati")
    private double lati;
    @SerializedName("logi")
    private double logi;
    @SerializedName("favorites")
    private String favorites;
    @SerializedName("rating")
    private double rating;
    @SerializedName("rating_num")
    private String rating_num;
    @SerializedName("meter")
    private String meter;

    public String getRating_num() {
        return rating_num;
    }

    public void setRating_num(String rating_num) {
        this.rating_num = rating_num;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }


    public Tasteitem(double lat, double lon, int number) {
        this.lati = lat;
        this.logi = lon;
        this.id = number;
    }

    public String getMeter() {
        return meter;
    }

    public void setMeter(String meter) {
        this.meter = meter;
    }

    public String getFavorites() {
        return favorites;
    }

    public void setFavorites(String favorites) {
        this.favorites = favorites;
    }

    public String getFood_address() {
        return food_address;
    }

    public void setFood_address(String food_address) {
        this.food_address = food_address;
    }



    @Override
    public String toString() {
        return "taste{" +
                "id='" + id +'\''+
                ", image_name='" + image_name +'\''+
                ", image_path='" + image_path + '\'' +
                ", food_name='" + food_name + '\'' +
                ", food_adress='" + food_address + '\'' +
                ", food_number='" + food_number + '\'' +
                ", food_memo='" + food_memo +'\'' +
                ", user_name='" + user_name +'\'' +
                ", lati='" + lati + '\'' +
                ", logi='" + logi + '\'' +
                ", favorites='" + favorites + '}';
    }
    public int getId() {
        return id;
    }

    public String getImage_name() {
        return image_name;
    }

    public String getImage_path() {
        return image_path;
    }

    public String getFood_name() {
        return food_name;
    }

    public String getFood_number() {
        return food_number;
    }

    public String getFood_memo() {
        return food_memo;
    }

    public String getUser_name() {
        return user_name;
    }

    public double getLati() {
        return lati;
    }

    public double getLogi() {
        return logi;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public void setFood_name(String food_name) {
        this.food_name = food_name;
    }

    public void setFood_number(String food_number) {
        this.food_number = food_number;
    }

    public void setFood_memo(String food_memo) {
        this.food_memo = food_memo;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public void setLati(double lati) {
        this.lati = lati;
    }

    public void setLogi(double logi) {
        this.logi = logi;
    }

    @Override
    public int compareTo(@NonNull Tasteitem _tasteitem) {
        return this.meter.compareTo(_tasteitem.meter);
    }

}