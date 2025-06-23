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

    // New query to get one record per day for the last 7 days
    @Query("SELECT * FROM weather_cache WHERE " +
            "cachedAt >= :startTime " +
            "GROUP BY date(cachedAt/1000, 'unixepoch', 'localtime') " +
            "ORDER BY cachedAt DESC " +
            "LIMIT 7")
    List<WeatherEntity> getLast7DaysWeatherGroupedByDay(long startTime);

    // Alternative query for better performance - gets the latest record for each day
    @Query("SELECT w1.* FROM weather_cache w1 " +
            "INNER JOIN (" +
            "    SELECT date(cachedAt/1000, 'unixepoch', 'localtime') as day, " +
            "           MAX(cachedAt) as maxTime " +
            "    FROM weather_cache " +
            "    WHERE cachedAt >= :startTime " +
            "    GROUP BY date(cachedAt/1000, 'unixepoch', 'localtime')" +
            ") w2 ON date(w1.cachedAt/1000, 'unixepoch', 'localtime') = w2.day " +
            "    AND w1.cachedAt = w2.maxTime " +
            "ORDER BY w1.cachedAt DESC " +
            "LIMIT 7")
    List<WeatherEntity> getLast7DaysLatestWeatherPerDay(long startTime);

    // Clean up old data (optional - keeps only last 30 days)
    @Query("DELETE FROM weather_cache WHERE cachedAt < :thirtyDaysAgo")
    void cleanOldWeatherData(long thirtyDaysAgo);

    // Get count of records per day (for debugging)
    @Query("SELECT date(cachedAt/1000, 'unixepoch', 'localtime') as day, " +
            "COUNT(*) as count " +
            "FROM weather_cache " +
            "WHERE cachedAt >= :startTime " +
            "GROUP BY date(cachedAt/1000, 'unixepoch', 'localtime') " +
            "ORDER BY day DESC")
    List<DayCountResult> getRecordsCountPerDay(long startTime);

    class DayCountResult {
        public String day;
        public int count;
    }
}