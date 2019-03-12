package com.kemikalreaktion.helios

import android.location.Location
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import java.util.*



/**
 * Wrapper class for SunriseSunsetCalculator library
 * Simplifies the library down to just the methods we need.
 * Allows for better compatibility with modern APIs and Kotlin.
 */
class SunCalculator(mLocation: Location) {
    private val mCalculator: SunriseSunsetCalculator = SunriseSunsetCalculator(
        com.luckycatlabs.sunrisesunset.dto.Location(mLocation.latitude, mLocation.longitude), TimeZone.getDefault())

    fun getSunrise(): Calendar {
        return mCalculator.getOfficialSunriseCalendarForDate(Calendar.getInstance())
    }

    fun getSunset(): Calendar {
        return mCalculator.getOfficialSunsetCalendarForDate(Calendar.getInstance())
    }
}
