package com.kemikalreaktion.helios

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class PaperRepository(private val paperDao: PaperDao) {
    val allPaper: LiveData<List<Paper>> = paperDao.getAll()

    @WorkerThread
    suspend fun insert(paper: Paper) {
        paperDao.insert(paper)
    }
}