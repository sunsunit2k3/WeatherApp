package com.example.weatherapp.activities;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.Toast;

import com.example.weatherapp.R;
import com.example.weatherapp.databinding.ActivityMapBinding;
import com.example.weatherapp.response.WeatherHistory;
import com.example.weatherapp.response.WeatherService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ActivityMapBinding binding;
    private OkHttpClient client;
    private Handler mainHandler;
    private WeatherService weatherService;
    private WeatherHistory weatherHistory;
    private List<WeightedLatLng> heatMapData;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Polygon weatherPolygon;
    private boolean isWeatherZoneVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        client = new OkHttpClient();
        mainHandler = new Handler(Looper.getMainLooper());
        weatherService = new WeatherService(getString(R.string.open_weather_map_api_key));
        weatherHistory = new WeatherHistory(this);
        heatMapData = new ArrayList<>();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Nút lịch sử
        Button historyButton = findViewById(R.id.history_button);
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Nút xóa lịch sử
        Button clearHistoryButton = findViewById(R.id.clear_history_button);
        clearHistoryButton.setOnClickListener(v -> {
            weatherHistory.clearHistory();
            Toast.makeText(MapActivity.this, "History cleared", Toast.LENGTH_SHORT).show();
        });

        // Nút định vị vị trí hiện tại
        Button locateButton = findViewById(R.id.locate_button);
        locateButton.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "Location permission is needed to show your current location", Toast.LENGTH_LONG).show();
                }
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                                fetchWeatherForLocation(currentLatLng, "Current Location");
                            } else {
                                Toast.makeText(MapActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });
        // Nút hiển thị vùng thời tiết
        Button weatherZoneButton = findViewById(R.id.weather_zone_button);
        weatherZoneButton.setOnClickListener(v -> {
            if (!isWeatherZoneVisible) {
                addWeatherZone();
                weatherZoneButton.setText("Hide Zone");
                isWeatherZoneVisible = true;
            } else {
                if (weatherPolygon != null) {
                    weatherPolygon.remove();
                }
                weatherZoneButton.setText("Weather Zone");
                isWeatherZoneVisible = false;
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        LatLng hanoi = new LatLng(21.0285, 105.8542);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanoi, 10));

        mMap.setOnMapClickListener(latLng -> fetchWeatherForLocation(latLng, "Unknown"));

        fetchDefaultCitiesWeather();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    Button locateButton = findViewById(R.id.locate_button);
                    locateButton.performClick();
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchDefaultCitiesWeather() {
        String[] cities = {
                "Hanoi,21.0285,105.8542",
                "Ho Chi Minh City,10.7769,106.7009",
                "Da Nang,16.0544,108.2022"
        };

        for (String city : cities) {
            String[] parts = city.split(",");
            String cityName = parts[0];
            double lat = Double.parseDouble(parts[1]);
            double lon = Double.parseDouble(parts[2]);
            LatLng location = new LatLng(lat, lon);
            fetchWeatherForLocation(location, cityName);
        }
    }

    private void fetchWeatherForLocation(LatLng latLng, String locationName) {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
                latLng.latitude, latLng.longitude, getString(R.string.open_weather_map_api_key));

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> {
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(locationName)
                            .snippet("Error: " + e.getMessage()));
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        WeatherService.WeatherResponse weather = weatherService.getWeather(
                                latLng.latitude, latLng.longitude);
                        double temp = weather.main.temp;
                        String description = weather.weather[0].description;
                        int humidity = weather.main.humidity;
                        String name = locationName.equals("Unknown") ? weather.name : locationName;

                        heatMapData.add(new WeightedLatLng(latLng, temp));

                        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                .format(new Date());
                        WeatherHistory.WeatherEntry entry = new WeatherHistory.WeatherEntry(
                                name, temp, description, timestamp);
                        weatherHistory.addEntry(entry);

                        mainHandler.post(() -> {
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(name)
                                    .snippet(String.format("Temp: %.1f°C, %s, Humidity: %d%%",
                                            temp, description, humidity)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                            addHeatmap(heatMapData);
                            if (isWeatherZoneVisible) addWeatherZone();
                        });
                    } catch (Exception e) {
                        mainHandler.post(() -> {
                            mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(locationName)
                                    .snippet("Error parsing data: " + e.getMessage()));
                        });
                    }
                }
            }
        });
    }

    private void addHeatmap(List<WeightedLatLng> data) {
        if (data.isEmpty()) return;
        HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                .weightedData(data)
                .radius(50)
                .opacity(0.6)
                .build();

        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
    }

    private void addWeatherZone() {
        // Vùng thời tiết giả định (vùng mưa quanh Hà Nội)
        List<LatLng> outerRing = Arrays.asList(
                new LatLng(21.5, 105.5), // Tây Bắc
                new LatLng(21.5, 106.2), // Đông Bắc
                new LatLng(20.8, 106.2), // Đông Nam
                new LatLng(20.8, 105.5)  // Tây Nam
        );

        // Lỗ 1: Khu vực không mưa (gần trung tâm Hà Nội)
        List<LatLng> hole1 = Arrays.asList(
                new LatLng(21.1, 105.8),
                new LatLng(21.1, 105.9),
                new LatLng(21.0, 105.9),
                new LatLng(21.0, 105.8)
        );

        // Lỗ 2: Khu vực không mưa (phía Tây Hà Nội)
        List<LatLng> hole2 = Arrays.asList(
                new LatLng(21.2, 105.6),
                new LatLng(21.2, 105.7),
                new LatLng(21.1, 105.7),
                new LatLng(21.1, 105.6)
        );

        weatherPolygon = mMap.addPolygon(new PolygonOptions()
                .addAll(outerRing)
                .addHole(hole1)
                .addHole(hole2)
                .fillColor(Color.argb(100, 0, 0, 255)) // Màu xanh lam, trong suốt
                .strokeColor(Color.BLUE)
                .strokeWidth(5));
    }
}