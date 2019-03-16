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

class SunCalculator(location: Location) {
    private val calculator: SunriseSunsetCalculator = SunriseSunsetCalculator(
        com.luckycatlabs.sunrisesunset.dto.Location(location.latitude, location.longitude), TimeZone.getDefault())

    fun getSunrise(): Calendar {
        val sunrise = calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance())
        Log.v(DEBUG_TAG, "Sunrise time: ${sunrise.time}")
        return sunrise
    }

    fun getSunset(): Calendar {
        val sunset = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance())
        Log.v(DEBUG_TAG, "Sunrise time: ${sunset.time}")
        return sunset
    }

    fun getByPaperTime(time: PaperTime): Calendar {
        return when(time) {
            PaperTime.SUNRISE -> getSunrise()
            PaperTime.SUNSET -> getSunset()
        }
    }

    fun getCurrentPaperTime(): PaperTime {
        val currentTime = Calendar.getInstance()
        return if (currentTime > getSunrise() && currentTime < getSunset()) {
            PaperTime.SUNRISE
        } else {
            PaperTime.SUNSET
        }
    }
}
