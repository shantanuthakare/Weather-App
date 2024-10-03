package com.example.myapplicationwa;

import android.Manifest;
import android.app.AlertDialog;

import androidx.appcompat.widget.SearchView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String API = "06c921750b9a82d8f5d1294e1586276f"; // Use your API key
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2;

    private CityEntity currentLocationWeather;

    private  boolean flag;

    Switch switcher;
    ImageView menuIcon;
    ImageView refreshWeather;
    boolean nightMODE;
    private Switch notificationSwitch;
    private NotificationManager notificationManager;
    private List<String> cityList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FusedLocationProviderClient fusedLocationClient;

    private AlertDialog enableLocationDialog; // Dialog instance
    private boolean dialogShown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if (intent != null) {
            String cityName = intent.getStringExtra("cityName");
            String cityTemp = intent.getStringExtra("cityTemp");

            if (cityName != null && cityTemp != null) {
                // Update sunrise, sunset, etc. using the received city name and temperature
                // Example: updateSunriseSunset(cityName);
                // Example: updateWeatherDetails(cityName, cityTemp);
            }
        }

        sharedPreferences = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE);

        schedulePeriodicWeatherUpdate(this);
    }

    private void schedulePeriodicWeatherUpdate(Context context) {
        // Retrieve the selected interval from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        String selectedInterval = sharedPreferences.getString("weatherUpdateInterval", "3 Hour");

        // Convert the selected interval to hours
        int intervalInHours = 1; // Default interval is 1 hour
        if (selectedInterval.equals("3 Hours")) {
            intervalInHours = 3;
        } else if (selectedInterval.equals("6 Hours")) {
            intervalInHours = 6;
        } else if (selectedInterval.equals("12 Hours")) {
            intervalInHours = 12;
        } else if (selectedInterval.equals("24 Hours")) {
            intervalInHours = 24;
        }

        // Log the selected interval
        Log.d("WeatherUpdate", "Scheduled periodic weather update every " + intervalInHours + " hours");

        // Create a periodic work request to update weather with the selected interval
        PeriodicWorkRequest weatherUpdateRequest = new PeriodicWorkRequest.Builder(
                WeatherUpdateWorker.class, // Worker class
                intervalInHours, // Repeat interval (hours)
                TimeUnit.HOURS // TimeUnit
        ).addTag("WeatherUpdateWork")
                .build();

        // Enqueue the periodic work request
        WorkManager.getInstance(context).enqueue(weatherUpdateRequest);






        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
        createNotificationChannel();

        switcher = findViewById(R.id.switcher);
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMODE = sharedPreferences.getBoolean("night", false);
        dialogShown = sharedPreferences.getBoolean("dialogShown", false);

        if (nightMODE) {
            switcher.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE); // Declare sharedPreferences as final

                if (nightMODE) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    SharedPreferences.Editor editor = sharedPreferences.edit(); // Use a local variable for editor
                    editor.putBoolean("night", false);
                    editor.apply();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    SharedPreferences.Editor editor = sharedPreferences.edit(); // Use a local variable for editor
                    editor.putBoolean("night", true);
                    editor.apply();
                }
            }
        });

        ImageView leftIcon = findViewById(R.id.left_icon);
        ImageView rightIcon = findViewById(R.id.right_icon);
//        ImageView locationIcon = findViewById(R.id.location); // Location icon
//        TextView title = findViewById(R.id.toolbar_title);

        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCityListActivity(cityList);
            }
        });

        rightIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);

            }
        });


//        notificationSwitch = findViewById(R.id.notificationSwitch);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (isValidCityName(query)) {
                    // Save the city name to SharedPreferences

                    SharedPreferences sharedPreferences = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userCity", query);
                    editor.apply();

                    // Fetch weather data immediately
                    new WeatherTask().execute(query);

//                notificationManager.areNotificationsEnabled();

                    saveList(query);
                    return false;
                } else {
                    // Show toast for invalid city name
                    Toast.makeText(MainActivity.this, "Invalid city name. Please enter a valid city name.", Toast.LENGTH_SHORT).show();
                }
                return false;

            }
            @Override
            public boolean onQueryTextChange(String neonwText) {
                return false;
            }
        });

        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.cancelAll();

        // Load cities from the database
        loadCityList();

        menuIcon = findViewById(R.id.menuIcon);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fetch the city name and temperature from the UI elements or stored variables
                TextView cityTextView = findViewById(R.id.address);
                TextView tempTextView = findViewById(R.id.temp);

                String cityName = cityTextView.getText().toString();
                String temperature = tempTextView.getText().toString();

                // Create the intent and put the city name and temperature as extras
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, "City: " + cityName + ", Temperature: " + temperature);

                // Start the activity to share the data
                startActivity(Intent.createChooser(sendIntent, "Share to: "));
            }
        });

        refreshWeather = findViewById(R.id.refreshApp123);
        refreshWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLocationPermission()) {
                    getLastLocation();
//                    getCurrentLocation();
                } else {
                    requestLocationPermission();
                }

            }
        });

        ImageView notificationEdit = findViewById(R.id.notificationedit);
        notificationEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the new activity here
                startActivity(new Intent(MainActivity.this, NotificationEdit.class));
            }
        });



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ImageView userLocation = findViewById(R.id.userLocation);
        userLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();

            }
        });
        // Request location permission if not granted


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastKnownLocation();
        }

        // Load the saved location when the app starts
        loadLocation();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showEnableLocationDialog();
        }
    }

    private boolean isValidCityName(String cityName) {
        // Regular expression pattern to match valid city names
        String regex = "^[a-zA-Z\\s-]+$";
        // Check if the cityName matches the pattern
        return cityName.matches(regex);
    }



    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastKnownLocation();
        }
    }


    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            saveLocationAndFetchWeather(latitude, longitude);
                        } else {
                            // If last known location is null, request location updates
                            requestLocationUpdates();
                        }
                    }
                });
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    Location lastLocation = locationResult.getLastLocation();
                    double latitude = lastLocation.getLatitude();
                    double longitude = lastLocation.getLongitude();
                    saveLocationAndFetchWeather(latitude, longitude);
                    // Stop location updates after getting the first result
                    fusedLocationClient.removeLocationUpdates(this);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void saveLocationAndFetchWeather(double latitude, double longitude) {
        loadLocation();
        getAddressFromLocation(latitude, longitude);
    }


    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String cityName = address.getLocality();
                String countryName = address.getCountryName();
                String addressString = cityName + ", " + countryName;
                // Optionally, you can update the UI to display the address
                TextView addressTextView = findViewById(R.id.address);
                addressTextView.setText(addressString);
                // Now, fetch weather data for this location
                fetchWeatherData(addressString);
            } else {
                // Handle case where no address is found
                fetchWeatherData("Paris"); // Use default city if address not found
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle IOException
            fetchWeatherData("Paris"); // Use default city in case of error
        }
    }


    private void loadLocation() {
        SharedPreferences sharedPreferences = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        String latitude = sharedPreferences.getString("latitude", null);
        String longitude = sharedPreferences.getString("longitude", null);

        if (latitude != null && longitude != null) {
            // If saved location exists, show enable location dialog
            fetchWeatherData("Paris");
        } else {
            // If no saved location, use a default city (e.g., Paris)

            //  showEnableLocationDialog();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation();
            }
        } else {
            // Permission denied, show dialog to request permission to enable location services
            showEnableLocationDialog();
        }
    }

    public void showEnableLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enable Location")
                .setMessage("Location permission is required to fetch weather data. Please enable location services.")
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open location settings
                        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Close the app or take appropriate action
                        Toast.makeText(MainActivity.this, "Location permission is required to fetch weather data.", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                })
                .setCancelable(false);

        enableLocationDialog = builder.create();
        enableLocationDialog.show();

        // Save that dialog has been shown
        sharedPreferences.edit().putBoolean("dialogShown", true).apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if the location services are enabled when returning to the activity
        if (enableLocationDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            enableLocationDialog = builder.create();
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && enableLocationDialog != null && enableLocationDialog.isShowing()) {
            enableLocationDialog.dismiss();
        }


    }


    public void refreshSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        String userCity = sharedPreferences.getString("userCity", "Paris");

        // Use a Handler to introduce a delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchWeatherData(userCity);
            }
        }, 5000); // 5000 milliseconds delay (5 seconds)
    }

    // Method to fetch weather data
    private String fetchWeatherData(String city) {
        new WeatherTask().execute(city);
        return city;
    }

    private void saveList(String city) {
        new SaveCityTask().execute(city);
    }

    private void loadCityList() {
        new LoadCityTask().execute();
    }

    private class SaveCityTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... cities) {
            String city = cities[0];
            DatabaseHelper database = DatabaseHelper.getDB(MainActivity.this);
            CityEntity newCityEntity = new CityEntity(city, "default_temp");
            database.cityDao().addCity(newCityEntity);
            return null;
        }
    }

    private class LoadCityTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            DatabaseHelper database = DatabaseHelper.getDB(MainActivity.this);
            List<CityEntity> cityEntities = database.cityDao().getAllCity();
            List<String> cities = new ArrayList<>();
            for (CityEntity cityEntity : cityEntities) {
                cities.add(cityEntity.getCity());
            }
            return cities;
        }

        @Override
        protected void onPostExecute(List<String> cities) {
            super.onPostExecute(cities);
            cityList.clear();
            cityList.addAll(cities);
        }
    }

    private void showCityListActivity(List<String> cities) {
        Intent intent = new Intent(MainActivity.this, CityListActivity.class);
        intent.putStringArrayListExtra("cities", new ArrayList<>(cities));
        startActivity(intent);
    }

    private class WeatherTask extends AsyncTask<String, Void, String> {
        private boolean isNotFoundError = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.mainContainer).setVisibility(View.GONE);
            findViewById(R.id.errorText).setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... cities) {
            String response;
            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + cities[0] + "&units=metric&appid=" + API);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) { // Check if response code is 200 (HTTP_OK)
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    response = stringBuilder.toString();
                } else {
                    // Handle non-OK response codes (e.g., 404)
                    response = null;
                }
                connection.disconnect();
            } catch (IOException e) {
                response = null;
            }
            return response;
        }

        @Override
        public void onPostExecute(String result) {
            findViewById(R.id.loader).setVisibility(View.GONE);
            findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);

            if (TextUtils.isEmpty(result))  {
                if (!flag)
                    Toast.makeText(MainActivity.this, "City not found. Please check the spelling and try again.", Toast.LENGTH_SHORT).show();
                // Show a toast message for wrong city name
                flag = false;
                return;
            }


            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject main = jsonObject.getJSONObject("main");
                JSONObject sys = jsonObject.getJSONObject("sys");
                JSONObject wind = jsonObject.getJSONObject("wind");
                JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);

                Long updatedAt = jsonObject.getLong("dt");
                String updatedAtText = "Updated at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                String temp = main.getString("temp") + "°C";
                String tempMin = "Min Temp: " + main.getString("temp_min") + "°C";
                String tempMax = "Max Temp: " + main.getString("temp_max") + "°C";
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");

                Long sunrise = sys.getLong("sunrise");
                Long sunset = sys.getLong("sunset");
                String windSpeed = wind.getString("speed");
                String weatherDescription = weather.getString("description");

                String address = jsonObject.getString("name") + ", " + sys.getString("country");
                String notificationContent = "Today's weather in " + address + " is " + temp + ".";

                // Continued from previous message...
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "weather_channel_id")
                        .setSmallIcon(R.drawable.weather_icon)
                        .setContentTitle("Weather Update")
                        .setContentText(notificationContent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                // Set the content intent
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                builder.setContentIntent(pendingIntent);

                // Notify the notification manager
                int notificationId = 0;
                notificationManager.notify(notificationId, builder.build());

                ((TextView) findViewById(R.id.address)).setText(address);
                ((TextView) findViewById(R.id.updated_at)).setText(updatedAtText);
                ((TextView) findViewById(R.id.status)).setText(weatherDescription.toUpperCase());
                ((TextView) findViewById(R.id.temp)).setText(temp);
                ((TextView) findViewById(R.id.temp_min)).setText(tempMin);
                ((TextView) findViewById(R.id.temp_max)).setText(tempMax);
                ((TextView) findViewById(R.id.sunrise)).setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunrise * 1000)));
                ((TextView) findViewById(R.id.sunset)).setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunset * 1000)));
                ((TextView) findViewById(R.id.wind)).setText(String.valueOf(windSpeed) + " km/h");
                ((TextView) findViewById(R.id.pressure)).setText(String.valueOf(pressure) + " hPa");
                ((TextView) findViewById(R.id.humidity)).setText(humidity);
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);

                // Save weather data to the database
                saveWeatherDataToDatabase(address, temp);
                showNotification(address, temp);

//                CityEntity currentLocationWeather = new CityEntity(address, temp);
//                Intent i = new Intent(MainActivity.this, CityListActivity.class);
//                i.putExtra("currentLocationWeather", currentLocationWeather);
//                startActivity(i);
//                finish();


                // Assuming address and temp are your data
                CityEntity currentLocationWeather = new CityEntity(address, temp);
                saveCurrentLocationWeather(currentLocationWeather);


            } catch (JSONException e) {
                showError("Failed to parse weather data. Please try again.");
            }
        }

        private void showNotification(String cityName, String temperature) {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "weather_channel")
                    .setSmallIcon(R.drawable.weather_icon)
                    .setContentTitle("Weather Update")
                    .setContentText("City: " + cityName + ", Temperature: " + temperature)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            notificationManager.notify(1, builder.build());
        }


        private void showError(String message) {
            findViewById(R.id.loader).setVisibility(View.GONE);
            findViewById(R.id.errorText).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.errorText)).setText(message);
        }

        private void saveWeatherDataToDatabase(String city, String temp) {
            new SaveWeatherDataTask().execute(new CityEntity(city, temp));
        }
    }

    private void saveCurrentLocationWeather(CityEntity currentLocationWeather) {
        SharedPreferences sharedPreferences = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("currentLocationAddress", currentLocationWeather.getCity());
        editor.putString("currentLocationTemp", currentLocationWeather.getTemp());
        editor.apply();
    }


    private class SaveWeatherDataTask extends AsyncTask<CityEntity, Void, Void> {
        @Override
        protected Void doInBackground(CityEntity... cityEntities) {
            CityEntity cityEntity = cityEntities[0];
            DatabaseHelper database = DatabaseHelper.getDB(MainActivity.this);
            database.cityDao().addCity(cityEntity);
            return null;
        }
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

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    flag = true;
                    fetchWeatherDataByLocation(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void fetchWeatherDataByLocation(double latitude, double longitude) {
        new WeatherTask().execute(String.valueOf(latitude), String.valueOf(longitude));
    }
}


