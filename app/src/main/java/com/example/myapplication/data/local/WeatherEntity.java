package com.example.myapplication.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.myapplication.data.local.converter.WeatherTypeConverter;
import com.example.myapplication.data.model.Clouds;
import com.example.myapplication.data.model.Coord;
import com.example.myapplication.data.model.Main;
import com.example.myapplication.data.model.Sys;
import com.example.myapplication.data.model.Weather;
import com.example.myapplication.data.model.WeatherResponse;
import com.example.myapplication.data.model.Wind;

import java.util.List;

@Entity(tableName = "weather_cache")
@TypeConverters(WeatherTypeConverter.class)
public class WeatherEntity {

    @PrimaryKey(autoGenerate = true)
    public int dbId;

    // Original API fields stored as JSON
    public long id;
    public Coord coord;
    public List<Weather> weather;
    public String base;
    public Main main;
    public int visibility;
    public Wind wind;
    public Clouds clouds;
    public long dt;
    public Sys sys;
    public int timezone;
    public String name;
    public int cod;

     public double latitude;
    public double longitude;
    public double temperature;
    public long cachedAt;

    public WeatherEntity() {}

    public WeatherEntity(WeatherResponse response) {
        // Copy all original fields
        this.id = response.id;
        this.coord = response.coord;
        this.weather = response.weather;
        this.base = response.base;
        this.main = response.main;
        this.visibility = response.visibility;
        this.wind = response.wind;
        this.clouds = response.clouds;
        this.dt = response.dt;
        this.sys = response.sys;
        this.timezone = response.timezone;
        this.name = response.name;
        this.cod = response.cod;

         if (response.coord != null) {
            this.latitude = response.coord.lat;
            this.longitude = response.coord.lon;
        }

        if (response.main != null) {
            this.temperature = response.main.temp;
        }

        this.cachedAt = System.currentTimeMillis();
    }



    public WeatherResponse toWeatherResponse() {
        WeatherResponse response = new WeatherResponse();
        response.id = this.id;
        response.coord = this.coord;
        response.weather = this.weather;
        response.base = this.base;
        response.main = this.main;
        response.visibility = this.visibility;
        response.wind = this.wind;
        response.clouds = this.clouds;
        response.dt = this.dt;
        response.sys = this.sys;
        response.timezone = this.timezone;
        response.name = this.name;
        response.cod = this.cod;
        return response;
    }


    public boolean matchesLocation(double lat, double lon) {
        if (coord == null) return false;
        return Math.abs(coord.lat - lat) < 0.01 && Math.abs(coord.lon - lon) < 0.01;
    }

    public long getCacheAge() {
        return System.currentTimeMillis() - cachedAt;
    }
}