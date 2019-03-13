package com.kemikalreaktion.helios

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

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
                    Log.v(DEBUG_TAG, "setting day wallpaper")
                }
                PaperTime.NIGHT -> {
                    WallpaperHelper(context).apply(time)
                    Log.v(DEBUG_TAG, "setting night wallpaper")
                }
            }
        }
    }
}
