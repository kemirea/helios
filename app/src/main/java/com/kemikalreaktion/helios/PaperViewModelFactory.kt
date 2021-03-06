package com.kemikalreaktion.helios

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PaperViewModelFactory(private val context:Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaperViewModel::class.java)) {
            return PaperViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
