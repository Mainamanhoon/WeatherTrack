package com.example.myapplication.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class LocationPreferences {
    private static final String PREFS_NAME = "weather_app_prefs";
    private static final String KEY_LATITUDE = "last_latitude";
    private static final String KEY_LONGITUDE = "last_longitude";
    private static final String KEY_TIMESTAMP = "location_timestamp";
    private static final String KEY_HAS_LOCATION = "has_location";

    public static void saveLastKnownLocation(Context context, double latitude, double longitude) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(KEY_LATITUDE, Double.doubleToRawLongBits(latitude))
                .putLong(KEY_LONGITUDE, Double.doubleToRawLongBits(longitude))
                .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                .putBoolean(KEY_HAS_LOCATION, true)
                .apply();
    }

    public static double[] getLastKnownLocation(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (!prefs.getBoolean(KEY_HAS_LOCATION, false)) {
            return null;
        }

         long timestamp = prefs.getLong(KEY_TIMESTAMP, 0);
        long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);

        if (timestamp < sevenDaysAgo) {
            return null;
        }

        double latitude = Double.longBitsToDouble(prefs.getLong(KEY_LATITUDE, 0));
        double longitude = Double.longBitsToDouble(prefs.getLong(KEY_LONGITUDE, 0));

         if (isValidCoordinate(latitude, longitude)) {
            return new double[]{latitude, longitude};
        }

        return null;
    }

    public static boolean hasValidLocation(Context context) {
        return getLastKnownLocation(context) != null;
    }

    public static void clearLocation(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_LATITUDE)
                .remove(KEY_LONGITUDE)
                .remove(KEY_TIMESTAMP)
                .putBoolean(KEY_HAS_LOCATION, false)
                .apply();
    }

    private static boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90.0 && latitude <= 90.0 &&
                longitude >= -180.0 && longitude <= 180.0 &&
                latitude != 0.0 && longitude != 0.0;
    }

    public static long getLocationAge(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long timestamp = prefs.getLong(KEY_TIMESTAMP, 0);
        if (timestamp == 0) return -1;
        return System.currentTimeMillis() - timestamp;
    }
}