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
        return calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance())
    }

    fun getSunset(): Calendar {
        return calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance())
    }

    fun getByPaperTime(time: PaperTime): Calendar {
        return when(time) {
            PaperTime.SUNRISE -> getSunrise()
            PaperTime.SUNSET -> getSunset()
            PaperTime.CUSTOM -> Calendar.getInstance()
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
