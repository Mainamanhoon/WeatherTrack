package com.example.myapplication.presentation.weatherDetailActivity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.model.WeatherResponse;
import com.example.myapplication.databinding.ActivityWeatherDetailBinding;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherDetailActivity extends AppCompatActivity {

    private static final String EXTRA_WEATHER_DATA = "weather_data";
    private ActivityWeatherDetailBinding binding;
    private WeatherResponse weatherData;

    public static Intent newIntent(Context context, WeatherResponse weather) {
        Intent intent = new Intent(context, WeatherDetailActivity.class);
        intent.putExtra(EXTRA_WEATHER_DATA, new Gson().toJson(weather));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeatherDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupActionBar();
        getWeatherDataFromIntent();
        setupUI();
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Weather Details");
        }
    }

    private void getWeatherDataFromIntent() {
        String weatherJson = getIntent().getStringExtra(EXTRA_WEATHER_DATA);
        if (weatherJson != null) {
            weatherData = new Gson().fromJson(weatherJson, WeatherResponse.class);
        }
    }

    private void setupUI() {
        if (weatherData == null) {
            finish();
            return;
        }

        // Set basic info
        binding.tvCityName.setText(weatherData.name != null ? weatherData.name : "Unknown Location");

        // Format date from timestamp
        if (weatherData.dt > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = new Date(weatherData.dt * 1000L);

            binding.tvDate.setText(dateFormat.format(date));
            binding.tvTime.setText("Last updated: " + timeFormat.format(date));
        }

        // Main weather info
        if (weatherData.main != null) {
            binding.tvTemperature.setText(String.format("%.0f°C", weatherData.main.temp));
            binding.tvFeelsLike.setText(String.format("Feels like %.0f°C", weatherData.main.feels_like));
            binding.tvHumidity.setText(weatherData.main.humidity + "%");
            binding.tvPressure.setText(weatherData.main.pressure + " hPa");

            if (weatherData.main.temp_min > 0 && weatherData.main.temp_max > 0) {
                binding.tvMinMaxTemp.setText(String.format("Min: %.0f°C | Max: %.0f°C",
                        weatherData.main.temp_min, weatherData.main.temp_max));
            }
        }

        // Weather description
        if (weatherData.weather != null && !weatherData.weather.isEmpty()) {
            binding.tvWeatherDescription.setText(weatherData.weather.get(0).description);
            binding.tvWeatherMain.setText(weatherData.weather.get(0).main);
            setWeatherIcon(weatherData.weather.get(0).main);
        }

        // Wind info
        if (weatherData.wind != null) {
            binding.tvWindSpeed.setText(String.format("%.1f km/h", weatherData.wind.speed * 3.6));
            binding.tvWindDirection.setText(getWindDirection(weatherData.wind.deg));
        }

        // Cloud info
        if (weatherData.clouds != null) {
            binding.tvCloudiness.setText(weatherData.clouds.all + "%");
        }

        // Visibility
        if (weatherData.visibility > 0) {
            binding.tvVisibility.setText(String.format("%.1f km", weatherData.visibility / 1000.0));
        }

        // Sunrise/Sunset
        if (weatherData.sys != null) {
            if (weatherData.sys.sunrise > 0) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                binding.tvSunrise.setText(timeFormat.format(new Date(weatherData.sys.sunrise * 1000L)));
            }
            if (weatherData.sys.sunset > 0) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                binding.tvSunset.setText(timeFormat.format(new Date(weatherData.sys.sunset * 1000L)));
            }

            if (weatherData.sys.country != null) {
                binding.tvCountry.setText(weatherData.sys.country);
            }
        }

        // Coordinates
        if (weatherData.coord != null) {
            binding.tvCoordinates.setText(String.format("Lat: %.4f, Lon: %.4f",
                    weatherData.coord.lat, weatherData.coord.lon));
        }
    }

    private void setWeatherIcon(String weatherMain) {
        int iconRes = R.drawable.cloudy_sunny; // default

        if (weatherMain != null) {
            switch (weatherMain.toLowerCase()) {
                case "clear":
                    iconRes = R.drawable.cloudy_sunny;
                    break;
                case "clouds":
                    iconRes = R.drawable.cloudy_sunny;
                    break;
                case "rain":
                case "drizzle":
                    iconRes = R.drawable.umbrella;
                    break;
                default:
                    iconRes = R.drawable.cloudy_sunny;
                    break;
            }
        }

        binding.ivWeatherIcon.setImageResource(iconRes);
    }

    private String getWindDirection(int degrees) {
        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int index = (int) ((degrees + 11.25) / 22.5) % 16;
        return directions[index] + " (" + degrees + "°)";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}