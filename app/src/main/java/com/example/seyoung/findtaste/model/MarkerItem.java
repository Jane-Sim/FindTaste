package com.example.seyoung.findtaste.model;

/**
 * Created by seyoung on 2017-11-09.
 *
 * 현재는 사용하지 않는 클래스입니다.
 */

public class MarkerItem {
    double lat;
    double lon;
    int number;
    public MarkerItem (double lat, double lon, int number) {
        this.lat = lat;
        this.lon = lon;
        this.number = number;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

}

