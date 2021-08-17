package com.negochat.androidclock

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

data class Weather(val current: Float, val min: Float, val max: Float)

class WeatherApi(val appId:String) {

    private val client = OkHttpClientFactory.getUnsafeOkHttpClient()
    private val objectMapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun currentTemperature(lat: Double, lon: Double, callback: (Double) -> Unit) {

        val request = Request.Builder()
                .url("https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$appId&units=metric")
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                response.body().use({ responseBody ->
                    if (!response.isSuccessful) throw IOException("Unexpected code " + response)

                    val result: WeatherResponse = objectMapper.readValue(responseBody?.byteStream(), WeatherResponse::class.java)

                    callback(result.main.temp)

                })
            }
        })

    }

    fun forecastTemperature(lat: Double, lon: Double, untilUnixTimestampSec: Long, callback:(min:Double, max:Double) -> Unit) {
        val request = Request.Builder()
                .url("https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$lon&appid=$appId&units=metric")
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                response.body().use({ responseBody ->
                    if (!response.isSuccessful) throw IOException("Unexpected code " + response)

                    val result: ForecastResponse = objectMapper.readValue(responseBody?.byteStream(), ForecastResponse::class.java)

                    val temps = result.list
                            .filter { (dt) -> dt <= untilUnixTimestampSec }
                            .map { item -> item.main.temp }

                    callback(temps.min()!!, temps.max()!!)

                })
            }
        })
    }

    data class WeatherResponseMain(val temp: Double)
    data class WeatherResponse(val main: WeatherResponseMain)
    data class ForecastResponseItem(val dt: Long, val main: WeatherResponseMain)
    data class ForecastResponse(val list: List<ForecastResponseItem>)
}