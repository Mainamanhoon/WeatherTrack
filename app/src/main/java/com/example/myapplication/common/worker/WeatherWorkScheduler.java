package com.example.myapplication.common.worker;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.concurrent.TimeUnit;

public class WeatherWorkScheduler {

    private static final String WEATHER_SYNC_WORK_NAME = "weather_sync_work";
    private static final String TAG = "WeatherSyncWorker";

    public static void scheduleWeatherSync(Context context) {
        scheduleWeatherSync(context, 40.7128, -74.0060);
    }
    public static void scheduleWeatherSync(Context context, double latitude, double longitude) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

         Data inputData = new Data.Builder()
                .putDouble("latitude", latitude)
                .putDouble("longitude", longitude)
                .build();

        // Create periodic work request - every 6 hours
        PeriodicWorkRequest weatherSyncRequest = new PeriodicWorkRequest.Builder(
                WeatherSyncWorker.class,
                1, TimeUnit.MINUTES,
                1, TimeUnit.MINUTES // Flex interval
        )
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag(TAG)
                .build();

         WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WEATHER_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                weatherSyncRequest
        );
//        OneTimeWorkRequest weatherSyncRequest = new OneTimeWorkRequest.Builder(WeatherSyncWorker.class)
//                .setConstraints(constraints)
//                .setInputData(inputData)
//                .addTag("weather_sync")
//                .addTag("one_time")
//                .build();
//
//        WorkManager.getInstance(context).enqueueUniqueWork(
//                WEATHER_SYNC_WORK_NAME,
//                ExistingWorkPolicy.REPLACE,
//                weatherSyncRequest
//        );

        Log.d(TAG, "Weather sync scheduled for coordinates: " + latitude + ", " + longitude);
    }

    public static void cancelWeatherSync(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WEATHER_SYNC_WORK_NAME);
        android.util.Log.d(TAG, "Weather sync cancelled");
    }

    public static void updateWeatherSyncLocation(Context context, double latitude, double longitude) {
        // Cancel existing and schedule with new coordinates
        cancelWeatherSync(context);
        scheduleWeatherSync(context, latitude, longitude);
    }
}