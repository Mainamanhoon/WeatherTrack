package com.example.myapplication.di;

import com.example.myapplication.data.repository.WeatherRepositoryImpl;
import com.example.myapplication.domain.repository.WeatherRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public abstract class RepositoryModule {
    @Provides
    @Singleton
    public static WeatherRepository providesWeatherRepository(WeatherRepositoryImpl impl){
        return impl;
    }
}
