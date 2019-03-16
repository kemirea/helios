package com.kemikalreaktion.helios.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kemikalreaktion.helios.PaperTime
import java.util.*

@Dao
interface PaperDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(paper: Paper)

    @Delete
    fun delete(paper: Paper)

    @Update
    fun update(paper: Paper)

    @Query("SELECT * from paper_table ORDER BY time ASC")
    fun getAll(): LiveData<List<Paper>>

    @Query("SELECT * from paper_table where paper_time = :time")
    fun getPaperByTime(time: Calendar): LiveData<Paper>

    @Query("SELECT * from paper_table where paper_time = :paperTime")
    fun getPaperByPaperTime(paperTime: PaperTime): Paper?
}