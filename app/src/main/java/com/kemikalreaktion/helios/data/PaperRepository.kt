package com.kemikalreaktion.helios.data

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.kemikalreaktion.helios.PaperTime
import java.util.*

class PaperRepository(private val paperDao: PaperDao) {
    val allPaper: LiveData<List<Paper>> = paperDao.getAll()

    @WorkerThread
    suspend fun insert(paper: Paper) {
        paperDao.insert(paper)
    }

    @WorkerThread
    suspend fun delete(paper: Paper) {
        paperDao.delete(paper)
    }

    fun getPaperById(id: Int) = paperDao.getPaperById(id)

    fun getPaperForTime(time: Calendar) = paperDao.getPaperByTime(time)

    fun getPaperForPaperTime(paperTime: PaperTime) = paperDao.getPaperByPaperTime(paperTime)
}