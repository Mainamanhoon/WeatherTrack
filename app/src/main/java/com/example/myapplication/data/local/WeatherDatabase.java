package com.example.myapplication.data.local;


import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import android.content.Context;

import com.example.myapplication.data.local.converter.WeatherTypeConverter;

@Database(
        entities = {WeatherEntity.class},
        version = 1,
        exportSchema = false
)
@TypeConverters({WeatherTypeConverter.class})
public abstract class WeatherDatabase extends RoomDatabase {

 private static final String DATABASE_NAME = "weather_database";
 private static volatile WeatherDatabase INSTANCE;

 public abstract WeatherDao weatherDao();

 public static WeatherDatabase getInstance(Context context) {
  if (INSTANCE == null) {
   synchronized (WeatherDatabase.class) {
    if (INSTANCE == null) {
     INSTANCE = Room.databaseBuilder(
                     context.getApplicationContext(),
                     WeatherDatabase.class,
                     DATABASE_NAME
             )
             .fallbackToDestructiveMigration()
             .build();
    }
   }
  }
  return INSTANCE;
 }
}