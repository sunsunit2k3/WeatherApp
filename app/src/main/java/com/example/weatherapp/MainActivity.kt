package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val apiKey = "0cc1b5e403e162a6abb1cd61236e59ce"
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thiết lập RecyclerView
        binding.forecastRecyclerView.layoutManager = LinearLayoutManager(this)

        // Thiết lập Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherApiService = retrofit.create(WeatherApiService::class.java)

        // Xử lý khi nhấn nút lấy dự báo
        binding.fetchButton.setOnClickListener {
            val city = binding.cityEditText.text.toString().trim()
            if (city.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên thành phố", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Bước 1: Lấy tọa độ từ tên thành phố
            weatherApiService.getGeocoding(city, limit = 1, apiKey = apiKey)
                .enqueue(object : Callback<List<GeocodingResponse>> {
                    override fun onResponse(call: Call<List<GeocodingResponse>>, response: Response<List<GeocodingResponse>>) {
                        if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                            val geoData = response.body()!![0]
                            Log.d(TAG, "Lấy tọa độ thành công: lat=${geoData.lat}, lon=${geoData.lon}, city=${geoData.name}")
                            // Bước 2: Lấy dự báo 5 ngày
                            weatherApiService.getForecast(geoData.lat, geoData.lon, apiKey = apiKey)
                                .enqueue(object : Callback<ForecastResponse> {
                                    override fun onResponse(call: Call<ForecastResponse>, response: Response<ForecastResponse>) {
                                        if (response.isSuccessful) {
                                            val forecastData = response.body()?.list ?: emptyList()
                                            // Tổng hợp dữ liệu 3 giờ thành dự báo hàng ngày
                                            val dailyForecasts = aggregateDailyForecasts(forecastData)
                                            Log.d(TAG, "Lấy dự báo thành công: ${dailyForecasts.size} ngày")
                                            binding.forecastRecyclerView.adapter = ForecastAdapter(dailyForecasts)
                                        } else {
                                            val errorBody = response.errorBody()?.string() ?: "Lỗi không xác định"
                                            Log.e(TAG, "Lỗi dự báo: HTTP ${response.code()}, $errorBody")
                                            Toast.makeText(this@MainActivity, "Lỗi lấy dự báo: ${response.code()}", Toast.LENGTH_LONG).show()
                                        }
                                    }

                                    override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                                        Log.e(TAG, "Lỗi mạng dự báo: ${t.message}", t)
                                        Toast.makeText(this@MainActivity, "Lỗi mạng: ${t.message}", Toast.LENGTH_LONG).show()
                                    }
                                })
                        } else {
                            Log.e(TAG, "Lấy tọa độ thất bại: HTTP ${response.code()}")
                            Toast.makeText(this@MainActivity, "Không tìm thấy thành phố", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<List<GeocodingResponse>>, t: Throwable) {
                        Log.e(TAG, "Lỗi mạng tọa độ: ${t.message}", t)
                        Toast.makeText(this@MainActivity, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun aggregateDailyForecasts(forecastData: List<ForecastItem>): List<ForecastItem> {
        val dailyForecasts = mutableListOf<ForecastItem>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val groupedByDay = forecastData.groupBy {
            dateFormat.format(Date(it.dt * 1000))
        }

        groupedByDay.forEach { (_, forecasts) ->
            // Chọn dự báo gần trưa (12:00) nhất cho mỗi ngày
            val middayForecast = forecasts.minByOrNull { forecast ->
                val forecastTime = Date(forecast.dt * 1000)
                val hour = forecastTime.hours
                Math.abs(hour - 12) // Ưu tiên dự báo gần 12:00
            }
            middayForecast?.let { dailyForecasts.add(it) }
        }

        // Giới hạn ở 5 ngày (API cung cấp 5 ngày dữ liệu)
        return dailyForecasts.take(5)
    }
}