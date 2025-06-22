package com.example.myapplication.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapplication.data.model.WeatherResponse;

import java.util.List;

@Dao
public interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWeatherData(WeatherEntity weatherEntity);

    @Query("SELECT * FROM weather_cache WHERE " +
            "ABS(latitude - :lat) < 0.01 AND " +
            "ABS(longitude - :lon) < 0.01 " +
            "ORDER BY cachedAt DESC LIMIT 1")
    WeatherEntity getCachedWeatherByLocation(double lat, double lon);


    @Query("SELECT * FROM weather_cache WHERE " +
            "ABS(latitude - :lat) < 0.01 AND " +
            "ABS(longitude - :lon) < 0.01 AND " +
            "cachedAt >= :startTime " +
            "ORDER BY cachedAt ASC")
    List<WeatherEntity> getLocationWeatherSince(double lat, double lon, long startTime);


    @Query("SELECT * FROM weather_cache WHERE " +
            "cachedAt >= :startTime " +
            "ORDER BY cachedAt ASC")
    List<WeatherEntity> getWeatherDataSince(long startTime);

//     @Query("SELECT cachedAt, temperature FROM weather_cache WHERE " +
//            "ABS(latitude - :lat) < 0.01 AND " +
//            "ABS(longitude - :lon) < 0.01 AND " +
//            "cachedAt >= :startTime " +
//            "ORDER BY cachedAt ASC")
//    List<WeatherResponse> getTemperatureReadings(double lat, double lon, long startTime);
}

