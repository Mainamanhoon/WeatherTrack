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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import retrofit2.Response;

@HiltWorker
public class WeatherSyncWorker extends Worker {
    private static final String TAG = "WeatherSyncWorker";

    private final ApiService apiService;
    private final WeatherDao weatherDao;
    private final Context context;

    @AssistedInject
    public WeatherSyncWorker(
            @Assisted Context context,
            @Assisted WorkerParameters params,
            ApiService apiService,
            WeatherDao weatherDao) {
        super(context, params);
        this.context = context;
        this.apiService = apiService;
        this.weatherDao = weatherDao;

        Log.d(TAG, "🔧 WeatherSyncWorker instance created");
    }

    @NonNull
    @Override
    public Result doWork() {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        Log.i(TAG, "🚀 ============ WEATHER SYNC STARTED ============");
        Log.i(TAG, "⏰ Timestamp: " + timestamp);
        Log.i(TAG, "🔄 Attempt #" + getRunAttemptCount());

        try {
            // Log worker input data
            logInputData();

            // Get coordinates from input data or use defaults
            double latitude = getInputData().getDouble("latitude", Constants.DEFAULT_LATITUDE);
            double longitude = getInputData().getDouble("longitude", Constants.DEFAULT_LONGITUDE);

            Log.i(TAG, "📍 Coordinates: lat=" + latitude + ", lon=" + longitude);

            // Check if coordinates are default (might indicate location issue)
            if (latitude == Constants.DEFAULT_LATITUDE && longitude == Constants.DEFAULT_LONGITUDE) {
                Log.w(TAG, "⚠️ Using default coordinates (NYC) - location might not be available");
            }

            // Log network attempt
            Log.d(TAG, "🌐 Making API call to OpenWeatherMap...");
            Log.d(TAG, "📡 API URL: " + Constants.BASE_URL + "weather");
            Log.d(TAG, "🔑 API Key: " + Constants.API_KEY.substring(0, 8) + "..."); // Only show first 8 chars for security

            // Make synchronous API call
            Response<WeatherResponse> response = apiService.getCurrentWeather(
                    latitude,
                    longitude,
                    Constants.API_KEY,
                    Constants.UNITS_METRIC
            ).execute();

            // Log response details
            Log.d(TAG, "📨 Response Code: " + response.code());
            Log.d(TAG, "📨 Response Message: " + response.message());
            Log.d(TAG, "📨 Response Headers: " + response.headers().toString());

            if (response.isSuccessful() && response.body() != null) {
                WeatherResponse weatherData = response.body();

                // Log weather data details
                logWeatherData(weatherData);

                // Save to database
                Log.d(TAG, "💾 Saving weather data to database...");
                WeatherEntity entity = new WeatherEntity(weatherData);
                weatherDao.insertWeatherData(entity);

                // Verify database save
                WeatherEntity savedEntity = weatherDao.getCachedWeatherByLocation(latitude, longitude);
                if (savedEntity != null) {
                    Log.i(TAG, "✅ Weather data successfully saved to database");
                    Log.d(TAG, "💾 Database ID: " + savedEntity.dbId);
                    Log.d(TAG, "💾 Cache timestamp: " + new Date(savedEntity.cachedAt));
                } else {
                    Log.e(TAG, "❌ Failed to verify database save");
                }

                Log.i(TAG, "🎉 ============ WEATHER SYNC COMPLETED SUCCESSFULLY ============");
                return Result.success();

            } else {
                Log.e(TAG, "❌ API call failed");
                Log.e(TAG, "📨 Response Code: " + response.code());
                Log.e(TAG, "📨 Error Body: " + (response.errorBody() != null ? response.errorBody().toString() : "null"));

                if (response.code() >= 400 && response.code() < 500) {
                    Log.e(TAG, "🚫 Client error - will not retry");
                    return Result.failure();
                } else {
                    Log.w(TAG, "🔄 Server error - will retry");
                    return Result.retry();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 Exception during weather sync", e);
            Log.e(TAG, "Exception Type: " + e.getClass().getSimpleName());
            Log.e(TAG, "Exception Message: " + e.getMessage());

            // Detailed exception handling
            if (e instanceof java.net.UnknownHostException) {
                Log.e(TAG, "🌐 Network error: No internet connection");
            } else if (e instanceof java.net.SocketTimeoutException) {
                Log.e(TAG, "⏰ Network error: Connection timeout");
            } else if (e instanceof java.io.IOException) {
                Log.e(TAG, "📡 Network error: IO Exception");
            }

            Log.e(TAG, "🔄 Will retry due to exception");
            return Result.retry();
        } finally {
            String endTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            Log.i(TAG, "🏁 Weather sync ended at: " + endTimestamp);
        }
    }

    private void logInputData() {
        Log.d(TAG, "📋 Worker Input Data:");
        for (String key : getInputData().getKeyValueMap().keySet()) {
            Object value = getInputData().getKeyValueMap().get(key);
            Log.d(TAG, "   " + key + " = " + value);
        }

        if (getInputData().getKeyValueMap().isEmpty()) {
            Log.w(TAG, "⚠️ No input data provided to worker");
        }
    }

    private void logWeatherData(WeatherResponse weather) {
        Log.i(TAG, "🌤️ ========== WEATHER DATA RECEIVED ==========");
        Log.i(TAG, "🏙️ City: " + (weather.name != null ? weather.name : "Unknown"));
        Log.i(TAG, "🌍 Country: " + (weather.sys != null && weather.sys.country != null ? weather.sys.country : "Unknown"));

        if (weather.main != null) {
            Log.i(TAG, "🌡️ Temperature: " + weather.main.temp + "°C");
            Log.i(TAG, "🤔 Feels like: " + weather.main.feels_like + "°C");
            Log.i(TAG, "📊 Humidity: " + weather.main.humidity + "%");
            Log.i(TAG, "🔽 Pressure: " + weather.main.pressure + " hPa");
        }

        if (weather.weather != null && !weather.weather.isEmpty()) {
            Log.i(TAG, "☁️ Weather: " + weather.weather.get(0).main);
            Log.i(TAG, "📝 Description: " + weather.weather.get(0).description);
        }

        if (weather.wind != null) {
            Log.i(TAG, "💨 Wind Speed: " + weather.wind.speed + " m/s");
            Log.i(TAG, "🧭 Wind Direction: " + weather.wind.deg + "°");
        }

        if (weather.clouds != null) {
            Log.i(TAG, "☁️ Cloudiness: " + weather.clouds.all + "%");
        }

        Log.i(TAG, "👁️ Visibility: " + weather.visibility + " meters");
        Log.i(TAG, "📅 Data timestamp: " + new Date(weather.dt * 1000L));
        Log.i(TAG, "🕐 Timezone: UTC+" + (weather.timezone / 3600));
        Log.i(TAG, "=========================================");
    }
}