package com.kemikalreaktion.helios

import android.location.Location
import android.util.Log
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
        val sunrise = mCalculator.getOfficialSunriseCalendarForDate(Calendar.getInstance())
        Log.v(DEBUG_TAG, "Sunrise time: ${sunrise.time}")
        return sunrise
    }

    fun getSunset(): Calendar {
        val sunset = mCalculator.getOfficialSunsetCalendarForDate(Calendar.getInstance())
        Log.v(DEBUG_TAG, "Sunrise time: ${sunset.time}")
        return sunset
    }

    fun getCurrentPaperTime(): PaperTime {
        val currentTime = Calendar.getInstance()
        return if (currentTime > getSunrise() && currentTime < getSunset()) {
            PaperTime.DAY
        } else {
            PaperTime.NIGHT
        }
    }
}
