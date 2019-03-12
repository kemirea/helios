package com.kemikalreaktion.helios

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


/**
 * TODO:
 *  - Save last known location to SharedPreferences
 *  - Check for location updates
 *  - How to handle null location?
 */

class LocationHelper(private val mContext: Context) {
    private val mFusedLocationProvider: FusedLocationProviderClient
            = LocationServices.getFusedLocationProviderClient(mContext)
    var mCurrentLocation: Location? = null

    init {
        if (mContext.checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
            mFusedLocationProvider.lastLocation
                .addOnSuccessListener {
                        location: Location? -> mCurrentLocation = location
                }
        }
    }
}