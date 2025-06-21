package com.example.myapplication.data.network;

import com.example.myapplication.data.model.WeatherResponse;

 import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("data/3.0/onecall/timemachine")
    Call<WeatherResponse> getWeatherDetails(
            @Query("lat")double lat,
            @Query("lon")double lon,
            @Query("dt")long timestamp,
            @Query("key")String key
    );
}
