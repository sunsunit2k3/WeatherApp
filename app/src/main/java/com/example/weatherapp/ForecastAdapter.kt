package com.example.weatherapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemForecastBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ForecastAdapter(private val forecastList: List<ForecastItem>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(private val binding: ItemForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: ForecastItem) {
            val date = SimpleDateFormat("EEE", Locale.getDefault()).format(forecast.dt * 1000)
            binding.dayTextView.text = date
            binding.tempTextView.text = "${forecast.main.temp.toInt()}°C"
            binding.descriptionTextView.text = forecast.weather[0].description.capitalize()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(forecastList[position])
    }

    override fun getItemCount(): Int = forecastList.size
}