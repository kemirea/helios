package com.kemikalreaktion.helios

import android.view.View
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * PagerSnapHelper with some additional functions for getting the current position and view
 */
class PositionalSnapHelper(private val recyclerView: RecyclerView) : PagerSnapHelper() {
    fun getPosition(): Int {
        val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
        val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        return layoutManager.getPosition(snapView)
    }

    fun getCurrentView(): View? {
        val layoutManager = recyclerView.layoutManager
        return layoutManager?.let {
            findSnapView(layoutManager)
        }
    }
}
