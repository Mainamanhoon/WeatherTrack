package com.example.myapplication.common.worker;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.domain.WeatherRepository;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker

public class WeatherSyncWorker extends Worker {
    private static final String TAG = "WeatherSyncWorker";
    private WeatherRepository weatherRepository;

    @AssistedInject
    public WeatherSyncWorker(
            @Assisted WeatherRepository repository,
            @Assisted Context context,
            @Assisted WorkerParameters params) {
        super(context, params);
        this.weatherRepository = repository;

    }


    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "WeatherSyncWorker started - fetching weather data");

        try {
            double defaultLat = getInputData().getDouble("latitude", 40.7128); // Default to NYC
            double defaultLon = getInputData().getDouble("longitude", -74.0060);



        }catch (Exception e){
            e.printStackTrace();
        }
     }
}
