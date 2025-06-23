package com.example.myapplication.common.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.ListenableWorker;
import androidx.work.WorkerFactory;
import androidx.work.WorkerParameters;

import com.example.myapplication.data.local.WeatherDao;
import com.example.myapplication.data.network.ApiService;

import javax.inject.Inject;

public class WeatherSyncWorkerFactory extends WorkerFactory {
    private final ApiService apiService;
    private final WeatherDao weatherDao;

    @Inject
    public WeatherSyncWorkerFactory(ApiService apiService, WeatherDao weatherDao){
        this.apiService = apiService;
        this.weatherDao = weatherDao;
    }

    @Nullable
    @Override
    public ListenableWorker createWorker(@NonNull Context appContext,
                                         @NonNull String workerClassName,
                                         @NonNull WorkerParameters workerParameters) {
        if(workerClassName.equals(WeatherSyncWorker.class.getName())){
            return new WeatherSyncWorker(appContext,workerParameters,apiService,weatherDao);
        }
        return null;
    }
}
