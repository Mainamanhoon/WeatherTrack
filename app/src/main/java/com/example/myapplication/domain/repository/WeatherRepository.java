package com.example.myapplication.domain.repository;


import androidx.lifecycle.LiveData;

import com.example.myapplication.common.Resource;
import com.example.myapplication.data.model.WeatherResponse;

import java.util.List;

public interface WeatherRepository {

    LiveData<Resource<WeatherResponse>> getCurrentWeatherByCoordinates(double latitude, double longitude);
    LiveData<List<WeatherResponse>> getLast7DaysWeather();
    WeatherResponse getCurrentWeatherSync(double latitude, double longitude) throws Exception;



}