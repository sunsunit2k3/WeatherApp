package com.example.bandoapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ListView historyListView = findViewById(R.id.history_list);
        WeatherHistory weatherHistory = new WeatherHistory(this);
        List<WeatherHistory.WeatherEntry> history = weatherHistory.getHistory();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1);
        for (WeatherHistory.WeatherEntry entry : history) {
            adapter.add(String.format("%s: %.1f°C, %s (%s)",
                    entry.location, entry.temp, entry.description, entry.timestamp));
        }
        historyListView.setAdapter(adapter);
    }
}