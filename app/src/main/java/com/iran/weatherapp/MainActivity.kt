package com.iran.weatherapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.iran.weatherapp.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val cityCoordinates = mapOf(
        "تهران" to Pair(35.6892, 51.3890),
        "خرم‌آباد" to Pair(33.4878, 48.3558),
        "مشهد" to Pair(36.2972, 59.6067),
        "اصفهان" to Pair(32.6546, 51.6680),
        "شیراز" to Pair(29.5918, 52.5836),
        "تبریز" to Pair(38.0800, 46.2919)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchWeatherData(33.4878, 48.3558, "خرم آباد")

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearchCity.text.toString().trim()
            val coords = cityCoordinates[query]
            if (coords != null) {
                fetchWeatherData(coords.first, coords.second, query)
            } else {
                Toast.makeText(this, "شهر مورد نظر یافت نشد", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchWeatherData(lat: Double, lon: Double, cityName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=$lat&lon=$lon"
                val connection = URL(url).openConnection() as java.net.HttpURLConnection
                connection.setRequestProperty("User-Agent", "HavaWeatherApp/1.0 contact@iranweather.ir")
                
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val timeseries = json.getJSONObject("properties").getJSONArray("timeseries")
                
                val firstHour = timeseries.getJSONObject(0)
                val instantDetails = firstHour.getJSONObject("data").getJSONObject("instant").getJSONObject("details")
                val temp = instantDetails.getDouble("air_temperature")
                val humidity = instantDetails.getDouble("relative_humidity")
                val windSpeed = instantDetails.getDouble("wind_speed")
                
                val next1Hours = firstHour.getJSONObject("data").optJSONObject("next_1_hours")
                val precipitation = next1Hours?.optJSONObject("details")?.optDouble("precipitation_amount", 0.0) ?: 0.0

                withContext(Dispatchers.Main) {
                    binding.tvCityName.text = cityName
                    binding.tvTemp.text = "${temp.toInt()}°C"
                    binding.tvHumidity.text = "رطوبت هوا: ${humidity.toInt()}%"
                    binding.tvWindSpeed.text = "سرعت باد: ${windSpeed.toInt()} کیلومتر بر ساعت"
                    binding.tvRainfall.text = "میزان بارندگی: $precipitation میلی‌متر"
                    binding.tvDataSource.text = "پیش‌بینی از MET Norway (جهانی)"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "خطا در ارتباط با سرور جهانی", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
