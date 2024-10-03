package com.example.myapplicationwa;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    private Switch notificationSwitch;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private static final String API = "06c921750b9a82d8f5d1294e1586276f";

    private static final String[] TOP_CITIES = {
            "Mumbai", "Hyderabad", "Kolkata", "Bengaluru", "Ahmedabad",
            "Chennai", "Amritsar", "Guwahati", "Ludhiana", "Visakhapatnam"
    };
    TextView topCityWeather;
    private RecyclerView topCitiesRecyclerView;
    private TopCitiesAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ImageView refreshIcon = findViewById(R.id.refreshApp1234);
        refreshIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fetch weather data for top cities again
                fetchWeatherForTopCities();

                Toast.makeText(SettingsActivity.this, "Refreshing list...", Toast.LENGTH_SHORT).show();
            }
        });


        topCityWeather = findViewById(R.id.topCityWeather);

        topCitiesRecyclerView = findViewById(R.id.topCitiesRecyclerView);

        adapter = new TopCitiesAdapter(this, new ArrayList<>());
        topCitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        topCitiesRecyclerView.setAdapter(adapter);

        // Fetch weather data for top cities
        fetchWeatherForTopCities();




        // Load the switch state from SharedPreferences
//        SharedPreferences prefs = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
//        boolean isNotificationEnabled = prefs.getBoolean("notifications_enabled", false);
//        notificationSwitch.setChecked(isNotificationEnabled);

        // Initialize AlarmManager
//        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//
//        // Set listener for switch
//        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                // Save the switch state in SharedPreferences
//                SharedPreferences.Editor editor = prefs.edit();
//                editor.putBoolean("notifications_enabled", isChecked);
//                editor.apply();
//
//                if (isChecked) {
//                    scheduleDailyNotification();
//                    Toast.makeText(SettingsActivity.this, "Notifications enabled", Toast.LENGTH_SHORT).show();
//                } else {
//                    cancelScheduledNotifications();
//                    Toast.makeText(SettingsActivity.this, "Notifications disabled", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }

//    private void scheduleDailyNotification() {
//        Intent intent = new Intent(this, WeatherNotifiactionReceiver.class);
//        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        // Set the alarm to start at approximately 8:00 a.m.
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.HOUR_OF_DAY, 8);
//        calendar.set(Calendar.MINUTE, 0);
//
//        // With setInexactRepeating(), you have to use one of the AlarmManager interval constants
//        // for the interval parameter.
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                AlarmManager.INTERVAL_DAY, alarmIntent);
//
//        createNotificationChannel();
//    }

//    private void cancelScheduledNotifications() {
//        if (alarmIntent != null) {
//            alarmManager.cancel(alarmIntent);
//        }
   }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Weather Channel";
            String description = "Channel for weather notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("weather_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back button click here
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchWeatherForTopCities() {
        new FetchTopCitiesWeatherTask().execute(TOP_CITIES);
    }

    private class FetchTopCitiesWeatherTask extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... cities) {
            ArrayList<String> weatherDataList = new ArrayList<>();
            for (String city : cities) {
                String response = fetchWeatherData(city);
                if (response != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject main = jsonObject.getJSONObject("main");
                        String temp = main.getString("temp") + "°C";
                        weatherDataList.add(city + ": " + temp);
                        Log.d("FetchWeatherTask", "Weather data fetched for " + city + ": " + temp);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return weatherDataList;
        }

        @Override
        public void onPostExecute(ArrayList<String> weatherDataList) {
            super.onPostExecute(weatherDataList);
            adapter.setData(weatherDataList);
            Log.d("FetchWeatherTask", "Weather data set to adapter");
        }

        private String fetchWeatherData(String city) {
            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=" + API);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                connection.disconnect();
                return stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }


}
