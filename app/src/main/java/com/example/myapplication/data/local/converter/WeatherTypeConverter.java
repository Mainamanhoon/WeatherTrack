package com.example.myapplication.data.local.converter;

import androidx.room.TypeConverter;

import com.example.myapplication.data.model.Clouds;
import com.example.myapplication.data.model.Coord;
import com.example.myapplication.data.model.Main;
import com.example.myapplication.data.model.Sys;
import com.example.myapplication.data.model.Weather;
import com.example.myapplication.data.model.Wind;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class WeatherTypeConverter {

    private static final Gson gson = new Gson();

    // Coord converters
    @TypeConverter
    public static String fromCoord(Coord coord) {
        return coord == null ? null : gson.toJson(coord);
    }

    @TypeConverter
    public static Coord toCoord(String coordString) {
        return coordString == null ? null : gson.fromJson(coordString, Coord.class);
    }

    // Weather List converters
    @TypeConverter
    public static String fromWeatherList(List<Weather> weatherList) {
        return weatherList == null ? null : gson.toJson(weatherList);
    }

    @TypeConverter
    public static List<Weather> toWeatherList(String weatherListString) {
        if (weatherListString == null) return null;
        Type listType = new TypeToken<List<Weather>>() {}.getType();
        return gson.fromJson(weatherListString, listType);
    }

    // Main converters
    @TypeConverter
    public static String fromMain(Main main) {
        return main == null ? null : gson.toJson(main);
    }

    @TypeConverter
    public static Main toMain(String mainString) {
        return mainString == null ? null : gson.fromJson(mainString, Main.class);
    }

    // Wind converters
    @TypeConverter
    public static String fromWind(Wind wind) {
        return wind == null ? null : gson.toJson(wind);
    }

    @TypeConverter
    public static Wind toWind(String windString) {
        return windString == null ? null : gson.fromJson(windString, Wind.class);
    }

    // Clouds converters
    @TypeConverter
    public static String fromClouds(Clouds clouds) {
        return clouds == null ? null : gson.toJson(clouds);
    }

    @TypeConverter
    public static Clouds toClouds(String cloudsString) {
        return cloudsString == null ? null : gson.fromJson(cloudsString, Clouds.class);
    }

    // Sys converters
    @TypeConverter
    public static String fromSys(Sys sys) {
        return sys == null ? null : gson.toJson(sys);
    }

    @TypeConverter
    public static Sys toSys(String sysString) {
        return sysString == null ? null : gson.fromJson(sysString, Sys.class);
    }
}