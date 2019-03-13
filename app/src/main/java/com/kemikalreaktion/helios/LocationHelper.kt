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

class LocationHelper(private val context: Context) {
    private val locationManager: LocationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager

    fun getLocation(): Location? {
        if (context.checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
                && context.checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            return locationManager.getLastKnownLocation(PASSIVE_PROVIDER)
        }
        return null
    }
}