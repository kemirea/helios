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
            val time: PaperTime = intent.getSerializableExtra(EXTRA_PAPER_TIME) as PaperTime
            when(time) {
                PaperTime.DAY -> {
                    Log.v(DEBUG_TAG, "received broadcast for day wallpaper")
                    WallpaperHelper(context).apply(time)
                }
                PaperTime.NIGHT -> {
                    Log.v(DEBUG_TAG, "received broadcast for night wallpaper")
                    WallpaperHelper(context).apply(time)
                }
            }
        }
    }
}
