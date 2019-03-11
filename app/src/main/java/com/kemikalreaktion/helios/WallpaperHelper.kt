package com.kemikalreaktion.helios

import android.app.WallpaperManager
import android.app.WallpaperManager.FLAG_SYSTEM
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri

import java.io.IOException

class WallpaperHelper(private val mContext: Context) {
    private val mWallpaperManager: WallpaperManager
            = mContext.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager

    // Try to set the wallpaper.
    // Return true if successful.
    fun set(): Boolean {
        try {
            mWallpaperManager.setResource(R.raw.omurice)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun set(bitmap: Bitmap): Boolean {
        try {
            mWallpaperManager.setBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun revert() {
        try {
            mWallpaperManager.clear(FLAG_SYSTEM)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    fun getCropIntent(imageUri: Uri): Intent? {
        try {
            return mWallpaperManager.getCropAndSetWallpaperIntent(imageUri)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace();
        }
        return null;
    }
}
