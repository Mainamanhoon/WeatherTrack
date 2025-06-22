package com.example.myapplication.data.repository;

import static com.example.myapplication.common.Constants.API_KEY;
import static com.example.myapplication.common.Constants.UNITS_METRIC;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.common.Resource;
import com.example.myapplication.data.model.WeatherResponse;
import com.example.myapplication.data.network.ApiService;
import com.example.myapplication.domain.WeatherRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepositoryImpl implements WeatherRepository {
    private final ApiService apiService;
    private final MutableLiveData<Resource<WeatherResponse>> weatherLiveData;
    private final List<Call<WeatherResponse>> activeCalls;


    @Inject
    public WeatherRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
        this.weatherLiveData = new MutableLiveData<>();
        this.activeCalls = new ArrayList<>();
    }


    @Override
    public LiveData<Resource<WeatherResponse>> getCurrentWeatherByCoordinates(double latitude, double longitude) {
        if(!isValidCoordinate(latitude,longitude)){
            weatherLiveData.setValue(Resource.error("Invalid Coordinates",null));
        }
        weatherLiveData.setValue(Resource.loading());
        Call<WeatherResponse> call = apiService.getCurrentWeather(latitude,longitude,API_KEY,UNITS_METRIC);

        activeCalls.add(call);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                activeCalls.remove(call);
                if(response.isSuccessful() && response.body()!=null){
                    weatherLiveData.setValue(Resource.success(response.body()));
                } else{
                    String errorMessage = getErrorMessage(response.code());
                    weatherLiveData.setValue((Resource.error(errorMessage,null)));
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                activeCalls.remove(call);

                if(!call.isCanceled()){
                    String errorMessage = getErrorMessage(t.hashCode());
                    weatherLiveData.setValue(Resource.error(errorMessage,null));
                }

            }
        });
        return weatherLiveData;
    }
     public void cancelAllRequests() {
        for (Call<WeatherResponse> call : activeCalls) {
            if (!call.isCanceled()) {
                call.cancel();
            }
        }
        activeCalls.clear();
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }


    private String getErrorMessage(int statusCode) {
        switch (statusCode) {
            case 401:
                return "Invalid API key";
            case 404:
                return "Location not found";
            case 500:
                return "Server error";
            default:
                return "Unknown error occurred";
        }
    }

}
