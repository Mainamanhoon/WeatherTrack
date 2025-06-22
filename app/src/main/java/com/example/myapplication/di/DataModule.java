package com.example.myapplication.di;

import android.content.Context;

import com.example.myapplication.data.local.WeatherDao;
import com.example.myapplication.data.local.WeatherDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public abstract class DataModule {

    @Provides
    @Singleton
    public static WeatherDatabase providesWeatherDatabase(@ApplicationContext Context context){
        return WeatherDatabase.getInstance(context);
    }

    @Provides
    @Singleton
    public static WeatherDao providesWeatherDao(WeatherDatabase database){
        return database.weatherDao();
    }

}
