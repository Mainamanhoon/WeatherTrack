package com.example.myapplication.presentation.mainActivity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.common.Resource;
import com.example.myapplication.data.local.WeatherDao;
import com.example.myapplication.data.model.WeatherResponse;
import com.example.myapplication.domain.WeatherRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainActivityViewmodel extends ViewModel {

    private final WeatherRepository weatherRepository;
    private final WeatherDao weatherDao;
    private final MediatorLiveData<Resource<WeatherResponse>> currentWeatherData;
    private final MediatorLiveData<Boolean> isLoading;

    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private boolean hasValidLocation = false;

    @Inject
    public MainActivityViewmodel(WeatherRepository weatherRepository, WeatherDao weatherDao) {
        this.weatherRepository = weatherRepository;
        this.weatherDao = weatherDao;

        this.currentWeatherData = new MediatorLiveData<>();
        this.isLoading = new MediatorLiveData<>();

        isLoading.setValue(false);
    }

    public void getCurrentWeatherByCoordinates(double latitude, double longitude) {
        if (!isValidCoordinate(latitude, longitude)) {
            currentWeatherData.setValue(Resource.error("Invalid coordinates", null));
            return;
        }

        // Store location
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        this.hasValidLocation = true;

        // Get data from repository
        LiveData<Resource<WeatherResponse>> source = weatherRepository.getCurrentWeatherByCoordinates(latitude, longitude);

        currentWeatherData.removeSource(source);

        currentWeatherData.addSource(source, resource -> {
            currentWeatherData.setValue(resource);

            if (resource != null) {
                isLoading.setValue(resource.getStatus() == Resource.Status.LOADING);
            }
        });
    }

    public void refreshWeather() {
        if (hasValidLocation) {
            getCurrentWeatherByCoordinates(currentLatitude, currentLongitude);
        } else {
            currentWeatherData.setValue(Resource.error("No location available", null));
        }
    }

    public LiveData<Resource<WeatherResponse>> getCurrentWeatherData() {
        return currentWeatherData;
    }

    public LiveData<List<WeatherResponse>> getLast7DaysWeather() {
        return weatherRepository.getLast7DaysWeather();
    }

    public LiveData<Boolean> getLoadingState() {
        return isLoading;
    }

    public boolean isCurrentWeatherAvailable() {
        Resource<WeatherResponse> data = currentWeatherData.getValue();
        return data != null && data.isSuccess() && data.getData() != null;
    }

    public String getCurrentTemperature() {
        if (isCurrentWeatherAvailable()) {
            WeatherResponse weather = currentWeatherData.getValue().getData();
            if (weather != null && weather.main != null) {
                return String.format("%.1f°C", weather.main.temp);
            }
        }
        return "N/A";
    }

    public String getCurrentCityName() {
        if (isCurrentWeatherAvailable()) {
            WeatherResponse weather = currentWeatherData.getValue().getData();
            if (weather != null) {
                return weather.name;
            }
        }
        return "Unknown";
    }

    public String getCurrentWeatherDescription() {
        if (isCurrentWeatherAvailable()) {
            WeatherResponse weather = currentWeatherData.getValue().getData();
            if (weather != null && weather.weather != null && !weather.weather.isEmpty()) {
                return weather.weather.get(0).description;
            }
        }
        return "Unknown";
    }

    public double getCurrentLatitude() {
        return currentLatitude;
    }

    public double getCurrentLongitude() {
        return currentLongitude;
    }

    public boolean hasLocation() {
        return hasValidLocation;
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90.0 && latitude <= 90.0 && longitude >= -180.0 && longitude <= 180.0;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up any resources if needed
    }
}