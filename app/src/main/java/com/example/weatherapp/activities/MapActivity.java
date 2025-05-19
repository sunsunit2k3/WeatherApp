package com.example.weatherapp.activities;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.weatherapp.R;

public class MapActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize back button
        Button btnBack = findViewById(R.id.buttonBack);
        btnBack.setOnClickListener(v -> finish()); // Return to MainActivity
    }
}