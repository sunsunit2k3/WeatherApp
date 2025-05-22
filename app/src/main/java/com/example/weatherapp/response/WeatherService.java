package com.example.weatherapp.response;

import com.google.gson.Gson;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherService {
    private final OkHttpClient client;
    private final Gson gson;
    private final String apiKey;

    public WeatherService(String apiKey) {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.apiKey = apiKey;
    }

    public static class WeatherResponse {
        public Main main;
        public Weather[] weather;
        public String name;
    }

    public static class Main {
        public double temp;
        public int humidity;
    }

    public static class Weather {
        public String description;
        public String icon;
    }

    public WeatherResponse getWeather(double lat, double lon) throws IOException {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
                lat, lon, apiKey);
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String json = response.body().string();
            return gson.fromJson(json, WeatherResponse.class);
        }
    }
}