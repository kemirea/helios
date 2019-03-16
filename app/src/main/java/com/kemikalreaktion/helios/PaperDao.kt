package com.kemikalreaktion.helios

import androidx.lifecycle.LiveData
import androidx.room.*

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
}