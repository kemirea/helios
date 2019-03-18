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
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kemikalreaktion.helios.data.Paper
import com.kemikalreaktion.helios.data.PaperDatabase
import com.kemikalreaktion.helios.data.PaperRepository
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext

class PaperViewModel(private val context: Context) : ViewModel() {
    private val wallpaperManager = context.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
    private val locationHelper = LocationHelper(context)
    private val paperRepository: PaperRepository
    private val allPaper: LiveData<List<Paper>>
    val sunCalculator: SunCalculator?

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    private var currentWallpaper: Drawable? = null

    init {
        val location = locationHelper.getLocation()
        sunCalculator = location?.let { SunCalculator(location) }

        val paperDao = PaperDatabase.getDatabase(context).paperDao()
        paperRepository = PaperRepository(paperDao)
        allPaper = paperRepository.allPaper
    }


    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

    // get and apply the wallpaper stored for the specified PaperTime
    // if no wallpaper was saved, do nothing
    fun apply() {
        sunCalculator?.let {
            val currentPaperTime = sunCalculator.getCurrentPaperTime()
            runBlocking {
                getPaperForPaperTimeAsync(currentPaperTime).await()?.let { img -> set(img) }
            }
            scheduleNextUpdate(currentPaperTime)
        }
    }

    fun apply(time: PaperTime) {
        runBlocking {
            getPaperForPaperTimeAsync(time).await()?.let { img -> set(img) }
        }
        sunCalculator?.let { scheduleNextUpdate(sunCalculator.getCurrentPaperTime()) }
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

    private fun reset(): Boolean {
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

    fun insert(paper: Paper) = scope.launch(Dispatchers.IO) {
        paperRepository.insert(paper)
    }

    // add the wallpaper for given time
    fun addPaperForTime(time: Calendar): Bitmap? {
        wallpaperManager.drawable?.let {
            val wallpaper = Util.drawableToBitmap(wallpaperManager.drawable)
            val paper = Paper(time, 0)

            scope.launch(Dispatchers.IO) {
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
        if (wallpaperManager.drawable != null && sunCalculator != null){
            val wallpaper = Util.drawableToBitmap(wallpaperManager.drawable)
            val paper = Paper(sunCalculator.getByPaperTime(time), 0, time)

            scope.launch(Dispatchers.IO) {
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

    fun getPaperForTimeAsync(time: Calendar): Deferred<Bitmap?> {
        return scope.async(Dispatchers.IO) {
            val paper = paperRepository.getPaperForTime(time).value
            paper?.let {
                val file = File(context.filesDir, paper.filename)
                if (file.exists()) {
                    return@async BitmapFactory.decodeFile(file.absolutePath)
                }
            }
            null
        }
    }

    fun getPaperForPaperTimeAsync(time: PaperTime): Deferred<Bitmap?> {
        return scope.async(Dispatchers.IO) {
            val paper = paperRepository.getPaperForPaperTime(time)
            paper?.let {
                val file = File(context.filesDir, paper.filename)
                if (file.exists()) {
                    return@async BitmapFactory.decodeFile(file.absolutePath)
                }
            }
            null
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
