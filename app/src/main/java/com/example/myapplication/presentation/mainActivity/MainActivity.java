package com.example.myapplication.presentation.mainActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.common.Constants;
import com.example.myapplication.common.Resource;
import com.example.myapplication.common.utils.LocationHelper;
import com.example.myapplication.common.worker.WeatherWorkScheduler;
import com.example.myapplication.data.model.WeatherResponse;
import com.example.myapplication.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainActivityViewmodel viewModel;
    private LocationHelper locationHelper;
    private WeatherForecastAdapter forecastAdapter;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean fineLocationGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                        boolean coarseLocationGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                        if (fineLocationGranted || coarseLocationGranted) {
                            getCurrentLocationAndFetchWeather();
                        } else {
                            showError("Location permission is required to get weather data");
                            loadWeatherWithDefaultLocation();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        initializeComponents();
        setupUI();
        setupObservers();
        checkLocationPermissionAndFetchWeather();
    }

    private void initializeComponents() {
        viewModel = new ViewModelProvider(this).get(MainActivityViewmodel.class);
        locationHelper = new LocationHelper(this);
        forecastAdapter = new WeatherForecastAdapter();
    }

    private void setupUI() {
        // Set current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM", Locale.getDefault());
        binding.tvDate.setText(dateFormat.format(new Date()));

        // Setup RecyclerView
        binding.rvForecast.setLayoutManager(new LinearLayoutManager(this));
        binding.rvForecast.setAdapter(forecastAdapter);

        // Setup SwipeRefreshLayout and other UI elements
        setupSwipeRefresh();
    }

    private void setupSwipeRefresh() {

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshWeatherData();
        });

        binding.swipeRefreshLayout.setColorSchemeResources(
                R.color.yellow,
                R.color.white
        );

        // Setup FAB click listener
        binding.fabRefresh.setOnClickListener(v -> refreshWeatherData());

        // Setup retry button click listener
        binding.btnRetry.setOnClickListener(v -> {
            hideError();
            checkLocationPermissionAndFetchWeather();
        });
    }

    private void setupObservers() {
        // Observe current weather data
        viewModel.getCurrentWeatherData().observe(this, resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        showLoading(true);
                        break;
                    case SUCCESS:
                        showLoading(false);
                        updateWeatherUI(resource.getData());
                        break;
                    case ERROR:
                        showLoading(false);
                        showError(resource.getMessage());
                        break;
                }
            }
        });

        // Observe forecast data
        viewModel.getLast7DaysWeather().observe(this, weatherList -> {
            if (weatherList != null && !weatherList.isEmpty()) {
                forecastAdapter.updateWeatherList(weatherList);
                binding.rvForecast.setVisibility(View.VISIBLE);
                binding.emptyForecastLayout.setVisibility(View.GONE);
            } else {
                binding.rvForecast.setVisibility(View.GONE);
                binding.emptyForecastLayout.setVisibility(View.VISIBLE);
            }
        });

        // Observe loading state
        viewModel.getLoadingState().observe(this, isLoading -> {
            showLoading(isLoading != null && isLoading);
        });
    }

    private void checkLocationPermissionAndFetchWeather() {
        if (hasLocationPermissions()) {
            getCurrentLocationAndFetchWeather();
        } else {
            requestLocationPermissions();
        }
    }

    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void getCurrentLocationAndFetchWeather() {
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                viewModel.getCurrentWeatherByCoordinates(latitude, longitude);
                // Update WorkManager with current location
                WeatherWorkScheduler.updateWeatherSyncLocation(MainActivity.this, latitude, longitude);
            }

            @Override
            public void onError(String error) {
                showError("Failed to get location: " + error);
                loadWeatherWithDefaultLocation();
            }
        });
    }

    private void loadWeatherWithDefaultLocation() {
        viewModel.getCurrentWeatherByCoordinates(
                Constants.DEFAULT_LATITUDE,
                Constants.DEFAULT_LONGITUDE
        );
    }

    private void refreshWeatherData() {
        if (hasLocationPermissions()) {
            getCurrentLocationAndFetchWeather();
        } else {
            viewModel.refreshWeather();
        }
    }

    private void updateWeatherUI(WeatherResponse weather) {
        if (weather == null) return;

        // Show content and hide error
        hideError();

        // Update main temperature section
        if (weather.main != null) {
            binding.tvTemperature.setText(String.format("%.0f°C", weather.main.temp));
            binding.tvFeelsLike.setText(String.format("Feels like %.0f°C", weather.main.feels_like));
            binding.tvHighLow.setText(String.format("H:%.0f°  L:%.0f°",
                    weather.main.temp_max, weather.main.temp_min));

            // Individual min/max temperatures
            binding.tvMinTemp.setText(String.format("%.0f°C", weather.main.temp_min));
            binding.tvMaxTemp.setText(String.format("%.0f°C", weather.main.temp_max));

            // Humidity and Pressure
            binding.tvHumidity.setText(weather.main.humidity + "%");
            binding.tvPressure.setText(weather.main.pressure + " hPa");
        }

        // Update weather description
        if (weather.weather != null && !weather.weather.isEmpty()) {
            String description = weather.weather.get(0).description;
            // Capitalize first letter
            if (description != null && !description.isEmpty()) {
                description = description.substring(0, 1).toUpperCase() + description.substring(1);
            }
            binding.tvWeatherDescription.setText(description);
            setWeatherIcon(weather.weather.get(0).main);
        }

        // Update wind information
        if (weather.wind != null) {
            binding.tvWindSpeed.setText(String.format("%.1f km/h", weather.wind.speed * 3.6)); // Convert m/s to km/h
            binding.tvWindDirection.setText(getWindDirection(weather.wind.deg));
        }

        // Update cloudiness (using clouds as rain percentage approximation)
        if (weather.clouds != null) {
            binding.tvRainPercentage.setText(weather.clouds.all + "%");
        }

        // Update visibility
        if (weather.visibility > 0) {
            binding.tvVisibility.setText(String.format("%.1f km", weather.visibility / 1000.0));
        } else {
            binding.tvVisibility.setText("N/A");
        }

        // Update timezone
        if (weather.timezone != 0) {
            int timezoneHours = weather.timezone / 3600;
            String timezoneStr = (timezoneHours >= 0 ? "UTC+" : "UTC") + timezoneHours;
            binding.tvTimezone.setText(timezoneStr);
        } else {
            binding.tvTimezone.setText("UTC");
        }

        // Update sunrise and sunset
        if (weather.sys != null) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            if (weather.sys.sunrise > 0) {
                binding.tvSunrise.setText(timeFormat.format(new Date(weather.sys.sunrise * 1000L)));
            } else {
                binding.tvSunrise.setText("--:--");
            }

            if (weather.sys.sunset > 0) {
                binding.tvSunset.setText(timeFormat.format(new Date(weather.sys.sunset * 1000L)));
            } else {
                binding.tvSunset.setText("--:--");
            }
        }

        // Update city name in date text
        if (weather.name != null && !weather.name.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM", Locale.getDefault());
            binding.tvDate.setText(dateFormat.format(new Date()) + " • " + weather.name);
        }
    }

    private void setWeatherIcon(String weatherMain) {
        int iconRes = R.drawable.cloudy_sunny; // default

        if (weatherMain != null) {
            switch (weatherMain.toLowerCase()) {
                case "clear":
                    iconRes = R.drawable.cloudy_sunny; // You might want to add a sunny icon
                    break;
                case "clouds":
                    iconRes = R.drawable.cloudy_sunny;
                    break;
                case "rain":
                case "drizzle":
                    iconRes = R.drawable.umbrella;
                    break;
                // Add more weather conditions as needed
                default:
                    iconRes = R.drawable.cloudy_sunny;
                    break;
            }
        }

        binding.ivWeatherIcon.setImageResource(iconRes);
    }

    private String getWindDirection(int degrees) {
        if (degrees < 0) return "N/A";

        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int index = (int) ((degrees + 11.25) / 22.5) % 16;
        return directions[index];
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.swipeRefreshLayout.setRefreshing(show);

        if (show) {
            hideError();
        }
    }

    private void showError(String message) {
        binding.contentLayout.setVisibility(View.GONE);
        binding.errorLayout.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
        binding.swipeRefreshLayout.setRefreshing(false);

        if (message != null) {
            binding.tvErrorMessage.setText(message);

            // Set appropriate error description based on the error
            if (message.toLowerCase().contains("network") ||
                    message.toLowerCase().contains("internet") ||
                    message.toLowerCase().contains("connection")) {
                binding.tvErrorDescription.setText("Please check your internet connection and try again");
            } else if (message.toLowerCase().contains("location")) {
                binding.tvErrorDescription.setText("Unable to determine your location. Using default location.");
            } else if (message.toLowerCase().contains("api") ||
                    message.toLowerCase().contains("server")) {
                binding.tvErrorDescription.setText("Weather service is temporarily unavailable");
            } else {
                binding.tvErrorDescription.setText("Something went wrong. Please try again.");
            }
        }
    }

    private void hideError() {
        binding.errorLayout.setVisibility(View.GONE);
        binding.contentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}