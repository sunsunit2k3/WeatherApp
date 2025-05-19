package com.example.weatherapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.adapters.HourlyAdapter;
import com.example.weatherapp.entities.Hourly;
import com.example.weatherapp.interfaces.WeatherService;
import com.example.weatherapp.response.CurrentWeatherResponse;
import com.example.weatherapp.response.ForecastResponse;
import com.example.weatherapp.retrofit.RetrofitClient;
import com.example.weatherapp.update.UpdateUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    public static final String API_KEY = "e5afb6abedc33f32a139cf17a8921af6";
    private static final String UNITS = "metric";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private ArrayList<Hourly> items;
    private HourlyAdapter hourlyAdapter;
    private RecyclerView recyclerViewHourly;
    private TextView textNameCity, textNext5Days, textDateTime, textState, textTemperature;
    private TextView textPercentHumidity, textWindSpeed, textFeelsLike;
    private ImageView imgIconWeather, imgSearch, imgLocation, imgMap;
    private EditText editTextSearch;
    private String nameCity = "";
    private String icon;
    private long pressBackTime;
    private WeatherService weatherService;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        weatherService = RetrofitClient.getInstance().create(WeatherService.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setMapping();
        recyclerViewHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        items = new ArrayList<>();
        hourlyAdapter = new HourlyAdapter(items);
        recyclerViewHourly.setAdapter(hourlyAdapter);

        textNext5Days.setOnClickListener(v -> setIntentExtras());
        textNameCity.setText("");
        textNameCity.setVisibility(View.GONE);

        imgMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            // Optionally pass data, e.g., city name or coordinates
            intent.putExtra("name", nameCity);
            startActivity(intent);
        });
        imgSearch.setOnClickListener(v -> {
            String city = editTextSearch.getText().toString();
            if (city.isEmpty()) {
                Toast.makeText(this, "Please enter city", Toast.LENGTH_SHORT).show();
            } else {
                nameCity = city;
                textNameCity.setText(nameCity);
                textNameCity.setVisibility(View.VISIBLE);
                getCurrentWeatherData(city);
                getHourlyData(city);
            }
        });

        imgLocation.setOnClickListener(v -> checkLocationPermissionAndFetch());
    }

    private void setMapping() {
        recyclerViewHourly = findViewById(R.id.recyclerViewHourly);
        textNext5Days = findViewById(R.id.textNext5Days);
        textDateTime = findViewById(R.id.textDateTime);
        editTextSearch = findViewById(R.id.editTextSearch);
        textState = findViewById(R.id.textState);
        textNameCity = findViewById(R.id.textNameCity);
        textTemperature = findViewById(R.id.textTemperature);
        imgIconWeather = findViewById(R.id.imgIconWeather);
        textPercentHumidity = findViewById(R.id.textPercentHumidity);
        textWindSpeed = findViewById(R.id.textWindSpeed);
        textFeelsLike = findViewById(R.id.textFeelsLike);
        imgSearch = findViewById(R.id.imgSearch);
        imgLocation = findViewById(R.id.imgLocation);
        imgMap = findViewById(R.id.imgMap);
    }

    private void setIntentExtras() {
        String city = editTextSearch.getText().toString();
        Intent intent = new Intent(MainActivity.this, FutureActivity.class);
        intent.putExtra("name", city);
        intent.putExtra("state", textState.getText().toString());
        intent.putExtra("temperature", textTemperature.getText().toString());
        intent.putExtra("feelsLike", textFeelsLike.getText().toString());
        intent.putExtra("windSpeed", textWindSpeed.getText().toString());
        intent.putExtra("humidity", textPercentHumidity.getText().toString());
        intent.putExtra("imgIconWeather", icon);
        startActivity(intent);
    }

    private void checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                System.out.println("print location" + latitude);
                System.out.println("print location" + longitude);
                getCurrentWeatherByLatLon(latitude, longitude);
                getHourlyDataByLatLon(latitude, longitude);
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentWeatherByLatLon(double lat, double lon) {
        weatherService.getCurrentWeatherByLatLon(lat, lon, API_KEY, UNITS)
                .enqueue(new Callback<CurrentWeatherResponse>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NonNull Call<CurrentWeatherResponse> call, @NonNull Response<CurrentWeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                CurrentWeatherResponse currentWeatherResponse = response.body();
                                nameCity = currentWeatherResponse.getName();
                                textNameCity.setText(currentWeatherResponse.getName() + "-" + currentWeatherResponse.getSys().getCountry());
                                textNameCity.setVisibility(View.VISIBLE);
                                textState.setText(currentWeatherResponse.getWeather()[0].getMain());
                                double dt = currentWeatherResponse.getDateTime();
                                Date date = new Date((long) dt * 1000L);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE yyyy-MM-dd | HH:mm a", Locale.ENGLISH);
                                textDateTime.setText(dateFormat.format(date));
                                textTemperature.setText((int) currentWeatherResponse.getMain().getTemp() + "°C");
                                textPercentHumidity.setText(currentWeatherResponse.getMain().getHumidity() + "%");
                                textFeelsLike.setText((int) currentWeatherResponse.getMain().getFeelsLike() + "°C");
                                textWindSpeed.setText((int) currentWeatherResponse.getWind().getSpeed() + "m/s");
                                icon = currentWeatherResponse.getWeather()[0].getIcon();
                                imgIconWeather.setImageResource(getResources()
                                        .getIdentifier(String.valueOf(UpdateUI.getIconID(icon)), "drawable", getPackageName()));
                            } catch (Exception e) {
                                Log.e("API", "Error parsing JSON: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CurrentWeatherResponse> call, @NonNull Throwable t) {
                        Log.e("API", "Error: " + t.getMessage());
                        Toast.makeText(MainActivity.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getHourlyDataByLatLon(double lat, double lon) {
        weatherService.getForecastByLatLon(lat, lon, API_KEY, UNITS)
                .enqueue(new Callback<ForecastResponse>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(@NonNull Call<ForecastResponse> call, @NonNull Response<ForecastResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                items.clear();
                                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                long currentTime = System.currentTimeMillis();

                                int count = 0;
                                for (ForecastResponse.HourlyForecast forecast : response.body().getList()) {
                                    Date forecastDate = inputFormat.parse(forecast.getDateTime());
                                    if (forecastDate != null && forecastDate.getTime() >= currentTime) {
                                        String hour = outputFormat.format(forecastDate);
                                        int temp = (int) forecast.getMain().getTemp();
                                        String icon = forecast.getWeather().get(0).getIcon();
                                        items.add(new Hourly(hour, temp, icon));
                                        count++;
                                        if (count >= 5) break; // Limit to 5 forecasts
                                    }
                                }
                                hourlyAdapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                Log.e("API", "Error parsing JSON: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) {
                        Log.e("API", "Error: " + t.getMessage());
                        Toast.makeText(MainActivity.this, "Failed to fetch hourly data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getCurrentWeatherData(String city) {
        weatherService.getCurrentWeather(city, API_KEY, UNITS)
                .enqueue(new Callback<CurrentWeatherResponse>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NonNull Call<CurrentWeatherResponse> call, @NonNull Response<CurrentWeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                CurrentWeatherResponse currentWeatherResponse = response.body();
                                textNameCity.setText(currentWeatherResponse.getName() + "-" + currentWeatherResponse.getSys().getCountry());
                                textState.setText(currentWeatherResponse.getWeather()[0].getMain());
                                double dt = currentWeatherResponse.getDateTime();
                                Date date = new Date((long) dt * 1000L);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE yyyy-MM-dd | HH:mm a", Locale.ENGLISH);
                                textDateTime.setText(dateFormat.format(date));
                                textTemperature.setText((int) currentWeatherResponse.getMain().getTemp() + "°C");
                                textPercentHumidity.setText(currentWeatherResponse.getMain().getHumidity() + "%");
                                textFeelsLike.setText((int) currentWeatherResponse.getMain().getFeelsLike() + "°C");
                                textWindSpeed.setText((int) currentWeatherResponse.getWind().getSpeed() + "m/s");
                                icon = currentWeatherResponse.getWeather()[0].getIcon();
                                imgIconWeather.setImageResource(getResources()
                                        .getIdentifier(String.valueOf(UpdateUI.getIconID(icon)), "drawable", getPackageName()));
                            } catch (Exception e) {
                                Log.e("API", "Error parsing JSON: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CurrentWeatherResponse> call, @NonNull Throwable t) {
                        Log.e("API", "Error: " + t.getMessage());
                        Toast.makeText(MainActivity.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getHourlyData(String city) {
        weatherService.getForecast(city, API_KEY, UNITS)
                .enqueue(new Callback<ForecastResponse>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(@NonNull Call<ForecastResponse> call, @NonNull Response<ForecastResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                items.clear();
                                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                long currentTime = System.currentTimeMillis();

                                int count = 0;
                                for (ForecastResponse.HourlyForecast forecast : response.body().getList()) {
                                    Date forecastDate = inputFormat.parse(forecast.getDateTime());
                                    if (forecastDate != null && forecastDate.getTime() >= currentTime) {
                                        String hour = outputFormat.format(forecastDate);
                                        int temp = (int) forecast.getMain().getTemp();
                                        String icon = forecast.getWeather().get(0).getIcon();
                                        items.add(new Hourly(hour, temp, icon));
                                        count++;
                                        if (count >= 5) break; // Limit to 5 forecasts
                                    }
                                }
                                hourlyAdapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                Log.e("API", "Error parsing JSON: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) {
                        Log.e("API", "Error: " + t.getMessage());
                        Toast.makeText(MainActivity.this, "Failed to fetch hourly data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - pressBackTime < 2000) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressBackTime = System.currentTimeMillis();
    }
}