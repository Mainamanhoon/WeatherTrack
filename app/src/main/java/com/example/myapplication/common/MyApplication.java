package com.example.myapplication.common;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.example.myapplication.common.worker.WeatherWorkScheduler;

import dagger.hilt.android.HiltAndroidApp;
import jakarta.inject.Inject;

@HiltAndroidApp
public class MyApplication extends Application implements Configuration.Provider {

    @Inject
    HiltWorkerFactory workerFactory;

    @Override
    public void onCreate(){
        super.onCreate();

        WeatherWorkScheduler.scheduleWeatherSync(this);

    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }
}