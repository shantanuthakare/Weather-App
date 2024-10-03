package com.example.myapplicationwa;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class NotificationEdit extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_edit);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);

        // Find the RadioGroup
        RadioGroup radioGroupInterval = findViewById(R.id.radioGroupInterval);

        // Find the Button
        Button buttonSetInterval = findViewById(R.id.buttonSetInterval);

        // Set a listener for the Button
        buttonSetInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the ID of the checked RadioButton
                int selectedId = radioGroupInterval.getCheckedRadioButtonId();

                // Find the RadioButton by its ID
                RadioButton selectedRadioButton = findViewById(selectedId);

                // Get the text of the selected RadioButton
                String selectedInterval = selectedRadioButton.getText().toString();

                // Save the selected interval in SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("weatherUpdateInterval", selectedInterval);
                editor.apply();

                // Display a toast message with the selected interval
                Toast.makeText(NotificationEdit.this, "Selected Interval: " + selectedInterval, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
