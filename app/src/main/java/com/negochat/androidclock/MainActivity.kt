package com.negochat.androidclock

import android.content.Context
import android.graphics.Typeface
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatTextView
import android.view.WindowManager
import android.widget.TextView
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var clock: AppCompatTextView
    private lateinit var date: TextView
    private lateinit var amPm: TextView
    private lateinit var currentTemp: TextView
    private lateinit var forecastTemp: TextView

    private lateinit var handler: Handler

    private var timer: Timer? = null

    private val dateFormat = SimpleDateFormat("EEE, d MMM")
    private val clockFormat = SimpleDateFormat("hh:mm")
    private val amPmFormat = SimpleDateFormat("a")

    private val temperatureFormat = DecimalFormat("#0.0°C")
    private val weatherApi = WeatherApi(BuildConfig.OPEN_WEATHER_MAP_API_KEY)
    private var nightTheme = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (shouldUpdateTheme(Date())) {
            applyTheme()
        }

        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        clock = findViewById(R.id.clock) as AppCompatTextView
        clock.typeface = Typeface.createFromAsset(assets, "font/digital-7 (mono).ttf")

        date = findViewById(R.id.date) as TextView
        amPm = findViewById(R.id.amPm) as TextView
        currentTemp = findViewById(R.id.currentTemperature) as TextView
        currentTemp.text = ""

        forecastTemp = findViewById(R.id.forecastTemperature) as TextView
        forecastTemp.text = ""

        handler = Handler()
        updateTime()
    }

    private fun applyTheme() {
        setTheme(if (nightTheme) R.style.NightTheme else R.style.DayTheme)
    }

    private fun shouldUpdateTheme(now: Date): Boolean {
        val shouldBeLightMode = now.hours in 7..21
        if (nightTheme && shouldBeLightMode) {
            nightTheme = false
            return true
        } else if (!nightTheme && !shouldBeLightMode) {
            nightTheme = true
            return true
        }
        return false
    }

    private fun scheduleWeatherFetch() {
        timer = Timer()
        val myLooper = Looper.myLooper()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val locationManager: LocationManager? = this@MainActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                locationManager?.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, LocationListenerLambdaAdapter(this@MainActivity::updateWeatherData), myLooper)
            }
        }, 0, 60000 * 5)
    }

    private fun updateWeatherData(location: Location?) {
        if (location != null) {
            weatherApi.currentTemperature(location.latitude, location.longitude, { current ->
                handler.post({ currentTemp.text = temperatureFormat.format(current) })
            })
            val nextTwelveHours = (System.currentTimeMillis() / 1000) + 12 * 3600
            weatherApi.forecastTemperature(location.latitude, location.longitude, nextTwelveHours, { min, max ->
                handler.post({ forecastTemp.text = temperatureFormat.format(min) + " - " + temperatureFormat.format(max) })
            })
        }
    }

    override fun onResume() {
        super.onResume()
        scheduleWeatherFetch()
    }

    override fun onPause() {
        super.onPause()
        timer?.cancel()
    }

    private fun updateTime() {
        val now = Date()

        if (shouldUpdateTheme(now)) {
            val intent = intent
            finish()
            startActivity(intent)
        }

        var textHour = clockFormat.format(now)
        if (textHour.startsWith("0")) {
            textHour = "  " + textHour.substring(1);
        }
        clock.text = textHour

        date.text = dateFormat.format(now)

        amPm.text = amPmFormat.format(now)

        handler.postDelayed(this::updateTime, 1000)
    }

}