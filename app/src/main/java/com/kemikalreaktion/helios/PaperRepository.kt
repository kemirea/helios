package com.kemikalreaktion.helios

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import java.util.*

class PaperRepository(private val paperDao: PaperDao) {
    val allPaper: LiveData<List<Paper>> = paperDao.getAll()

    @WorkerThread
    suspend fun insert(paper: Paper) {
        paperDao.insert(paper)
    }

    fun getPaperForTime(time: Calendar) = paperDao.getPaperByTime(time)

    fun getPaperForPaperTime(paperTime: PaperTime) = paperDao.getPaperByPaperTime(paperTime)
}