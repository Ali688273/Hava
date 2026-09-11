package com.iran.weatherapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.iran.weatherapp.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // پیش‌فرض روی تهران
        fetchLocationAndWeather("تهران")

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearchCity.text.toString().trim()
            if (query.isNotEmpty()) {
                fetchLocationAndWeather(query)
            } else {
                Toast.makeText(this, "لطفاً نام شهر را وارد کنید", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchLocationAndWeather(cityName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val encodedCity = URLEncoder.encode(cityName, "UTF-8")
                val geoUrl = "https://nominatim.openstreetmap.org/search?q=$encodedCity&format=json&limit=1"
                val geoConn = URL(geoUrl).openConnection() as HttpURLConnection
                geoConn.setRequestProperty("User-Agent", "HavaWeatherApp/1.0")
                
                val geoResponse = geoConn.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(geoResponse)
                
                if (jsonArray.length() > 0) {
                    val place = jsonArray.getJSONObject(0)
                    val lat = place.getDouble("lat")
                    val lon = place.getDouble("lon")
                    val shortName = place.getString("display_name").split(",")[0]

                    fetchWeatherData(lat, lon, shortName)
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "شهر مورد نظر پیدا نشد", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "خطا در اتصال برای یافتن شهر", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchWeatherData(lat: Double, lon: Double, cityName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=$lat&lon=$lon"
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "HavaWeatherApp/1.0 contact@iranweather.ir")
                
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val timeseries = json.getJSONObject("properties").getJSONArray("timeseries")
                
                // ساعت اول (اکنون)
                val firstHour = timeseries.getJSONObject(0)
                val instantDetails = firstHour.getJSONObject("data").getJSONObject("instant").getJSONObject("details")
                val temp = instantDetails.getDouble("air_temperature")
                val humidity = instantDetails.getDouble("relative_humidity")
                val windSpeed = instantDetails.getDouble("wind_speed")
                val pressure = instantDetails.optDouble("air_pressure_at_sea_level", 1012.0)
                
                val next1Hours = firstHour.getJSONObject("data").optJSONObject("next_1_hours")
                val precipitation = next1Hours?.optJSONObject("details")?.optDouble("precipitation_amount", 0.0) ?: 0.0

                // اطلاعات ساعات آینده برای اسکرول افقی
                val hourDataList = mutableListOf<Pair<String, Int>>()
                for (i in 1..3) {
                    if (i < timeseries.length()) {
                        val item = timeseries.getJSONObject(i)
                        val timeStr = item.getString("time")
                        val tDetails = item.getJSONObject("data").getJSONObject("instant").getJSONObject("details")
                        val tTemp = tDetails.getDouble("air_temperature").toInt()
                        
                        val shortTime = try {
                            val parsedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(timeStr)
                            SimpleDateFormat("HH:mm", Locale.US).format(parsedDate!!)
                        } catch (e: Exception) {
                            "+$i ساعت"
                        }
                        hourDataList.add(Pair(shortTime, tTemp))
                    }
                }

                withContext(Dispatchers.Main) {
                    binding.tvCityName.text = cityName
                    binding.tvTemp.text = "${temp.toInt()}°C"
                    binding.tvHumidity.text = "رطوبت: ${humidity.toInt()}%"
                    binding.tvWindSpeed.text = "باد: ${windSpeed.toInt()} km/h"
                    binding.tvPressure.text = "فشار: ${pressure.toInt()} hPa"
                    binding.tvRainfall.text = "میزان بارندگی: $precipitation میلی‌متر"
                    binding.tvCondition.text = if (precipitation > 0.0) "بارانی" else "آسمان صاف و پایدار"

                    // به‌روزرسانی کارت‌های ساعتی افقی و زمان‌ها
                    if (hourDataList.size >= 3) {
                        binding.tvHourlyTemp1.text = "${temp.toInt()}°"
                        binding.tvHourlyTime1.text = "اکنون"

                        binding.tvHourlyTemp2.text = "${hourDataList[0].second}°"
                        binding.tvHourlyTime2.text = hourDataList[0].first

                        binding.tvHourlyTemp3.text = "${hourDataList[1].second}°"
                        binding.tvHourlyTime3.text = hourDataList[1].first

                        binding.tvHourlyTemp4.text = "${hourDataList[2].second}°"
                        binding.tvHourlyTime4.text = hourDataList[2].first
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "خطا در دریافت اطلاعات هواشناسی", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
