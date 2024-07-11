package com.example.myapplication22;

import static com.azure.android.maps.control.options.BubbleLayerOptions.bubbleColor;
import static com.azure.android.maps.control.options.BubbleLayerOptions.bubbleRadius;
import static com.azure.android.maps.control.options.BubbleLayerOptions.bubbleStrokeColor;
import static com.azure.android.maps.control.options.BubbleLayerOptions.bubbleStrokeWidth;
import static com.azure.android.maps.control.options.CameraOptions.center;
import static com.azure.android.maps.control.options.CameraOptions.zoom;
import static com.azure.android.maps.control.options.Expression.get;
import static com.azure.android.maps.control.options.Expression.literal;
import static com.azure.android.maps.control.options.Expression.match;
import static com.azure.android.maps.control.options.LineLayerOptions.strokeColor;
import static com.azure.android.maps.control.options.LineLayerOptions.strokeWidth;
import static com.azure.android.maps.control.options.SymbolLayerOptions.iconImage;
import static com.azure.android.maps.control.options.SymbolLayerOptions.textColor;
import static com.azure.android.maps.control.options.SymbolLayerOptions.textField;
import static com.azure.android.maps.control.options.SymbolLayerOptions.textFont;
import static com.azure.android.maps.control.options.SymbolLayerOptions.textOffset;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.azure.android.maps.control.AzureMaps;
import com.azure.android.maps.control.MapControl;
import com.azure.android.maps.control.controls.ZoomControl;
import com.azure.android.maps.control.layer.BubbleLayer;
import com.azure.android.maps.control.layer.LineLayer;
import com.azure.android.maps.control.layer.SymbolLayer;
import com.azure.android.maps.control.source.DataSource;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InspectActivity extends AppCompatActivity {

    static { // Azure Maps API Key
        //AzureMaps.setSubscriptionKey("");
        AzureMaps.setSubscriptionKey("5a4-bIL2yWRWmQ_dYQA_Ca6B01S1XkQyOCRgQFdF_-s");
    }
    MapControl mapControl; // Azure Maps Object
    DatabaseHelper sqliteDatabase;
    InspectActivity instance = this;
    float distance = 0f;
    float totalDistance = 0f;
    boolean isCalculated = false;
    boolean isBubblesShow = false;
    List<Point> points;
    List<Coordinate> coordinates;
    BubbleLayer bubbles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /* Defaults */
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inspect);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* Map initialisieren */
        mapControl = findViewById(R.id.mapcontrol2);
        mapControl.onCreate(savedInstanceState);


        if (Build.VERSION.SDK_INT >= 30){
            if (!Environment.isExternalStorageManager()){
                Intent getpermission = new Intent();
                getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getpermission);
            }
        }

        /* Extra Wert überreicht */
        long trackid = -1;// default for not set
        if (getIntent().hasExtra("trackid")) {
            // Get the message from the intent
            trackid = getIntent().getLongExtra("trackid", -2);
        }

        /* Datenbankverbindung herstellen */
        sqliteDatabase = new DatabaseHelper(this);
        sqliteDatabase.getWritableDatabase(); // !!!

        /* Inspezierter Track */
        Track inspectedTrack = sqliteDatabase.getSingleTrack(trackid);

        /* Textfelder füllen */
        TextView header = findViewById(R.id.inspectedTrackName);
        header.setText(inspectedTrack.getName());

        TextView elapsed = findViewById(R.id.elapsed);
        elapsed.setText("Elapsed Time: " + inspectedTrack.getElapsedTime());

        TextView textViewDateStart = findViewById(R.id.textViewDateStart);
        textViewDateStart.setText("Start Date: "+ inspectedTrack.getStart().replace(" ", " / "));

        TextView textViewDateEnd = findViewById(R.id.textViewDateEnd);
        textViewDateEnd.setText("Finish Date: "+ inspectedTrack.getFinish().replace(" ", " / "));

        TextView textViewDistance = findViewById(R.id.distance);
        textViewDistance.setText(inspectedTrack.getDistance());

        Switch colorSwitch = findViewById(R.id.switchButton);


        /* Koordinaten beschaffen */
        coordinates = sqliteDatabase.getCoordinatesByTrackId(trackid); // Get your list of tracks from the database or elsewhere

        Point current = null;
        Location currentLocation = new Location("1");
        Location previousLocation = new Location("2");
        int count = 0;
        points = new ArrayList<>();

        for(Coordinate c : coordinates) {
            currentLocation.setLongitude(Double.parseDouble(c.getLongitude()));
            currentLocation.setLatitude(Double.parseDouble(c.getLatitude()));

            if(isCalculated == false && count != 0) {
                distance = currentLocation.distanceTo(previousLocation);

                totalDistance = totalDistance + distance;
            }

            double longitude = Double.parseDouble(c.getLongitude());
            double latitude = Double.parseDouble(c.getLatitude());

            current = Point.fromLngLat(longitude, latitude);

            Log.d(String.valueOf(InspectActivity.class),current.toString());

            points.add(current);

            previousLocation.setLongitude(currentLocation.getLongitude());
            previousLocation.setLatitude(currentLocation.getLatitude());

            count++;
        }

        TextView textViewWaypoints = findViewById(R.id.waypoints);
        textViewWaypoints.setTextColor(Color.parseColor("#70000000"));
        textViewWaypoints.setText("Waypoints: " + points.size());

        /*  1.2 Berechnete Distanz formatieren und schreiben */
        isCalculated = true;
        /* Distanz in Textfeld schreiben*/
        String format;

        if(totalDistance >= 1000) {
            totalDistance = totalDistance / 1000; // to Kilometers
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            format = decimalFormat.format(totalDistance) + "km";
        } else {
            inspectedTrack.setDistance(String.valueOf(totalDistance));
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            format = decimalFormat.format(totalDistance) + "m";
        }


        textViewDistance.setText("Distance: " + format);




        /* 1.3 ASYNC Initalisieren / Start, Ende & Weg zeichnen */
        mapControl.getMapAsync(map -> {
            /* Quelle Layer & Controls initialisieren */
            map.controls.add(new ZoomControl());

            DataSource dataSource = new DataSource();
            map.sources.add(dataSource);

            dataSource.add(LineString.fromLngLats(points)); // Line String geometry
            LineLayer line = new LineLayer(dataSource, // Line layer
                    strokeColor("#283593"),
                    strokeWidth(5f)
            );
            map.layers.add(line);



            /* Start */
            DataSource startMarkerSource = new DataSource();
            Feature startFeature = Feature.fromGeometry(points.get(0));
            startFeature.addStringProperty("type", "start");
            startFeature.addStringProperty("label", "S");
            startMarkerSource.add(startFeature);
            map.sources.add(startMarkerSource); // Add the data source to the map

            SymbolLayer startLayer = new SymbolLayer(startMarkerSource, // Correctly reference the startMarkerSource
                    textField(get("label")),
                    iconImage(match(
                            get("type"),
                            literal("start"), literal("marker-darkblue"),
                            literal("")
                    )),
                    textColor("#FFFFFF"),
                    textOffset(new Float[] {0f, -1.0f})
            );
            map.layers.add(startLayer);

            /* Finish */
            DataSource finishMarkerSource = new DataSource();
            Feature finishFeature = Feature.fromGeometry(points.get(points.size() - 1));
            finishFeature.addStringProperty("type", "finish");
            finishFeature.addStringProperty("label", "F");
            finishMarkerSource.add(finishFeature);
            map.sources.add(finishMarkerSource); // Add the data source to the map

            SymbolLayer finishLayer = new SymbolLayer(finishMarkerSource, // Correctly reference the finishMarkerSource
                    textField(get("label")),
                    iconImage(match(
                            get("type"),
                            literal("finish"), literal("marker-darkblue"),
                            literal("")
                    )),
                    textColor("#FFFFFF"),
                    textOffset(new Float[] {0f, -1.0f})
            );
            map.layers.add(finishLayer);

            /* Einmal zentrieren */
            map.setCamera(
                    center(points.get(0)), // start
                    zoom(14)
            );

            colorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {

                    bubbles = new BubbleLayer(dataSource,
                            bubbleRadius(1.5f),
                            bubbleColor("#FFFFFF"),
                            bubbleStrokeWidth(1f),
                            bubbleStrokeColor("#FFFFFF")
                    );
                    map.layers.add(bubbles);
                } else {
                    map.layers.remove(bubbles);
                }
            });
        });









        /* 2. "Go Back" Button */
        ImageView listingToMainButton = findViewById(R.id.clickableImageArrow);
        listingToMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InspectActivity.this, ListingActivity.class);
                startActivity(intent);
            }
        });






        /* 3. Export Button */
        Button exportButton = findViewById(R.id.exportButton);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                BuildGpxHelper buildGpx = new BuildGpxHelper(instance, inspectedTrack.getName(), coordinates);
                String filePath = buildGpx.getTotalFilePath();
                Log.d(String.valueOf(InspectActivity.class), filePath);
                Uri fileUri = Uri.fromFile(new File(filePath));

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.setType("text/gpx");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, null));

                Toast toast= Toast.makeText(InspectActivity.this,
                        "GPX saved locally at \n" + filePath, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 100);
                toast.show();
                /*
                Log.d("","");
                Intent intent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download");
                intent.setDataAndType(uri, "*//*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                */


            }
        });
    }
}