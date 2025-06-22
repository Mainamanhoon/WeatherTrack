package com.example.myapplication.common;
import android.app.Application;

import com.example.myapplication.common.worker.WeatherWorkScheduler;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MyApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();

        WeatherWorkScheduler.scheduleWeatherSync(this);

    }
}