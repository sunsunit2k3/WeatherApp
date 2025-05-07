package com.example.weatherapp

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface WeatherApiService {
    @GET("geo/1.0/direct")
    fun getGeocoding(
        @Query("q") city: String,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): Call<List<GeocodingResponse>>

    @GET("data/2.5/forecast")
    fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): Call<ForecastResponse>
}