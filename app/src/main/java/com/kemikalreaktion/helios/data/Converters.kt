package com.kemikalreaktion.helios.data

import androidx.room.TypeConverter
import com.kemikalreaktion.helios.PaperTime
import java.util.*

class Converters {
    @TypeConverter
    fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

    @TypeConverter
    fun datestampToCalendar(value: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = value }

    @TypeConverter
    fun paperTimeToOrdinal(paperTime: PaperTime): Int = paperTime.ordinal

    @TypeConverter
    fun ordinalToPaperTime(ordinal: Int): PaperTime = PaperTime.values()[ordinal]
}