package com.kemikalreaktion.helios

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext

private const val FILENAME_WALLPAPER_DAY = "wallpaper_day.png"
private const val FILENAME_WALLPAPER_NIGHT = "wallpaper_night.png"

class PaperManager(private val context: Application) : AndroidViewModel(context) {
    private val wallpaperManager = context.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
    private val locationHelper = LocationHelper(context)
    private val calculator: SunCalculator?
    private val paperRepository: PaperRepository
    private val allPaper: LiveData<List<Paper>>

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    private var currentWallpaper: Drawable? = null
    private var dayWallpaper: Bitmap? = null
    private var nightWallpaper: Bitmap? = null

    init {
        val location = locationHelper.getLocation()
        calculator = location?.let { SunCalculator(location) }

        val paperDao = PaperDatabase.getDatabase(context).paperDao()
        paperRepository = PaperRepository(paperDao)
        allPaper = paperRepository.allPaper
    }


    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

    fun insert(paper: Paper) = scope.launch(Dispatchers.IO) {
        paperRepository.insert(paper)
    }

    // get and apply the wallpaper stored for the specified PaperTime
    // if no wallpaper was saved, do nothing
    fun apply(time: PaperTime) {
        runBlocking {
            getPaperForTimeAsync(time).await()?.let { img -> set(img) }
        }
    }

    // set the bitmap as current wallpaper
    private fun set(bitmap: Bitmap?): Boolean {
        bitmap?.let {
            try {
                wallpaperManager.setBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            return true
        }
        return false
    }

    fun reset(): Boolean {
        val wp = currentWallpaper
        wp?.let { return set(Util.drawableToBitmap(wp)) }
        return false
    }

    // TODO: consider creating a custom cropper. When only one page is present, NovaLauncher (or maybe Android?)
    // doesn't always properly position the image after cropping. We also receive a square for the resulting wallpaper
    // drawable in this situation
    fun getCropIntent(imageUri: Uri): Intent? {
        try {
            currentWallpaper = wallpaperManager.drawable
            return wallpaperManager.getCropAndSetWallpaperIntent(imageUri)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        return null
    }

    // add the wallpaper for given time
    fun addPaperForTime(time: Calendar): Bitmap? {
        wallpaperManager.drawable?.let {
            val wallpaper = Util.drawableToBitmap(wallpaperManager.drawable)
            val paper = Paper(time, 0)

            GlobalScope.launch {
                // save wallpaper to internal storage
                val file = File(context.filesDir, paper.filename)
                val os = FileOutputStream(file)
                wallpaper.compress(Bitmap.CompressFormat.PNG, 100, os)
                os.close()

                // add paper object to repository
                paperRepository.insert(paper)
            }

            reset()
            return wallpaper
        }
        return null
    }

    // add the wallpaper for given PaperTime
    fun addPaperForPaperTime(time: PaperTime): Bitmap? {
        if (wallpaperManager.drawable != null && calculator != null){
            val wallpaper = Util.drawableToBitmap(wallpaperManager.drawable)
            val paper = Paper(calculator.getByPaperTime(time), 0, time)

            GlobalScope.launch {
                // save wallpaper to internal storage
                val file = File(context.filesDir, getFilenameForTime(time))
                val os = FileOutputStream(file)
                wallpaper.compress(Bitmap.CompressFormat.PNG, 100, os)
                os.close()

                // add paper object to repository
                paperRepository.insert(paper)
            }

            reset()
            return wallpaper
        }
        return null
    }

    fun getPaperForTimeAsync(time: PaperTime): Deferred<Bitmap?> {
        return GlobalScope.async {
            val file = File(context.filesDir, getFilenameForTime(time))
            if (file.exists()) {
                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                when (time) {
                    PaperTime.SUNRISE -> dayWallpaper = bmp
                    PaperTime.SUNSET -> nightWallpaper = bmp
                }
                bmp
            } else null
        }
    }

    private fun getFilenameForTime(time: PaperTime): String? {
        return when(time) {
            PaperTime.SUNRISE -> FILENAME_WALLPAPER_DAY
            PaperTime.SUNSET -> FILENAME_WALLPAPER_NIGHT
        }
    }

    fun getIntentForTime(time: PaperTime): Intent {
        return Intent(context, WallpaperBroadcastReceiver::class.java).let { intent ->
            intent.action = ACTION_APPLY_WALLPAPER
            intent.putExtra(EXTRA_PAPER_TIME, time.ordinal)
        }
    }

    fun scheduleNextUpdate(time: PaperTime) {
        LocationHelper(context).getLocation()?.let { location ->
            val calculator = SunCalculator(location)
            val mAlarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (time == PaperTime.SUNRISE) {
                val sunsetIntent = getIntentForTime(PaperTime.SUNSET).let { intent ->
                    PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT) }
                mAlarmManager.set(AlarmManager.RTC, calculator.getSunset().timeInMillis, sunsetIntent)
            } else if (time == PaperTime.SUNSET) {
                val sunriseIntent = getIntentForTime(PaperTime.SUNRISE).let { intent ->
                    PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT) }
                mAlarmManager.set(AlarmManager.RTC, calculator.getSunrise().timeInMillis, sunriseIntent)
            }
        }
    }
}
