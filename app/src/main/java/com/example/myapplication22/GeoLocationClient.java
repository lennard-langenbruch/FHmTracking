package com.example.myapplication22;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class GeoLocationClient {

    private final Context mContext;
    private final FusedLocationProviderClient mFusedLocationClient;
    private Location mLastUpdatedLocation;
    public double longitude;
    public double latitude;
    public float distance;
    private boolean isUpdatingLocation;

    public GeoLocationClient(Context context) {
        mContext = context;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        isUpdatingLocation = false;
        startLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        if (!isUpdatingLocation) {
            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    1000
            ).build();

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            isUpdatingLocation = true;
            Log.d("GeoLocationHelper", "Location updates started.");
        }
    }

    public void stopLocationUpdates() {
        if (isUpdatingLocation) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
            isUpdatingLocation = false;
            Log.d("GeoLocationHelper", "Location updates stopped.");
        }
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }

            for (Location location : locationResult.getLocations()) {
                Log.d("GeoLocationHelper", "onLocationChanged called");
                Log.d("GeoLocationHelper", "Received location: Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());

                if (mLastUpdatedLocation == null) {
                    mLastUpdatedLocation = location;
                    updateLocationVariables(location);
                    Log.d("GeoLocationHelper", "First location fetched");
                } else {
                    distance = mLastUpdatedLocation.distanceTo(location);
                    Log.d("GeoLocationHelper", "Distance to new location: " + distance);
                    if (distance > 1.0) {
                        updateLocationVariables(location);
                    } else {
                        Log.d("GeoLocationHelper", "Location variables not updated because distance to last updated location is less than 1m");
                    }
                }
            }
        }
    };

    private void updateLocationVariables(Location location) {
        mLastUpdatedLocation = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.d("GeoLocationHelper", "Location variables updated: Latitude: " + latitude + ", Longitude: " + longitude);
    }
}
