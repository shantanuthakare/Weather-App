package com.example.myapplicationwa;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherNotificationService extends IntentService {
    private static final String CHANNEL_ID = "weather_channel";

    public WeatherNotificationService() {
        super("WeatherNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        String userCity = sharedPreferences.getString("userCity", "Paris");
        fetchWeatherData(userCity);
    }

    private void fetchWeatherData(String city) {
        String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=06c921750b9a82d8f5d1294e1586276f");
        try {
            JSONObject jsonObj = new JSONObject(response);
            JSONObject main = jsonObj.getJSONObject("main");
            JSONObject sys = jsonObj.getJSONObject("sys");
            JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

            String temp = main.getString("temp") + "°C";
            String weatherDescription = weather.getString("description");
            String address = jsonObj.getString("name") + ", " + sys.getString("country");

            String notificationText = address + ": " + temp + ", " + weatherDescription;

            createNotificationChannel();
            showNotification(notificationText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Weather Channel";
            String description = "Channel for weather notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_weather)
                .setContentTitle("Daily Weather Update")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }
}
