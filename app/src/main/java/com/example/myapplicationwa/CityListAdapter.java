package com.example.myapplicationwa;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.ViewHolder> {

    private List<CityEntity> cities;
    private CityEntity currentLocationWeather;
    private OnCityClickListener listener;

    RecyclerView recyclerView;

    public interface OnCityClickListener {
        void onCityClicked(String cityName);
    }

    public void setOnCityClickListener(OnCityClickListener listener) {
        this.listener = listener;
    }

    public CityListAdapter(List<CityEntity> cities, CityEntity currentLocationWeather) {
        this.cities = cities;
        this.currentLocationWeather = currentLocationWeather;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View cityView = inflater.inflate(R.layout.item_c, parent, false);
        return new ViewHolder(cityView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CityEntity cityEntity = cities.get(position);

//        holder.itemView.setOnClickListener((View.OnClickListener) this);

        // Check if the current item is the first item and if the current location weather is not null
        if (position == 0 && currentLocationWeather != null) {
            cityEntity = currentLocationWeather; // Set current location weather data to the first item
        }

        holder.cityNameTextView.setText(cityEntity.getCity());
        holder.tempTextView.setText(cityEntity.getTemp());

        // Apply UI enhancements for the first item
        if (position == 0) {
            holder.itemView.setBackgroundColor(Color.BLUE);
            holder.cityNameTextView.setTextColor(Color.WHITE);
            holder.cityNameTextView.setTextSize(18);
            holder.cityNameTextView.setTypeface(null, Typeface.BOLD);
            int padding = 16; // in pixels
            holder.cityNameTextView.setPadding(padding, padding, padding, padding);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
            int margin = 8; // in pixels
            params.leftMargin = margin;
            holder.itemView.setLayoutParams(params);
        }
    }

    public void onClick(View v) {
        if (listener != null) {
            int position = recyclerView.getChildLayoutPosition(v);
            listener.onCityClicked(cities.get(position).getCity());
        }
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    public void setRecyclerView(RecyclerView recyclerView) {
    }

    public void setFirstItemEnhancement(RecyclerView recyclerView) {
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView cityNameTextView;
        TextView tempTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            cityNameTextView = itemView.findViewById(R.id.textViewC2);
            tempTextView = itemView.findViewById(R.id.temp2);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            CityEntity clickedCity = cities.get(position);

            // Handle the click event, for example, by passing the clicked city name to a listener
            if (listener != null) {
                listener.onCityClicked(clickedCity.getCity());
            }
        }
    }
        public void moveToFirstPosition(String searchedCity) {
            if (searchedCity != null && !searchedCity.isEmpty()) {
                for (int i = cities.size() - 1; i >= 0; i--) {
                    CityEntity cityEntity = cities.get(i);
                    if (cityEntity.getCity().equalsIgnoreCase(searchedCity)) {
                        cities.remove(i);
                        cities.add(0, cityEntity);
                        notifyDataSetChanged();  // Notify the adapter about the data change
                        break;
                    }
                }
            }
        }
    }
