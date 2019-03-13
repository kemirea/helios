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

    fun getLocation(): Location? {
        if (mContext.checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
                && mContext.checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            return mLocationManager.getLastKnownLocation(PASSIVE_PROVIDER)
        }
        return null
    }
}