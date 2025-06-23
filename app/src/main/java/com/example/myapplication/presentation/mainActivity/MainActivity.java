package com.example.myapplication.presentation.mainActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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
import com.example.myapplication.common.utils.LocationPreferences;
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
                            Toast.makeText(this, "Location permission granted. Getting your location...", Toast.LENGTH_SHORT).show();
                            getCurrentLocationAndFetchWeather();
                        } else {
                            // Check for saved location before falling back to default
                            double[] savedLocation = LocationPreferences.getLastKnownLocation(this);
                            if (savedLocation != null) {
                                showError("Location permission denied. Using your last known location.");
                                viewModel.getCurrentWeatherByCoordinates(savedLocation[0], savedLocation[1]);
                            } else {
                                showError("Location permission denied. Using default location: Bhopal, India");
                                loadWeatherWithDefaultLocation();
                            }
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
         SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM", Locale.getDefault());
        binding.tvDate.setText(dateFormat.format(new Date()));

         binding.rvForecast.setLayoutManager(new LinearLayoutManager(this));
        binding.rvForecast.setAdapter(forecastAdapter);

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

         binding.fabRefresh.setOnClickListener(v -> refreshWeatherData());

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
        showLoading(true);

        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                Log.d("MainActivity", "Location received: " + latitude + ", " + longitude);

                 viewModel.getCurrentWeatherByCoordinates(latitude, longitude);

                 WeatherWorkScheduler.updateWeatherSyncLocation(MainActivity.this, latitude, longitude);

                 Toast.makeText(MainActivity.this,
                        "Using your current location", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Log.e("MainActivity", "Location error: " + error);

                 double[] savedLocation = LocationPreferences.getLastKnownLocation(MainActivity.this);
                if (savedLocation != null) {
                    Log.d("MainActivity", "Using saved location: " + savedLocation[0] + ", " + savedLocation[1]);
                    viewModel.getCurrentWeatherByCoordinates(savedLocation[0], savedLocation[1]);

                    Toast.makeText(MainActivity.this,
                            "Using your last known location", Toast.LENGTH_SHORT).show();
                } else {
                    // Fall back to default location only if no saved location exists
                    showError("Unable to get your location. Using default location: Bhopal, India");
                    loadWeatherWithDefaultLocation();
                }

                showLoading(false);
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
            // Check if we have saved location
            double[] savedLocation = LocationPreferences.getLastKnownLocation(this);
            if (savedLocation != null) {
                viewModel.getCurrentWeatherByCoordinates(savedLocation[0], savedLocation[1]);
                Toast.makeText(this, "Using saved location", Toast.LENGTH_SHORT).show();
            } else {
                // Request permissions or use default
                showError("Location permission required for accurate weather data");
                loadWeatherWithDefaultLocation();
            }
        }
    }

    private void updateWeatherUI(WeatherResponse weather) {
        if (weather == null) return;

         hideError();

         if (weather.main != null) {
            binding.tvTemperature.setText(String.format("%.0f°C", weather.main.temp));
            binding.tvFeelsLike.setText(String.format("Feels like %.0f°C", weather.main.feels_like));
            binding.tvHighLow.setText(String.format("H:%.0f°  L:%.0f°",
                    weather.main.temp_max, weather.main.temp_min));

             binding.tvMinTemp.setText(String.format("%.0f°C", weather.main.temp_min));
            binding.tvMaxTemp.setText(String.format("%.0f°C", weather.main.temp_max));

             binding.tvHumidity.setText(weather.main.humidity + "%");
            binding.tvPressure.setText(weather.main.pressure + " hPa");
        }

         if (weather.weather != null && !weather.weather.isEmpty()) {
            String description = weather.weather.get(0).description;
            // Capitalize first letter
            if (description != null && !description.isEmpty()) {
                description = description.substring(0, 1).toUpperCase() + description.substring(1);
            }
            binding.tvWeatherDescription.setText(description);
            setWeatherIcon(weather.weather.get(0).main);
        }

         if (weather.wind != null) {
            binding.tvWindSpeed.setText(String.format("%.1f km/h", weather.wind.speed * 3.6)); // Convert m/s to km/h
            binding.tvWindDirection.setText(getWindDirection(weather.wind.deg));
        }

         if (weather.clouds != null) {
            binding.tvRainPercentage.setText(weather.clouds.all + "%");
        }

         if (weather.visibility > 0) {
            binding.tvVisibility.setText(String.format("%.1f km", weather.visibility / 1000.0));
        } else {
            binding.tvVisibility.setText("N/A");
        }

         if (weather.timezone != 0) {
            int timezoneHours = weather.timezone / 3600;
            String timezoneStr = (timezoneHours >= 0 ? "UTC+" : "UTC") + timezoneHours;
            binding.tvTimezone.setText(timezoneStr);
        } else {
            binding.tvTimezone.setText("UTC");
        }

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

         if (weather.name != null && !weather.name.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM", Locale.getDefault());
            binding.tvDate.setText(dateFormat.format(new Date()) + " • " + weather.name);
        }
    }

    private void setWeatherIcon(String weatherMain) {
        int iconRes = R.drawable.cloudy_sunny;

        if (weatherMain != null) {
            switch (weatherMain.toLowerCase()) {
                case "clear":
                    iconRes = R.drawable.sun;
                    break;
                case "clouds":
                    iconRes = R.drawable.cloudy_3;
                    break;
                case "rain":
                    iconRes = R.drawable.rainy;
                    break;
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