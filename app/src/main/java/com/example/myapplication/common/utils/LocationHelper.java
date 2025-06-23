package com.example.myapplication.common.utils;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {

    private static final String TAG = "LocationHelper";
    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

     @SuppressLint("MissingPermission")
     public void getCurrentLocation(LocationCallback callback) {
        if (!hasLocationPermission()) {
            callback.onError("Location permission not granted");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                         callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                    } else {
                         requestFreshLocation(callback);
                    }
                })
                .addOnFailureListener(e -> {
                     callback.onError("Failed to get location: " + e.getMessage());
                });
    }

    private void requestFreshLocation(LocationCallback callback) {
        callback.onError("Unable to get current location");
    }

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
        void onError(String error);
    }
}