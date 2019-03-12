package com.kemikalreaktion.helios

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationManager
import android.location.LocationManager.PASSIVE_PROVIDER


/**
 * TODO:
 *  - Save last known location to SharedPreferences
 *  - Check for location updates
 *  - How to handle null location?
 */

class LocationHelper(private val mContext: Context) {
    private val mLocationManager: LocationManager = mContext.getSystemService(LOCATION_SERVICE) as LocationManager
    var mCurrentLocation: Location? = null

    fun getLocation() {
        if (mContext.checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
                && mContext.checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            val location = mLocationManager.getLastKnownLocation(PASSIVE_PROVIDER)
            mCurrentLocation = location
            location?.let {
                val calculator = SunCalculator(location)
                android.util.Log.v(DEBUG_TAG, "Sunrise time: ${calculator.getSunrise().time}")
                android.util.Log.v(DEBUG_TAG, "Sunset time: ${calculator.getSunset().time}")
            }
        }
    }
}