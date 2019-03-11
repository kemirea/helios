package com.kemikalreaktion.helios

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri

import java.io.IOException
import android.graphics.drawable.BitmapDrawable
import android.graphics.Canvas

class WallpaperHelper(private val mContext: Context) {
    private val mWallpaperManager: WallpaperManager
            = mContext.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
    private lateinit var mCurrentWallpaper: Drawable
    private lateinit var mNewWallpaper: Drawable

    // Try to set the wallpaper.
    // Return true if successful.
    fun set(): Boolean {
        return set(mNewWallpaper)
    }

    fun set(drawable: Drawable): Boolean {
        val bmp: Bitmap
        if (drawable is BitmapDrawable) {
            android.util.Log.v("catcat", "drawable was a BitmapDrawable!");
            bmp = drawable.bitmap
        } else {
            bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }

        try {
            mWallpaperManager.setBitmap(bmp)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun reset() {
        set(mCurrentWallpaper)
    }

    fun getCropIntent(imageUri: Uri): Intent? {
        try {
            mCurrentWallpaper = mWallpaperManager.drawable
            return mWallpaperManager.getCropAndSetWallpaperIntent(imageUri)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace();
        }
        return null;
    }

    fun addWallpaperAndReset(): Drawable {
        mNewWallpaper = mWallpaperManager.drawable
        reset()
        return mNewWallpaper
    }
}
