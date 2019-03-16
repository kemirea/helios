package com.kemikalreaktion.helios

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "paper_table", primaryKeys = arrayOf("time", "paper_time"))
data class Paper(@PrimaryKey val time: Calendar,
                 val filename: String,
                 val which: Int,
                 @ColumnInfo(name = "paper_time") val paperTime: PaperTime?) : Comparable<Paper> {
    constructor(time: Calendar, which: Int) : this(time, "${time.time}_$which.png", which, null)
    constructor(time: Calendar, which: Int, paperTime: PaperTime) :
            this(time, "${paperTime.name}_$which.png", which, paperTime)

    override fun equals(other: Any?): Boolean {
        return if (other is Paper) {
            if (paperTime != null) {
                paperTime == other.paperTime
            } else {
                time == other.time
            }
        } else {
            false
        }
    }

    override fun compareTo(other: Paper): Int {
        return time.compareTo(other.time)
    }
}