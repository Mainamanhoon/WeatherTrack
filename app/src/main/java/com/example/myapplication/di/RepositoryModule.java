package com.example.myapplication.di;

import static com.example.myapplication.common.Constants.BASE_URL;

import com.example.myapplication.data.repository.WeatherRepositoryImpl;
import com.example.myapplication.domain.WeatherRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@InstallIn(SingletonComponent.class)
@Module
public abstract class RepositoryModule {
    @Provides
    @Singleton
    public static WeatherRepository providesWeatherRepository(WeatherRepositoryImpl impl){
        return impl;
    }
}
