package com.iran.weatherapp

data class WeatherResponse(
    val properties: Properties
)

data class Properties(
    val timeseries: List<TimeSeries>
)

data class TimeSeries(
    val time: String,
    val data: InstantData
)

data class InstantData(
    val instant: DetailsWrapper,
    val next_1_hours: NextHours?
)

data class DetailsWrapper(
    val details: WeatherDetails
)

data class WeatherDetails(
    val air_temperature: Double,
    val relative_humidity: Double,
    val wind_speed: Double,
    val ultraviolet_index_clear_sky: Double?
)

data class NextHours(
    val summary: WeatherSummary,
    val details: PrecipitationDetails?
)

data class WeatherSummary(
    val symbol_code: String
)

data class PrecipitationDetails(
    val precipitation_amount: Double?
)
