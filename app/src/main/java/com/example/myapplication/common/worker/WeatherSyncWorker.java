package com.example.myapplication.common.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.common.Constants;
import com.example.myapplication.data.local.WeatherDao;
import com.example.myapplication.data.local.WeatherEntity;
import com.example.myapplication.data.model.WeatherResponse;
import com.example.myapplication.data.network.ApiService;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import retrofit2.Response;

@HiltWorker
public class WeatherSyncWorker extends Worker {
    private static final String TAG = "WeatherSyncWorker";

    private final ApiService apiService;
    private final WeatherDao weatherDao;

    @AssistedInject
    public WeatherSyncWorker(
            @Assisted Context context,
            @Assisted WorkerParameters params,
            ApiService apiService,
            WeatherDao weatherDao) {
        super(context, params);
        this.apiService = apiService;
        this.weatherDao = weatherDao;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "WeatherSyncWorker started - fetching weather data");

        try {
            // Get coordinates from input data or use defaults
            double latitude = getInputData().getDouble("latitude", Constants.DEFAULT_LATITUDE);
            double longitude = getInputData().getDouble("longitude", Constants.DEFAULT_LONGITUDE);

            Log.d(TAG, "Fetching weather for coordinates: " + latitude + ", " + longitude);

            // Make synchronous API call
            Response<WeatherResponse> response = apiService.getCurrentWeather(
                    latitude,
                    longitude,
                    Constants.API_KEY,
                    Constants.UNITS_METRIC
            ).execute();

            if (response.isSuccessful() && response.body() != null) {
                WeatherResponse weatherData = response.body();

                // Save to database
                WeatherEntity entity = new WeatherEntity(weatherData);
                weatherDao.insertWeatherData(entity);

                Log.d(TAG, "Weather data successfully saved to database");
                return Result.success();

            } else {
                Log.e(TAG, "API call failed with code: " + response.code());
                return Result.retry();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error during weather sync", e);
            return Result.failure();
        }
    }
}