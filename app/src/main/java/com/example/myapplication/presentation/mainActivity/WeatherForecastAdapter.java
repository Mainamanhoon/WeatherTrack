package com.example.myapplication.presentation.mainActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.WeatherResponse;
import com.example.myapplication.presentation.weatherDetailActivity.WeatherDetailActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastAdapter.ViewHolder> {

    private List<WeatherResponse> weatherList = new ArrayList<>();

    public void updateWeatherList(List<WeatherResponse> newList) {
        this.weatherList.clear();
        if (newList != null) {
            this.weatherList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherResponse weather = weatherList.get(position);
        holder.bind(weather, position);
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDayLabel, tvTemperature;
        ImageView ivWeatherIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDayLabel = itemView.findViewById(R.id.tvDayLabel);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);

            // Set click listener for the entire item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < weatherList.size()) {
                    WeatherResponse selectedWeather = weatherList.get(position);
                    itemView.getContext().startActivity(
                            WeatherDetailActivity.newIntent(itemView.getContext(), selectedWeather)
                    );
                }
            });
        }

        public void bind(WeatherResponse weather, int position) {
            // Format date from timestamp
            String dateStr = formatDate(weather.dt * 1000); // dt is in seconds
            tvDate.setText(dateStr);

            // Set day label
            String dayLabel = getDayLabel(position);
            tvDayLabel.setText(dayLabel);

            // Set temperature (using temp and feels_like)
            if (weather.main != null) {
                int temp = (int) Math.round(weather.main.temp);
                int feelsLike = (int) Math.round(weather.main.feels_like);
                tvTemperature.setText(temp + "° " + feelsLike + "°");
            }

            // Set weather icon
            if (weather.weather != null && !weather.weather.isEmpty()) {
                String mainWeather = weather.weather.get(0).main;
                setWeatherIcon(mainWeather);
            }
        }

        private String formatDate(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }

        private String getDayLabel(int position) {
            // Since we're showing past 7 days, we need to go backwards from today
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -(weatherList.size() - 1 - position)); // Reverse order

            Calendar today = Calendar.getInstance();
            long diffDays = (today.getTimeInMillis() - cal.getTimeInMillis()) / (24 * 60 * 60 * 1000);

            if (diffDays == 0) return "Today";
            if (diffDays == 1) return "Yesterday";
            if (diffDays < 7) return diffDays + " days ago";

            SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
            return sdf.format(cal.getTime());
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
                    case "thunderstorm":
                        iconRes = R.drawable.umbrella; // You might want to add a thunderstorm icon
                        break;
                    case "snow":
                        iconRes = R.drawable.cloudy_sunny; // You might want to add a snow icon
                        break;
                    case "mist":
                    case "smoke":
                    case "haze":
                    case "dust":
                    case "fog":
                    case "sand":
                    case "ash":
                    case "squall":
                    case "tornado":
                        iconRes = R.drawable.cloudy_sunny; // You might want to add specific icons
                        break;
                    default:
                        iconRes = R.drawable.cloudy_sunny;
                        break;
                }
            }

            ivWeatherIcon.setImageResource(iconRes);
        }
    }
}