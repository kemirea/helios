package com.kemikalreaktion.helios

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

import java.io.IOException

private const val FILENAME_WALLPAPER_DAY = "wallpaper_day.png"
private const val FILENAME_WALLPAPER_NIGHT = "wallpaper_night.png"

enum class PaperTime {
    DAY,
    NIGHT
}

class WallpaperHelper(private val mContext: Context) {
    private val mWallpaperManager: WallpaperManager
            = mContext.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
    private var mCurrentWallpaper: Drawable? = null
    private var mDayWallpaper: Bitmap? = null
    private var mNightWallpaper: Bitmap? = null

    // Try to set the wallpaper.
    // Return true if successful.
    fun apply(): Boolean {
        mDayWallpaper?.let {
            return set(mDayWallpaper)
        }
        return false
    }

    private fun set(bitmap: Bitmap?): Boolean {
        bitmap?.let {
            try {
                mWallpaperManager.setBitmap(bitmap)
                //mWallpaperManager.setBitmap(Util.drawableToBitmapGlide(mContext, drawable))
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            return true
        }
        return false
    }

    fun reset(): Boolean {
        val wp = mCurrentWallpaper
        wp?.let {
            return set(Util.drawableToBitmap(wp))
        }
        return false
    }

    // TODO: consider creating a custom cropper. When only one page is present, NovaLauncher (or maybe Android?)
    // doesn't always properly position the image after cropping. We also receive a square for the resulting wallpaper
    // drawable in this situation
    fun getCropIntent(imageUri: Uri): Intent? {
        try {
            mCurrentWallpaper = mWallpaperManager.drawable
            return mWallpaperManager.getCropAndSetWallpaperIntent(imageUri)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace();
        }
        return null;
    }

    fun updateWallpaper(time: PaperTime): Bitmap? {
        mWallpaperManager.drawable?.let {
            val wp = Util.drawableToBitmap(mWallpaperManager.drawable)
            if (time == PaperTime.DAY) mDayWallpaper = wp else mNightWallpaper = wp
            saveWallpaperAsync(time, wp)
        }
        return if (time == PaperTime.DAY) mDayWallpaper else mNightWallpaper
    }

    private fun saveWallpaperAsync(time: PaperTime, wallpaper: Bitmap) {
        GlobalScope.launch {
            val file = File(mContext.filesDir, getFilenameForTime(time))
            val os = FileOutputStream(file)
            wallpaper.compress(Bitmap.CompressFormat.PNG, 100, os)
            os.close()
        }
    }

    fun getSavedWallpaperAsync(time: PaperTime): Deferred<Bitmap?> {
        return GlobalScope.async {
            val file = File(mContext.filesDir, getFilenameForTime(time))
            if (file.exists()) {
                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                when (time) {
                    PaperTime.DAY -> mDayWallpaper = bmp
                    PaperTime.NIGHT -> mNightWallpaper = bmp
                }
                bmp
            }
            else null
        }
    }

    private fun getFilenameForTime(time: PaperTime): String? {
        return when(time) {
            PaperTime.DAY -> FILENAME_WALLPAPER_DAY
            PaperTime.NIGHT -> FILENAME_WALLPAPER_NIGHT
        }
    }
}
