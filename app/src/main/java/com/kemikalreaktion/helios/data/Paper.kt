package com.kemikalreaktion.helios.data

import android.app.WallpaperManager
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kemikalreaktion.helios.PaperTime
import java.util.*

@Entity(tableName = "paper_table")
data class Paper(@PrimaryKey val id: Int,
                 var time: Calendar,
                 val filename: String,
                 var which: Int,
                 @ColumnInfo(name = "paper_time") var paperTime: PaperTime) : Comparable<Paper> {
    constructor(id: Int) : this(id, Calendar.getInstance(), "heliospaper-$id.png",
            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK, PaperTime.CUSTOM)
    constructor(id: Int, time: Calendar, which: Int) : this(id, time, "heliospaper-$id.png", which, PaperTime.CUSTOM)
    constructor(id: Int, time: Calendar, which: Int, paperTime: PaperTime) :
            this(id, time, "heliospaper-$id.png", which, paperTime)

    override fun equals(other: Any?): Boolean {
        return other is Paper && id == other.id
    }

    override fun compareTo(other: Paper): Int {
        return time.compareTo(other.time)
    }
}