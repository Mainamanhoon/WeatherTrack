package com.example.myapplication.common.worker;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.myapplication.common.Constants;
import com.example.myapplication.common.utils.LocationPreferences;

import java.util.concurrent.TimeUnit;

public class WeatherWorkScheduler {

    private static final String WEATHER_SYNC_WORK_NAME = "weather_sync_work";
    private static final String TAG = "WeatherWorkScheduler";

    public static void scheduleWeatherSync(Context context) {
         double[] userLocation = LocationPreferences.getLastKnownLocation(context);

        if (userLocation != null) {
             scheduleWeatherSync(context, userLocation[0], userLocation[1]);
        } else {
             scheduleWeatherSync(context, Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE);
        }
    }

    public static void scheduleWeatherSync(Context context, double latitude, double longitude) {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        Data inputData = new Data.Builder()
                .putDouble("latitude", latitude)
                .putDouble("longitude", longitude)
                .build();

        PeriodicWorkRequest weatherSyncRequest = new PeriodicWorkRequest.Builder(
                WeatherSyncWorker.class,
                6, TimeUnit.HOURS,
                1, TimeUnit.HOURS
        )
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag(TAG)
                .addTag("weather_sync_periodic")
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WEATHER_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                weatherSyncRequest
        );
     }

    public static void updateWeatherSyncLocation(Context context, double latitude, double longitude) {

         LocationPreferences.saveLastKnownLocation(context, latitude, longitude);
         cancelWeatherSync(context);

         try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        scheduleWeatherSync(context, latitude, longitude);
    }

    public static void cancelWeatherSync(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WEATHER_SYNC_WORK_NAME);
     }

    /**
     * Call this method when the user grants location permission for the first time
     * or when the app gets a fresh location update
     */
    public static void refreshWeatherSyncWithUserLocation(Context context) {
        double[] userLocation = LocationPreferences.getLastKnownLocation(context);

        if (userLocation != null) {
             updateWeatherSyncLocation(context, userLocation[0], userLocation[1]);
        } else {
            Log.d(TAG, "No user location available for refresh");
        }
    }

    /**
     * Check if the current scheduled work is using stale location
     */
    public static boolean isUsingStaleLocation(Context context) {
        double[] currentLocation = LocationPreferences.getLastKnownLocation(context);
        if (currentLocation == null) {
            return false;
        }

         long locationAge = LocationPreferences.getLocationAge(context);
        return locationAge > (24 * 60 * 60 * 1000L); // 1 day in milliseconds
    }

    /**
     * Force an immediate weather sync (useful for testing or manual refresh)
     */
    public static void triggerImmediateSync(Context context) {

        double[] userLocation = LocationPreferences.getLastKnownLocation(context);
        double latitude = Constants.DEFAULT_LATITUDE;
        double longitude = Constants.DEFAULT_LONGITUDE;

        if (userLocation != null) {
            latitude = userLocation[0];
            longitude = userLocation[1];
        }

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        Data inputData = new Data.Builder()
                .putDouble("latitude", latitude)
                .putDouble("longitude", longitude)
                .build();

        androidx.work.OneTimeWorkRequest immediateSync =
                new androidx.work.OneTimeWorkRequest.Builder(WeatherSyncWorker.class)
                        .setConstraints(constraints)
                        .setInputData(inputData)
                        .addTag("immediate_sync")
                        .build();

        WorkManager.getInstance(context).enqueue(immediateSync);
     }

    /**
     * Get the status of the periodic work
     */
    public static void checkWorkStatus(Context context) {
        WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(WEATHER_SYNC_WORK_NAME)
                .addListener(() -> {
                    try {
                        androidx.lifecycle.LiveData<java.util.List<androidx.work.WorkInfo>> workInfos =
                                (androidx.lifecycle.LiveData<java.util.List<androidx.work.WorkInfo>>) WorkManager.getInstance(context).getWorkInfosForUniqueWork(WEATHER_SYNC_WORK_NAME);

                        if (workInfos.getValue() != null && !workInfos.getValue().isEmpty()) {
                            androidx.work.WorkInfo workInfo = workInfos.getValue().get(0);
                         } else {
                            Log.d(TAG, "No weather sync work found");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error checking work status", e);
                    }
                }, Runnable::run);
    }
}