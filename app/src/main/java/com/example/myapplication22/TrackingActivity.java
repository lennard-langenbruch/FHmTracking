package com.example.myapplication22;

import static com.azure.android.maps.control.options.AnimationOptions.animationDuration;
import static com.azure.android.maps.control.options.AnimationOptions.animationType;
import static com.azure.android.maps.control.options.CameraOptions.center;
import static com.azure.android.maps.control.options.CameraOptions.zoom;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.azure.android.maps.control.AzureMaps;
import com.azure.android.maps.control.MapControl;
import com.azure.android.maps.control.controls.ZoomControl;
import com.azure.android.maps.control.layer.SymbolLayer;
import com.azure.android.maps.control.options.AnimationType;
import com.azure.android.maps.control.source.DataSource;
import com.mapbox.geojson.Point;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TrackingActivity extends AppCompatActivity {


    static {
        // Azure Maps API Key
        //AzureMaps.setSubscriptionKey("");
    }

    MapControl mapControl; // Azure Maps Object
    DatabaseHelper sqliteDao; // Database Access Object
    GeoLocationClient geoLocation;
    StopWatchHelper sw;
    long currentTackId;
    TextView textLong;
    TextView textLat;
    String longitudeString;
    String latitudeString;
    String lastSavedLongitude;
    String lastSavedLatitude;
    Date df;
    Date ds;
    String pattern = "dd.MM.yy HH:mm:ss.SSS"; // Define the desired pattern
    String gpxPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
    SimpleDateFormat gpxFormat = new SimpleDateFormat(gpxPattern);
    List<SymbolLayer> layerList = new ArrayList<>();
    boolean started = false;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(String.valueOf(TrackingActivity.class), "Tracking onCreate() called");

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tracking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.verticalLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Start Tracking Foreground Service
        Intent serviceIntent = new Intent(this, GeoLocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(serviceIntent);
        } else {
            this.startService(serviceIntent);
        }

        sqliteDao = new DatabaseHelper(this);
        sqliteDao.getWritableDatabase(); // !!!

        currentTackId = sqliteDao.createSingleTrack("temp", "start", "0");
        String currentTrackName = "Track " + currentTackId;
        ds = new Date();

        String dateStart = dateFormat.format(ds);
        sqliteDao.updateSingleTrackById(currentTackId, currentTrackName, dateStart,null,null, null, null);

        TextView timeText = findViewById(R.id.time);
        sw = new StopWatchHelper(timeText);
        //sw.start();

        geoLocation = new GeoLocationClient(this);

            /* Initial Map Load */
            //geoLocation.getLocation();
            textLong = findViewById(R.id.longitude);
            textLat = findViewById(R.id.latitude);
            mapControl = findViewById(R.id.mapcontrol);
            mapControl.onCreate(savedInstanceState);

            if(geoLocation.longitude == 0.0) {
                timeText.setText("waiting for location to start timer");
                textLong.setText("loading ... ");
                textLat.setText("loading ...");
            }

            if(geoLocation.longitude != 0.0) {



                started = true;
                sw.start();
                longitudeString = String.valueOf(geoLocation.longitude);
                latitudeString = String.valueOf(geoLocation.latitude);
                textLong.setText("Longitude: " + longitudeString);
                textLat.setText("Latitude: " + latitudeString);

                mapControl.getMapAsync(map -> { //);
                    DataSource source = new DataSource();
                    map.sources.add(source);
                    Point p = Point.fromLngLat(geoLocation.longitude, geoLocation.latitude);
                    source.add(p);
                    SymbolLayer layer = new SymbolLayer(source);
                    map.layers.add(layer);
                    layerList.add(layer);

                    map.setCamera(
                            center(p),
                            zoom(14),
                            animationType(AnimationType.FLY),
                            animationDuration(2000)
                    );

                    map.controls.add(new ZoomControl());
                });
        }

        Button stopTrackingButton = findViewById(R.id.stopTrackingButton);
        stopTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sw.stop();
                sw.setRunning(false);

                df = new Date();
                String dateFinish = dateFormat.format(df); // dateFormat.format(d);

                long differenceInMillis = df.getTime() - ds.getTime();

                long minutes = differenceInMillis / (1000 * 60);
                long seconds = (differenceInMillis / 1000) % 60;
                long milliseconds = differenceInMillis % 1000;
                @SuppressLint("DefaultLocale") String elapsedTimeWithoutStopwatch = String.format("%02d:%02d:%02d", minutes, seconds, milliseconds);

                handler.removeCallbacksAndMessages(null); //

                sqliteDao.updateSingleTrackById(currentTackId, null, null, dateFinish, elapsedTimeWithoutStopwatch, "0", null);

                GeoLocationService.staticInstance.stopSelf();

                Intent intent = new Intent(TrackingActivity.this, MainActivity.class);
                intent.putExtra("message", currentTrackName + " was saved successfully");
                startActivity(intent);
            }
        });

        // Start a Runnable to periodically update location
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    updateLocation();
                    handler.postDelayed(this, 1000); // 1 second interval
                }
        }, 0); // Delay for the first run, 0 second
    }

    // update Location by seconds passed
    void updateLocation() {
        DataSource source = new DataSource();

        // geoLocation.getLocation();
        if(geoLocation.longitude == 0.0) {
            textLong.setText("loading ... ");
            textLat.setText("loading ...");
        }


        if(geoLocation.longitude != 0.0) { // avoid first value at 0.0
        if(started == false) {
            started = true;
            sw.start();
        }

        Log.d("TrackingActivity", "(" + geoLocation.latitude + "," + geoLocation.longitude + ")");

        longitudeString = String.valueOf(geoLocation.longitude);
        latitudeString = String.valueOf(geoLocation.latitude);

        textLong.setText("Longitude: " + longitudeString);
        textLat.setText("Latitude: " + latitudeString);

        boolean latitudeStringChanged = !latitudeString.equals(lastSavedLatitude);
        boolean longitudeStringChanged = !longitudeString.equals(lastSavedLongitude);


        if (!latitudeStringChanged && !longitudeStringChanged) {
            Log.d(String.valueOf(GeoLocationClient.class), "No update because location has not changed");
        }

        if (latitudeStringChanged || longitudeStringChanged) {
            // Save coordinates and link to track id
            String gpx = gpxFormat.format(new Date());
            Log.d(String.valueOf(TrackingActivity.class), "Coordinate assigned to track: " + currentTackId);
            long currentCoordinateid = sqliteDao.createSingleCoordinate(currentTackId, longitudeString, latitudeString, gpx);

            Log.d(String.valueOf(GeoLocationClient.class), "Location/coordinate saved to database: " + longitudeString + ", " + latitudeString + ", timestamp: " + gpx);
        }

            // place marker on azure map:
            mapControl = findViewById(R.id.mapcontrol);

            mapControl.getMapAsync(map -> { //mapControl.onReady(map -> {});

                for (SymbolLayer addedLayer : layerList) {
                    map.layers.remove(addedLayer);
                }
                layerList.clear();

                map.sources.add(source);

                Point p = Point.fromLngLat(geoLocation.longitude, geoLocation.latitude);
                source.add(p);
                SymbolLayer layer = new SymbolLayer(source);
                map.layers.add(layer);
                layerList.add(layer);

                map.setCamera(
                        center(p),
                        zoom(14),
                        animationType(AnimationType.FLY),
                        animationDuration(2000)
                );

                if (map.controls.getControls() == null)
                    map.controls.add(new ZoomControl());

            });

        }

        lastSavedLongitude = longitudeString;
        lastSavedLatitude = latitudeString;

    }

    @Override
    protected void onPause() {
        Log.d(String.valueOf(TrackingActivity.class), "Tracking onPause() called");


        super.onPause();
        df = new Date();
        String dateFinish = dateFormat.format(df); // dateFormat.format(d);

        long differenceInMillis = df.getTime() - ds.getTime();

        long minutes = differenceInMillis / (1000 * 60);
        long seconds = (differenceInMillis / 1000) % 60;
        long milliseconds = differenceInMillis % 1000;
        @SuppressLint("DefaultLocale") String elapsedTimeWithoutStopwatch = String.format("%02d:%02d:%02d", minutes, seconds, milliseconds);

        sqliteDao.updateSingleTrackById(currentTackId, null, null, dateFinish, elapsedTimeWithoutStopwatch, "0", null);
    }



    @Override
    protected void onDestroy() {
        Log.d(String.valueOf(TrackingActivity.class), "Tracking onDestroy() called");


        super.onDestroy(); // necesarry
        // stopTracking Button Action minus Intent & View switch
        sw.stop();
        sw.setRunning(false);

        handler.removeCallbacksAndMessages(null); //
        GeoLocationService.staticInstance.stopSelf();
    }

}
