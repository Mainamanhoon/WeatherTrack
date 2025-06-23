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

            String dateStr = formatDate(weather.dt * 1000);
            tvDate.setText(dateStr);

             String dayLabel = getDayLabel(position);
            tvDayLabel.setText(dayLabel);

             if (weather.main != null) {
                String tempText;
                if (weather.main.temp_min > 0 && weather.main.temp_max > 0) {
                     int minTemp = (int) Math.round(weather.main.temp_min);
                    int maxTemp = (int) Math.round(weather.main.temp_max);
                    tempText = minTemp + "° " + maxTemp + "°";
                } else {
                     int temp = (int) Math.round(weather.main.temp);
                    int feelsLike = (int) Math.round(weather.main.feels_like);
                    tempText = temp + "° " + feelsLike + "°";
                }
                tvTemperature.setText(tempText);
            }

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

            Calendar today = Calendar.getInstance();
            Calendar itemDate = Calendar.getInstance();
            itemDate.add(Calendar.DAY_OF_YEAR, -position);

             long diffInMillis = today.getTimeInMillis() - itemDate.getTimeInMillis();
            int daysDiff = (int) (diffInMillis / (24 * 60 * 60 * 1000));

            switch (daysDiff) {
                case 0:
                    return "Today";
                case 1:
                    return "Yesterday";
                case 2:
                    return "2 days ago";
                case 3:
                    return "3 days ago";
                case 4:
                    return "4 days ago";
                case 5:
                    return "5 days ago";
                case 6:
                    return "6 days ago";
                default:
                     SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                    return sdf.format(itemDate.getTime());
            }
        }

        private void setWeatherIcon(String weatherMain) {
            int iconRes = R.drawable.cloudy_sunny;

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
                    case "thunderstorm":
                        iconRes = R.drawable.umbrella;
                        break;
                    case "snow":
                        iconRes = R.drawable.cloudy_sunny;
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
                        iconRes = R.drawable.cloudy_sunny;
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