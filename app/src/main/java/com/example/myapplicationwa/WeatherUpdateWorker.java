package com.example.myapplicationwa;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherUpdateWorker extends Worker {

    private static final String API = "06c921750b9a82d8f5d1294e1586276f";
    private static final String TAG = "WeatherUpdateWorker";

    public WeatherUpdateWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Result result = fetchWeatherDataAndShowNotification();
        Log.d(TAG, "Work finished");
        return result;
    }

    private Result fetchWeatherDataAndShowNotification() {
        Context context = getApplicationContext();
        if (checkPermissions(context)) {
            Location location = getLastKnownLocation(context);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d(TAG, "Location: " + latitude + ", " + longitude);

                try {
                    String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&units=metric&appid=" + API;
                    URL url = new URL(apiUrl);

                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line).append("\n");
                    }

                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONObject main = jsonObject.getJSONObject("main");
                    JSONObject sys = jsonObject.getJSONObject("sys");
                    String cityName = jsonObject.getString("name");
                    String countryName = sys.getString("country");
                    String temp = main.getString("temp") + "°C";
                    String weatherDescription = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    long updatedAt = jsonObject.getLong("dt");
                    String updatedAtText = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));

                    String notificationContent = "Weather in " + cityName + ", " + countryName + ": " + temp + ", " + weatherDescription + " (Updated at " + updatedAtText + ")";
                    showNotification(notificationContent);

                    inputStream.close();
                    urlConnection.disconnect();
                    return Result.success();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    return Result.failure();
                }
            } else {
                Log.e(TAG, "Location is null");
                return Result.failure();
            }
        } else {
            Log.e(TAG, "Permissions not granted");
            return Result.failure();
        }
    }

    private boolean checkPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private Location getLastKnownLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        return location;
    }

    private void showNotification(String content) {
        Context context = getApplicationContext();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "weather_channel")
                .setSmallIcon(R.drawable.weather_icon)
                .setContentTitle("Weather Update")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Log.d(TAG, "Notification shown");
        notificationManager.notify(1, builder.build());
    }
}
