package com.example.weatherapp.response;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class WeatherHistory {
    private static final String PREF_NAME = "WeatherHistory";
    protected static final String KEY_HISTORY = "history"; // Đổi thành protected để tránh lỗi truy cập
    private final SharedPreferences prefs;
    private final Gson gson;

    public static class WeatherEntry {
        public String location;
        public double temp;
        public String description;
        public String timestamp;

        public WeatherEntry(String location, double temp, String description, String timestamp) {
            this.location = location;
            this.temp = temp;
            this.description = description;
            this.timestamp = timestamp;
        }
    }

    public WeatherHistory(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void addEntry(WeatherEntry entry) {
        List<WeatherEntry> history = getHistory();
        history.add(entry);
        String json = gson.toJson(history);
        prefs.edit().putString(KEY_HISTORY, json).apply();
    }

    public List<WeatherEntry> getHistory() {
        String json = prefs.getString(KEY_HISTORY, "[]");
        WeatherEntry[] entries = gson.fromJson(json, WeatherEntry[].class);
        List<WeatherEntry> history = new ArrayList<>();
        if (entries != null) {
            for (WeatherEntry entry : entries) {
                history.add(entry);
            }
        }
        return history;
    }

    public void clearHistory() {
        prefs.edit().putString(KEY_HISTORY, "[]").apply();
    }
}