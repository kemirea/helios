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
            when (time) {
                PaperTime.DAY -> {
                    WallpaperHelper(context).apply(time)

                    // first wallpaper update of the day, also update alarms
                    WallpaperHelper(context).updatePaperSchedule()
                }
                PaperTime.NIGHT -> {
                    WallpaperHelper(context).apply(time)
                }
            }
        }
    }
}
