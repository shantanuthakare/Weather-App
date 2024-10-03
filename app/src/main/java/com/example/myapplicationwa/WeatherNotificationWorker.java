package com.example.myapplicationwa;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
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

import javax.xml.transform.Result;

public class WeatherNotificationWorker extends Worker {

    private static final String API = "06c921750b9a82d8f5d1294e1586276f"; // Use your API key

    public WeatherNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE);
        String userCity = sharedPreferences.getString("userCity", "Paris");

        try {
            String response = getWeatherData(userCity);
            showNotification(userCity, response);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return ListenableWorker.Result.failure();
        }

        return Result.success();
    }

    private String getWeatherData(String city) throws IOException {
        String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=" + API;
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        reader.close();
        inputStream.close();
        connection.disconnect();

        return result.toString();
    }

    private void showNotification(String city, String weatherData) throws JSONException {
        JSONObject jsonObj = new JSONObject(weatherData);
        JSONObject main = jsonObj.getJSONObject("main");
        String temp = main.getString("temp") + "°C";
        String weatherDescription = jsonObj.getJSONArray("weather").getJSONObject(0).getString("description");

        String notificationContent = city + ": " + weatherDescription + ", " + temp;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "weather_channel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Daily Weather")
                .setContentText(notificationContent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    public static void enqueueWork(Context context) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(WeatherNotificationWorker.class).build();
        WorkManager.getInstance(context).enqueue(workRequest);
    }
}
