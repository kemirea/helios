package com.kemikalreaktion.helios

import android.app.WallpaperManager
import android.content.Context

import java.io.IOException

class WallpaperHelper(private val mContext: Context) {
    private val mWallpaperManager: WallpaperManager
            = mContext.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager

    // Try to set the wallpaper.
    // Return true if successful.
    fun setWallpaper(): Boolean {
        try {
            mWallpaperManager.setResource(R.raw.omurice)
            return true
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            return false
        }

    }
}
