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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDayLabel, tvTemperature;
        ImageView ivWeatherIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDayLabel = itemView.findViewById(R.id.tvDayLabel);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
        }

        public void bind(WeatherResponse weather, int position) {
            // Format date from timestamp
            String dateStr = formatDate(weather.dt * 1000); // dt is in seconds
            tvDate.setText(dateStr);

            // Set day label
            String dayLabel = getDayLabel(position);
            tvDayLabel.setText(dayLabel);

            // Set temperature (using feels_like as max for demo)
            if (weather.main != null) {
                int temp = (int) Math.round(weather.main.temp);
                int feelsLike = (int) Math.round(weather.main.feels_like);
                tvTemperature.setText(temp + "° " + feelsLike + "°");
            }

            // Set weather icon
            if (weather.weather != null && !weather.weather.isEmpty()) {
                String description = weather.weather.get(0).description;
                setWeatherIcon(description);
            }
        }

        private String formatDate(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }

        private String getDayLabel(int position) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -position); // Go back in time

            Calendar today = Calendar.getInstance();
            long diffDays = (today.getTimeInMillis() - cal.getTimeInMillis()) / (24 * 60 * 60 * 1000);

            if (diffDays == 0) return "Today";
            if (diffDays == 1) return "Yesterday";

            SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
            return sdf.format(cal.getTime());
        }

        private void setWeatherIcon(String description) {
            int iconRes = R.drawable.cloudy_sunny; // default

            if (description != null) {
                String desc = description.toLowerCase();
                if (desc.contains("rain")) {
                    iconRes = R.drawable.umbrella;
                } else if (desc.contains("cloud")) {
                    iconRes = R.drawable.cloudy_sunny;
                }
            }

            ivWeatherIcon.setImageResource(iconRes);
        }
    }
}