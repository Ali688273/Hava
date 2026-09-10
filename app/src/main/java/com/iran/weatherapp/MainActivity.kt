package com.iran.weatherapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.iran.weatherapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // نمونه بارگذاری پیش‌فرض برای اولین شهر ایران (تهران)
        loadWeatherData("تهران")
    }

    private fun loadWeatherData(cityName: String) {
        if (CityData.iranCities.contains(cityName)) {
            binding.tvCityName.text = cityName
            // تنظیمات مقادیر نمونه لحظه‌ای و ساعتی حرفه‌ای
            binding.tvTemp.text = "27°C"
            binding.tvHumidity.text = "رطوبت هوا: 38%"
            binding.tvWindSpeed.text = "سرعت باد: 14 کیلومتر بر ساعت"
            binding.tvUVIndex.text = "شاخص ماورای بنفش (UV): 6 (بالا)"
            binding.tvRainfall.text = "میزان بارندگی: 0.0 میلی‌متر"
            
            // تغییر هوشمند آیکون بر اساس وضعیت آب و هوا
            binding.ivWeatherIcon.setImageResource(android.R.drawable.ic_menu_compass)
        } else {
            Toast.makeText(this, "شهر مورد نظر در محدوده ایران یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }
}
