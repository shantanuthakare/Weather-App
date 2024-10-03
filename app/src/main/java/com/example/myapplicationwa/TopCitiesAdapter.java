package com.example.myapplicationwa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TopCitiesAdapter extends RecyclerView.Adapter<TopCitiesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> weatherDataList;

    public TopCitiesAdapter(Context context, ArrayList<String> weatherDataList) {
        this.context = context;
        this.weatherDataList = weatherDataList;
    }

    public void setData(ArrayList<String> weatherDataList) {
        this.weatherDataList = weatherDataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_city_weather, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String weatherData = weatherDataList.get(position);
        holder.bind(weatherData);
    }

    @Override
    public int getItemCount() {
        return weatherDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cityNameTextView;
        TextView cityWeatherTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cityNameTextView = itemView.findViewById(R.id.cityNameTextView);
            cityWeatherTextView = itemView.findViewById(R.id.weatherInfoTextView);
        }

        public void bind(String weatherData) {
            // Split the weather data string to get city name and temperature
            String[] parts = weatherData.split(": ");
            if (parts.length == 2) {
                String cityName = parts[0];
                String weatherInfo = parts[1];
                cityNameTextView.setText(cityName);
                cityWeatherTextView.setText(weatherInfo);
            }
        }
    }
}
