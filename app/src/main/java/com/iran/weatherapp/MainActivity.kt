package com.iran.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fetchLocationAndWeather("تهران")

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearchCity.text.toString().trim()
            if (query.isNotEmpty()) {
                fetchLocationAndWeather(query)
            } else {
                Toast.makeText(this, "لطفاً نام شهر را وارد کنید", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGps.setOnClickListener {
            getCurrentLocationWeather()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    private fun playRainSound(isRainy: Boolean) {
        try {
            if (isRainy) {
                if (mediaPlayer == null) {
                    val resId = resources.getIdentifier("rain_sound", "raw", packageName)
                    if (resId != 0) {
                        mediaPlayer = MediaPlayer.create(this, resId).apply {
                            isLooping = true
                            start()
                        }
                    }
                } else if (!mediaPlayer!!.isPlaying) {
                    mediaPlayer?.start()
                }
            } else {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.pause()
                    }
                }
            }
        } catch (e: Exception) {
            // جلوگیری از خطا در صورت نبود فایل صوتی
        }
    }

    private fun getCurrentLocationWeather() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                fetchWeatherData(location.latitude, location.longitude, "موقعیت فعلی شما")
            } else {
                Toast.makeText(this, "امکان دریافت موقعیت وجود ندارد. GPS را روشن کنید", Toast.LENGTH_SHORT).show()
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
                
                val firstHour = timeseries.getJSONObject(0)
                val instantDetails = firstHour.getJSONObject("data").getJSONObject("instant").getJSONObject("details")
                val temp = instantDetails.getDouble("air_temperature")
                val humidity = instantDetails.getDouble("relative_humidity")
                val windSpeed = instantDetails.getDouble("wind_speed")
                val pressure = instantDetails.optDouble("air_pressure_at_sea_level", 1012.0)
                
                val next1Hours = firstHour.getJSONObject("data").optJSONObject("next_1_hours")
                val precipitation = next1Hours?.optJSONObject("details")?.optDouble("precipitation_amount", 0.0) ?: 0.0

                val hourDataList = mutableListOf<Triple<String, Int, Double>>()
                for (i in 1..3) {
                    if (i < timeseries.length()) {
                        val item = timeseries.getJSONObject(i)
                        val timeStr = item.getString("time")
                        val tDetails = item.getJSONObject("data").getJSONObject("instant").getJSONObject("details")
                        val tTemp = tDetails.getDouble("air_temperature").toInt()
                        
                        val tNext1 = item.getJSONObject("data").optJSONObject("next_1_hours")
                        val tRain = tNext1?.optJSONObject("details")?.optDouble("precipitation_amount", 0.0) ?: 0.0

                        val shortTime = try {
                            val parsedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(timeStr)
                            SimpleDateFormat("HH:mm", Locale.US).format(parsedDate!!)
                        } catch (e: Exception) {
                            "+$i ساعت"
                        }
                        hourDataList.add(Triple(shortTime, tTemp, tRain))
                    }
                }

                val dailyTemps = mutableListOf<Int>()
                for (i in 6..24 step 6) {
                    if (i < timeseries.length()) {
                        val dItem = timeseries.getJSONObject(i)
                        val dTemp = dItem.getJSONObject("data").getJSONObject("instant").getJSONObject("details").getDouble("air_temperature").toInt()
                        dailyTemps.add(dTemp)
                    }
                }

                withContext(Dispatchers.Main) {
                    binding.tvCityName.text = cityName
                    binding.tvTemp.text = "${temp.toInt()}°C"
                    binding.tvHumidity.text = "رطوبت: ${humidity.toInt()}%"
                    binding.tvWindSpeed.text = "باد: ${windSpeed.toInt()} km/h"
                    binding.tvPressure.text = "فشار: ${pressure.toInt()} hPa"
                    binding.tvRainfall.text = "میزان بارندگی: $precipitation میلی‌متر"

                    val isRainy = precipitation > 0.0
                    if (isRainy) {
                        binding.tvCondition.text = "بارانی"
                        binding.ivMainConditionIcon.setImageResource(android.R.drawable.ic_menu_compass)
                    } else {
                        binding.tvCondition.text = "آسمان صاف و پایدار"
                        binding.ivMainConditionIcon.setImageResource(android.R.drawable.ic_menu_day)
                    }

                    playRainSound(isRainy)

                    binding.tvHourlyTemp1.text = "${temp.toInt()}°"
                    binding.tvHourlyTime1.text = "اکنون"
                    binding.ivHourlyIcon1.setImageResource(if (isRainy) android.R.drawable.ic_menu_compass else android.R.drawable.ic_menu_day)

                    if (hourDataList.size >= 3) {
                        binding.tvHourlyTemp2.text = "${hourDataList[0].second}°"
                        binding.tvHourlyTime2.text = hourDataList[0].first
                        binding.ivHourlyIcon2.setImageResource(if (hourDataList[0].third > 0.0) android.R.drawable.ic_menu_compass else android.R.drawable.ic_menu_day)

                        binding.tvHourlyTemp3.text = "${hourDataList[1].second}°"
                        binding.tvHourlyTime3.text = hourDataList[1].first
                        binding.ivHourlyIcon3.setImageResource(if (hourDataList[1].third > 0.0) android.R.drawable.ic_menu_compass else android.R.drawable.ic_menu_day)

                        binding.tvHourlyTemp4.text = "${hourDataList[2].second}°"
                        binding.tvHourlyTime4.text = hourDataList[2].first
                        binding.ivHourlyIcon4.setImageResource(if (hourDataList[2].third > 0.0) android.R.drawable.ic_menu_compass else android.R.drawable.ic_menu_day)
                    }

                    if (dailyTemps.size >= 3) {
                        binding.tvDay1.text = "فردا: ${dailyTemps[0]}°"
                        binding.tvDay2.text = "پس‌فردا: ${dailyTemps[1]}°"
                        binding.tvDay3.text = "روز سوم: ${dailyTemps[2]}°"
                    }

                    updateBackgroundBasedOnTime()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "خطا در دریافت اطلاعات هواشناسی", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateBackgroundBasedOnTime() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        if (hour in 6..18) {
            binding.rootLayout.setBackgroundColor(android.graphics.Color.parseColor("#1B263B"))
        } else {
            binding.rootLayout.setBackgroundColor(android.graphics.Color.parseColor("#0D1B2A"))
        }
    }
}
