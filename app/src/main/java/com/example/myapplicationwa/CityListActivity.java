package com.example.myapplicationwa;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CityListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CityListAdapter adapter;

    private CityEntity currentLocationWeather;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get current location weather data from intent
        Intent intent = getIntent();
        currentLocationWeather = (CityEntity) intent.getSerializableExtra("currentLocationWeather");


        // Load cities from the database
        loadCityList();
    }

    private void loadCityList() {
        new LoadCityTask().execute();
    }

    private class LoadCityTask extends AsyncTask<Void, Void, List<CityEntity>> {
        @Override
        protected List<CityEntity> doInBackground(Void... voids) {
            DatabaseHelper database = DatabaseHelper.getDB(CityListActivity.this);

            return database.cityDao().getAllCity();
        }

        @Override
        public void onPostExecute(List<CityEntity> cityEntities) {
            super.onPostExecute(cityEntities);

            if (currentLocationWeather != null) {
                cityEntities.add(0, currentLocationWeather);
            }
            CityEntity getCurrentLocationWeather = getCurrentLocationWeather();
            adapter = new CityListAdapter(cityEntities,currentLocationWeather);
            recyclerView.setAdapter(adapter);
            adapter.setRecyclerView(recyclerView);
            adapter.moveToFirstPosition(cityEntities.toString());
            adapter.setFirstItemEnhancement(recyclerView);
        }
    }

//    private String getLastSearchedCity() {
//        SharedPreferences sharedPreferences = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE);
//        String userCity = sharedPreferences.getString("userCity", "Mumbai");
//        Log.d("CityListActivity", "Last searched city: " + userCity);
//        return userCity;
//    }

    private CityEntity getCurrentLocationWeather() {
        SharedPreferences sharedPreferences = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        String address = sharedPreferences.getString("currentLocationAddress", "");
        String temp = sharedPreferences.getString("currentLocationTemp", "");
        return new CityEntity(address, temp);
    }
}
