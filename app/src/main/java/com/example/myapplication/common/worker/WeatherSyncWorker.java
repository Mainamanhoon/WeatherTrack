package com.example.myapplication.common.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.common.Constants;
import com.example.myapplication.common.utils.LocationPreferences;
import com.example.myapplication.data.local.WeatherDao;
import com.example.myapplication.data.local.WeatherEntity;
import com.example.myapplication.data.model.WeatherResponse;
import com.example.myapplication.data.network.ApiService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
             if (shouldSkipTodaysSync()) {
                 return Result.success();
            }

             double latitude = getInputData().getDouble("latitude", 0);
            double longitude = getInputData().getDouble("longitude", 0);

             if (!isValidCoordinate(latitude, longitude)) {

                 double[] userLocation = LocationPreferences.getLastKnownLocation(getApplicationContext());
                if (userLocation != null) {
                    latitude = userLocation[0];
                    longitude = userLocation[1];
                 } else {
                     latitude = Constants.DEFAULT_LATITUDE;
                    longitude = Constants.DEFAULT_LONGITUDE;
                 }
            }

              Response<WeatherResponse> response = apiService.getCurrentWeather(
                    latitude,
                    longitude,
                    Constants.API_KEY,
                    Constants.UNITS_METRIC
            ).execute();

            if (response.isSuccessful() && response.body() != null) {
                WeatherResponse weatherData = response.body();

                 WeatherEntity entity = new WeatherEntity(weatherData);
                weatherDao.insertWeatherData(entity);

                 cleanOldData();

                logDailyRecordsCount();

                return Result.success();

            } else {

                if (response.code() >= 500) {
                    return Result.retry();
                } else {
                    return Result.failure();
                }
            }

        } catch (Exception e) {

             if (e instanceof java.net.UnknownHostException ||
                    e instanceof java.net.SocketTimeoutException ||
                    e instanceof java.io.IOException) {
                 return Result.retry();
            } else {
                return Result.failure();
            }
        }
    }

    private boolean shouldSkipTodaysSync() {
        try {
             Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long startOfToday = today.getTimeInMillis();

             List<WeatherEntity> todaysRecords = weatherDao.getWeatherDataSince(startOfToday);

            if (!todaysRecords.isEmpty()) {
                 WeatherEntity latestRecord = todaysRecords.get(todaysRecords.size() - 1);
                long timeSinceLastUpdate = System.currentTimeMillis() - latestRecord.cachedAt;

                 if (timeSinceLastUpdate < (6 * 60 * 60 * 1000L)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
             return false;
        }
    }

    private void cleanOldData() {
        try {
            long thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L);
            weatherDao.cleanOldWeatherData(thirtyDaysAgo);
         } catch (Exception e) {
            Log.e(TAG, "Error cleaning old data", e);
        }
    }

    private void logDailyRecordsCount() {
        try {
            long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
            List<WeatherDao.DayCountResult> counts = weatherDao.getRecordsCountPerDay(sevenDaysAgo);

        } catch (Exception e) {
            Log.e(TAG, "Error logging daily records count", e);
        }
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90.0 && latitude <= 90.0 &&
                longitude >= -180.0 && longitude <= 180.0 &&
                latitude != 0.0 && longitude != 0.0;
    }
}