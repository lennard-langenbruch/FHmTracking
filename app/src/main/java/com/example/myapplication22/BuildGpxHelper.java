package com.example.myapplication22;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class BuildGpxHelper {

    private static final int REQUEST_CODE = 123;

    private String totalFilePath = "";

    public BuildGpxHelper(Activity activity, String trackName, List<Coordinate> coordinates) {
        activity = activity;

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        } else {
            handleFileUriExposure();

            writeGpxFile(activity, trackName, coordinates);
        }
    }

    private void handleFileUriExposure() {
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Handle if permission denied for export
    // add time stamps

    private void writeGpxFile(Context context, String trackName, List<Coordinate> coordinates) {
        // Create a directory named "GPXFiles" in the Downloads folder
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generate file path with trackName
        trackName = trackName.trim();
        String fileName =  trackName + ".gpx";

        String filePath = directory + "";
        File file = new File(directory, fileName);


        // GPX file content
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
        String trackNameTag = "<name>" + trackName + "</name><trkseg>\n";
        String waypoints = "";

        DateFormat gpxFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        for (Coordinate coordinate : coordinates) {
            /*
            try {
                String dateString = coordinate.getTimestamp();
                Date date = gpxFormat.parse(dateString);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }*/

            waypoints += "<trkpt lat=\"" + coordinate.getLatitude() + "\" lon=\"" + coordinate.getLongitude() + "\"><time>" + coordinate.getTimestamp() + "</time></trkpt>\n";
        }
        String footer = "</trkseg></trk></gpx>";

        try {
            // Write to the GPX file
            FileWriter writer = new FileWriter(file, false);
            writer.append(header);
            writer.append(trackNameTag);
            writer.append(waypoints);
            writer.append(footer);
            writer.flush();
            writer.close();

            // Log success message
            Log.d(String.valueOf(BuildGpxHelper.class), "GPX file saved successfully in Download Folder" + filePath);
        } catch (IOException e) {
            // Log error message if failed to write GPX file
            Log.e(String.valueOf(BuildGpxHelper.class), "Error writing GPX file", e);
        }
        this.totalFilePath = filePath + "/" + fileName;
    }


    private static Activity getActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        return null;
    }

    public String getTotalFilePath() {
        return this.totalFilePath;
    }

}
