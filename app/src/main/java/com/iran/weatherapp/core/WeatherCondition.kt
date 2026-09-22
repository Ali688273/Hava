package com.iran.weatherapp.core
enum class WeatherCondition{CLEAR,CLOUDY,RAIN,SNOW,STORM,UNKNOWN}
object WeatherConditionMapper{
 fun fromPrecipitation(precipitationMm:Double,cloudPercent:Int=0)=when{
  precipitationMm>=8.0->WeatherCondition.STORM
  precipitationMm>0.0->WeatherCondition.RAIN
  cloudPercent>=70->WeatherCondition.CLOUDY
  cloudPercent>=0->WeatherCondition.CLEAR
  else->WeatherCondition.UNKNOWN
 }
}