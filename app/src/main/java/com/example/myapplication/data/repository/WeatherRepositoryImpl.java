package com.example.myapplication.data.repository;

import static com.example.myapplication.common.Constants.API_KEY;
import static com.example.myapplication.common.Constants.UNITS_METRIC;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.common.Resource;
import com.example.myapplication.data.local.WeatherDao;
import com.example.myapplication.data.local.WeatherEntity;
import com.example.myapplication.data.model.WeatherResponse;
import com.example.myapplication.data.network.ApiService;
import com.example.myapplication.domain.repository.WeatherRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepositoryImpl implements WeatherRepository {
    private final ApiService apiService;
    private final WeatherDao weatherDao;
    private final Executor executor;

    private final MutableLiveData<Resource<WeatherResponse>> weatherLiveData;
    private final List<Call<WeatherResponse>> activeCalls;

    @Inject
    public WeatherRepositoryImpl(ApiService apiService, WeatherDao weatherDao) {
        this.apiService = apiService;
        this.weatherDao = weatherDao;
        this.executor = Executors.newFixedThreadPool(2);
        this.weatherLiveData = new MutableLiveData<>();
        this.activeCalls = new ArrayList<>();
    }

    @Override
    public LiveData<Resource<WeatherResponse>> getCurrentWeatherByCoordinates(double latitude, double longitude) {
        if (!isValidCoordinate(latitude, longitude)) {
            weatherLiveData.setValue(Resource.error("Invalid Coordinates", null));
            return weatherLiveData;
        }

        weatherLiveData.setValue(Resource.loading());
        Call<WeatherResponse> call = apiService.getCurrentWeather(latitude, longitude, API_KEY, UNITS_METRIC);

        activeCalls.add(call);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                activeCalls.remove(call);
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherResponse = response.body();

                     executor.execute(() -> {
                        try {
                            WeatherEntity entity = new WeatherEntity(weatherResponse);
                            weatherDao.insertWeatherData(entity);
                        } catch (Exception e) {
                            android.util.Log.e("WeatherRepo", "Error saving weather data", e);
                        }
                    });

                    weatherLiveData.setValue(Resource.success(weatherResponse));
                } else {
                    String errorMessage = getErrorMessage(response.code());
                    weatherLiveData.setValue(Resource.error(errorMessage, null));
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                activeCalls.remove(call);
                if (!call.isCanceled()) {
                    String errorMessage = getNetworkErrorMessage(t);
                    weatherLiveData.setValue(Resource.error(errorMessage, null));
                }
            }
        });
        return weatherLiveData;
    }

    @Override
    public LiveData<List<WeatherResponse>> getLast7DaysWeather() {
        MutableLiveData<List<WeatherResponse>> result = new MutableLiveData<>();

        executor.execute(() -> {
            try {
                 long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);

                 List<WeatherEntity> entities = weatherDao.getLast7DaysLatestWeatherPerDay(sevenDaysAgo);

                 List<WeatherResponse> weatherList = new ArrayList<>();
                for (WeatherEntity entity : entities) {
                    weatherList.add(entity.toWeatherResponse());
                }

                 result.postValue(weatherList);

            } catch (Exception e) {
                 result.postValue(new ArrayList<>());
            }
        });

        return result;
    }

    @Override
    public WeatherResponse getCurrentWeatherSync(double latitude, double longitude) throws Exception {
        return null;
    }

     public void cleanOldWeatherData() {
        executor.execute(() -> {
            try {
                long thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L);
                weatherDao.cleanOldWeatherData(thirtyDaysAgo);
             } catch (Exception e) {
                android.util.Log.e("WeatherRepo", "Error cleaning old data", e);
            }
        });
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
            case 400:
                return "Invalid request parameters";
            case 401:
                return "API key is invalid or expired";
            case 403:
                return "Access denied to weather service";
            case 404:
                return "Location not found";
            case 429:
                return "Too many requests. Please wait and try again";
            case 500:
                return "Weather service is temporarily unavailable";
            case 502:
            case 503:
            case 504:
                return "Weather service is down for maintenance";
            default:
                if (statusCode >= 400 && statusCode < 500) {
                    return "Client error occurred";
                } else if (statusCode >= 500) {
                    return "Server error occurred";
                } else {
                    return "Network error occurred";
                }
        }
    }

    private String getNetworkErrorMessage(Throwable t) {
        if (t instanceof java.net.UnknownHostException) {
            return "No internet connection available";
        } else if (t instanceof java.net.SocketTimeoutException) {
            return "Connection timed out. Please try again";
        } else if (t instanceof java.net.ConnectException) {
            return "Unable to connect to weather service";
        } else if (t instanceof java.io.IOException) {
            return "Network error occurred";
        } else {
            return "An unexpected error occurred";
        }
    }
}