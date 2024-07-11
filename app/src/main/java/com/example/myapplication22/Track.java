package com.example.myapplication22;

public class Track {

    public Track(long id, String name, String start, String finish, String elapsedTime, String distance, String deleted) {
        this.id = id;
        this.name = name;
        this.start = start;
        this.finish = finish;
        this.elapsedTime = elapsedTime;
        this.distance = distance;
        this.deleted = deleted;
    }

    private long id;
    private String name;
    private String start;
    private String finish;
    private String deleted;
    private String elapsedTime;
    private String distance;

    public Track() {}

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getDeleted() {
        return deleted;
    }


    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Track {" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", start='" + start + '\'' +
                ", deleted='" + deleted + '\'' +
                '}';
    }

    public String getFinish() {
        return finish;
    }

    public void setFinish(String finish) {
        this.finish = finish;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}
