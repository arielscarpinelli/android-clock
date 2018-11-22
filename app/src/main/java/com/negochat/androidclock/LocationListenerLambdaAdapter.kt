package com.negochat.androidclock

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

data class LocationListenerLambdaAdapter(private val lambda: (Location?) -> Unit): LocationListener {
    override fun onLocationChanged(p0: Location?) {
        lambda(p0)
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }
}