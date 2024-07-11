package com.example.myapplication22;

import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, "example3.db", null, 3);

        SQLiteDatabase db = this.getWritableDatabase();
        Log.d(String.valueOf(DatabaseHelper.class),"reinstanciated at path below");
        Log.d(String.valueOf(DatabaseHelper.class), db.getPath());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTrackTable = "CREATE TABLE track (ID INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(255), start VARCHAR(255), finish VARCHAR(255), elapsed VARCHAR(255), distance VARCHAR(255), deleted VARCHAR(255))";
        db.execSQL(createTrackTable);
        Log.d(String.valueOf(DatabaseHelper.class), createTrackTable);

        String createCoordinatesTable = "CREATE TABLE coordinates (ID INTEGER PRIMARY KEY AUTOINCREMENT,track_id INTEGER, longitude VARCHAR(255), latitude VARCHAR(255), timestamp VARCHAR(255))";
        db.execSQL(createCoordinatesTable);
        Log.d(String.valueOf(DatabaseHelper.class), createCoordinatesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    long createSingleCoordinate(long track_id, String longitude, String latitude, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("track_id", track_id);
        values.put("longitude", longitude);
        values.put("latitude", latitude);
        values.put("timestamp", timestamp);

        long id = db.insert("coordinates", null, values);
        Log.d(String.valueOf(DatabaseHelper.class), "Coordinate with id " + id + " created");
        Log.d(String.valueOf(DatabaseHelper.class), "Linked to track with id " + track_id);

        db.close();

        return id;
    }

    List<Coordinate> getCoordinatesByTrackId(long track_id) {
        List<Coordinate> coordinates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM coordinates WHERE track_id = " + track_id, null); // WHERE track_id = " + track_id
            Log.d(String.valueOf(DatabaseHelper.class), "Records fetched: " + cursor.getCount());

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex("ID"));
                    @SuppressLint("Range") long trackId = cursor.getLong(cursor.getColumnIndex("track_id"));
                    @SuppressLint("Range") String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
                    @SuppressLint("Range") String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                    @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));


                    Coordinate coordinate = new Coordinate(id, trackId, longitude, latitude, timestamp);
                    coordinates.add(coordinate);

                    Log.d(String.valueOf(DatabaseHelper.class), "ID: " + id + ", Track ID: " + trackId + ", Longitude: " + longitude + ", Latitude: " + latitude);
                } while (cursor.moveToNext());
                cursor.close();
            }
        }  catch (Exception e) { Log.d(String.valueOf(DatabaseHelper.class), e.toString()); }

        db.close();
        return coordinates;
    }

    long createSingleTrack(String name, String start, String deleted) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("start", start);
        values.put("deleted", deleted);

        long id = db.insert("track", null, values);
        Log.d(String.valueOf(DatabaseHelper.class), "Track with id " + id + " created");

        db.close();

        return id;
    }

    public Track getSingleTrack(long trackId) {
        Track track = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("Track", null, "ID=?", new String[]{String.valueOf(trackId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex("ID"));
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
            @SuppressLint("Range") String start = cursor.getString(cursor.getColumnIndex("start"));
            @SuppressLint("Range") String finish = cursor.getString(cursor.getColumnIndex("finish"));
            @SuppressLint("Range") String elapsed = cursor.getString(cursor.getColumnIndex("elapsed"));
            @SuppressLint("Range") String distance = cursor.getString(cursor.getColumnIndex("distance"));
            @SuppressLint("Range") String deleted = cursor.getString(cursor.getColumnIndex("deleted"));

            track = new Track(id, name, start, finish, elapsed, distance, deleted);
        }
        // Close the cursor and database connection
        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return track;
    }

    public List<Track> getAllTracks() {
        List<Track> tracks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM track WHERE deleted = '0'", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex("ID"));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                @SuppressLint("Range") String start = cursor.getString(cursor.getColumnIndex("start"));
                @SuppressLint("Range") String finish = cursor.getString(cursor.getColumnIndex("finish"));
                @SuppressLint("Range") String elapsed = cursor.getString(cursor.getColumnIndex("elapsed"));
                @SuppressLint("Range") String distance = cursor.getString(cursor.getColumnIndex("distance"));
                @SuppressLint("Range") String deleted = cursor.getString(cursor.getColumnIndex("deleted"));
                Track track = new Track(id, name, start, finish, elapsed, distance, deleted);
                tracks.add(track);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return tracks;
    }

    public void updateSingleTrackById(long trackId, String newName, String newStart, String newFinish, String newElapsed, String newDistance,String newDeleted) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        if(newName != null) {
            values.put("name", newName);
            Log.d(String.valueOf(DatabaseHelper.class),"name updated");
        }
        if(newStart != null) {
            values.put("start", newStart);
            Log.d(String.valueOf(DatabaseHelper.class),"start updated");
        }
        if(newFinish != null) {
            values.put("finish", newFinish);
            Log.d(String.valueOf(DatabaseHelper.class),"finish updated");
        }
        if(newElapsed != null) {
            values.put("elapsed", newElapsed);
            Log.d(String.valueOf(DatabaseHelper.class), "elapsed updated");
        }
        if(newDistance != null) {
            values.put("distance", newDistance);
            Log.d(String.valueOf(DatabaseHelper.class), "distance updated");
        }
        if(newDeleted != null) {
            values.put("deleted", newDeleted);
            Log.d(String.valueOf(DatabaseHelper.class),"deleted updated");
        }

        db.update("track", values, "ID=?", new String[]{String.valueOf(trackId)});

        db.close();
    }

}
