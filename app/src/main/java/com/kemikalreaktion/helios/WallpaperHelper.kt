package com.kemikalreaktion.helios

import android.app.AlarmManager
import android.app.PendingIntent
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

class WallpaperHelper(private val mContext: Context) {
    private val mWallpaperManager: WallpaperManager
            = mContext.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
    private var mCurrentWallpaper: Drawable? = null
    private var mDayWallpaper: Bitmap? = null
    private var mNightWallpaper: Bitmap? = null

    // get and apply the wallpaper stored for the specified PaperTime
    // if no wallpaper was saved, do nothing
    fun apply(time: PaperTime) {
        runBlocking {
            getSavedWallpaperAsync(time).await()?.let {
                    img -> set(img)
            }
        }
    }

    // set the bitmap as current wallpaper
    private fun set(bitmap: Bitmap?): Boolean {
        bitmap?.let {
            try {
                mWallpaperManager.setBitmap(bitmap)
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

    // update the saved wallpaper for given PaperTime
    fun updateWallpaperForTime(time: PaperTime): Bitmap? {
        mWallpaperManager.drawable?.let {
            val wallpaper = Util.drawableToBitmap(mWallpaperManager.drawable)
            // save wallpaper to internal storage
            GlobalScope.launch {
                val file = File(mContext.filesDir, getFilenameForTime(time))
                val os = FileOutputStream(file)
                wallpaper.compress(Bitmap.CompressFormat.PNG, 100, os)
                os.close()
            }

            if (time == PaperTime.DAY)
                mDayWallpaper = wallpaper
            else
                mNightWallpaper = wallpaper

            return wallpaper
        }
        return null
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

    fun getIntentForTime(time: PaperTime): Intent {
        return Intent(mContext, WallpaperBroadcastReceiver::class.java).let {
                intent -> intent.action = ACTION_APPLY_WALLPAPER
            intent.putExtra(EXTRA_PAPER_TIME, time.ordinal)
        }
    }

    fun updatePaperSchedule() {
        LocationHelper(mContext).getLocation()?.let {
                location ->
            val calculator = SunCalculator(location)
            val mAlarmManager: AlarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val sunriseIntent = getIntentForTime(PaperTime.DAY).let {
                    intent -> PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT) }
            val sunsetIntent = getIntentForTime(PaperTime.NIGHT).let {
                    intent -> PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT) }
            mAlarmManager.setInexactRepeating(
                AlarmManager.RTC, calculator.getSunrise().timeInMillis,
                AlarmManager.INTERVAL_DAY, sunriseIntent)
            mAlarmManager.setInexactRepeating(
                AlarmManager.RTC, calculator.getSunset().timeInMillis,
                AlarmManager.INTERVAL_DAY, sunsetIntent)
        }
    }
}
