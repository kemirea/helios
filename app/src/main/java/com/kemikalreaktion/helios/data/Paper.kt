package com.kemikalreaktion.helios.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kemikalreaktion.helios.PaperTime
import java.util.*

@Entity(tableName = "paper_table")
data class Paper(@PrimaryKey val id: Int,
                 val time: Calendar,
                 val filename: String,
                 val which: Int,
                 @ColumnInfo(name = "paper_time") val paperTime: PaperTime?) : Comparable<Paper> {
    constructor(id: Int, time: Calendar, which: Int) : this(id, time, "${time.time}_$which.png", which, null)
    constructor(id: Int, time: Calendar, which: Int, paperTime: PaperTime) :
            this(id, time, "${paperTime.name}_$which.png", which, paperTime)

    override fun equals(other: Any?): Boolean {
        return other is Paper && id == other.id
    }

    override fun compareTo(other: Paper): Int {
        return time.compareTo(other.time)
    }
}