package com.example.myapplicationwa;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SearchCityActivity extends AppCompatActivity implements CityListAdapter.OnCityClickListener{

    private SearchView searchView;
    private RecyclerView recyclerView;
    private CityListAdapter adapter;
    ImageView leftIcon;
    private boolean dialogShown;
    Switch switcher;

    SharedPreferences sharedPreferences;
    ImageView rightIcon;


    private List<CityEntity> cityList;
    private static final String API = "06c921750b9a82d8f5d1294e1586276f"; //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city);

        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);

        cityList = new ArrayList<>();
        adapter = new CityListAdapter(cityList, null);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnCityClickListener((CityListAdapter.OnCityClickListener) this);
        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new FetchCityDataTask().execute(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Optional: Implement real-time search
                return false;
            }
        });


    }



    @Override
    public void onCityClicked(String cityName) {
        // Handle city click here

        CityEntity clickedCity = getCityByName(cityName);
        if (clickedCity != null) {
            // Pass data to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("cityName", clickedCity.getCity());
            intent.putExtra("cityTemp", clickedCity.getTemp());
            startActivity(intent);
        }
    }

    private CityEntity getCityByName(String cityName){
        for (CityEntity city : cityList) {
            if (city.getCity().equals(cityName)) {
                return city;
            }
        }
        return null;
    }






    private class FetchCityDataTask extends AsyncTask<String, Void, List<CityEntity>> {

        @Override
        protected List<CityEntity> doInBackground(String... params) {
            String query = params[0];
            List<CityEntity> cityEntities = new ArrayList<>();

            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + query + "&units=metric&appid=" + API);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());

                    // Extract city name and temperature directly from the JSON object
                    String cityName = jsonResponse.getString("name");
                    JSONObject mainObject = jsonResponse.getJSONObject("main");
                    String cityTemp = mainObject.getString("temp") + "°C";

                    // Add the retrieved city to the list of city entities
                    cityEntities.add(new CityEntity(cityName, cityTemp));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return cityEntities;
        }

        @Override
        protected void onPostExecute(List<CityEntity> cityEntities) {
            if (cityEntities != null) {
                cityList.clear();
                cityList.addAll(cityEntities);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(0);
            } else {
                Toast.makeText(SearchCityActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
