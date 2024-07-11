package com.example.myapplication22;

import java.time.LocalDateTime;
import java.util.Date;

public class Coordinate {

    public Coordinate(){}

    public Coordinate(long id, long trackId, String longitude, String latitude, String timestamp) {
        this.id = id;
        this.trackId = trackId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.timestamp = timestamp;
    }

    private long id;
    private long trackId;
    private String longitude;
    private String latitude;
    private String timestamp;


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public long getId() { // no setter
        return id;
    }

    public long getTrackId() { // no setter
        return trackId;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
