package com.kemikalreaktion.helios

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Handle applying wallpaper when alarm goes off
 */
class WallpaperBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_APPLY_WALLPAPER) {
            val index = intent.getIntExtra(EXTRA_PAPER_TIME, PaperTime.DAY.ordinal)
            val time = PaperTime.values()[index]
            WallpaperHelper(context).apply(time)
            WallpaperHelper(context).scheduleNextUpdate(time)
        }
    }
}
