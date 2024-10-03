package com.example.myapplicationwa;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class  WeatherNotifiactionReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "weather_notification_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Create notification channel
        createNotificationChannel(context);

        // Build and display the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
//                .setSmallIcon(R.drawable.ic_notification)  // Change this to your app's notification icon
                .setContentTitle("Daily Weather Update")
                .setContentText("Check today's weather forecast!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Weather Notifications";
            String description = "Channel for daily weather notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
