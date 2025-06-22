package com.example.myapplication.domain;


import androidx.lifecycle.LiveData;

import com.example.myapplication.common.Resource;
import com.example.myapplication.data.model.WeatherResponse;

public interface WeatherRepository {

    LiveData<Resource<WeatherResponse>> getCurrentWeatherByCoordinates(double latitude, double longitude);



}