package com.kemikalreaktion.helios

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.WallpaperManager
import android.app.WallpaperManager.FLAG_LOCK
import android.app.WallpaperManager.FLAG_SYSTEM
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kemikalreaktion.helios.Util.getBitmapForPaper
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
    val allPaper: LiveData<List<Paper>>
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

    // get and setForPaperTime the wallpaper stored for the specified PaperTime
    // if no wallpaper was saved, do nothing
    fun apply() {
        sunCalculator?.let {
            val currentPaperTime = sunCalculator.getCurrentPaperTime()
            setForPaperTime(currentPaperTime)
            scheduleNextUpdate(currentPaperTime)
        }
    }

    // get bitmap for current PaperTime and set as current wallpaper
    fun setForPaperTime(time: PaperTime) {
        scope.launch(Dispatchers.IO) {
            paperRepository.getPaperForPaperTime(time)?.let { paper ->
                getBitmapForPaper(context, paper)?.let { bitmap ->
                    try {
                        wallpaperManager.setBitmap(bitmap, null, true, paper.which)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun reset(): Boolean {
        currentWallpaper?.let { wallpaper ->
            try {
                wallpaperManager.setBitmap(Util.drawableToBitmap(wallpaper), null, true, FLAG_SYSTEM or FLAG_LOCK)
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            return true
        }
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
    fun addPaperForTime(id: Int, time: Calendar): Bitmap? {
        wallpaperManager.drawable?.let {
            val wallpaper = Util.drawableToBitmap(wallpaperManager.drawable)
            // TODO; we should take the result of the crop intent for the which argument
            val paper = Paper(id, time, FLAG_SYSTEM or FLAG_LOCK)

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
    fun addPaperForPaperTime(id: Int, time: PaperTime): Bitmap? {
        if (wallpaperManager.drawable != null && sunCalculator != null){
            val wallpaper = Util.drawableToBitmap(wallpaperManager.drawable)
            // TODO; we should take the result of the crop intent for the which argument
            val paper = Paper(id, sunCalculator.getByPaperTime(time), FLAG_SYSTEM or FLAG_LOCK, time)

            scope.launch(Dispatchers.IO) {
                // save wallpaper to internal storage
                val file = File(context.filesDir, paper.filename)
                val os = FileOutputStream(file)
                wallpaper.compress(Bitmap.CompressFormat.PNG, 100, os)
                os.close()

                // add paper object to repository
                paperRepository.insert(paper)
            }

            return wallpaper
        }
        return null
    }

    fun addOrUpdatePaper(paper: Paper): Bitmap? {
        var wallpaper: Bitmap? = null
        wallpaperManager.drawable?.let { drawable ->
            wallpaper = Util.drawableToBitmap(drawable)
            scope.launch(Dispatchers.IO) {
                // save wallpaper to internal storage
                val file = File(context.filesDir, paper.filename)
                val os = FileOutputStream(file)
                wallpaper?.compress(Bitmap.CompressFormat.PNG, 100, os)
                os.close()

                // add paper object to repository
                paperRepository.insert(paper)
            }
        }

        reset()
        return wallpaper
    }

    fun getBitmapForTimeAsync(time: Calendar): Deferred<Bitmap?> {
        return scope.async(Dispatchers.IO) {
            getBitmapForPaper(context, paperRepository.getPaperForTime(time))
        }
    }

    fun getBitmapForPaperTimeAsync(time: PaperTime): Deferred<Bitmap?> {
        return scope.async(Dispatchers.IO) {
            getBitmapForPaper(context, paperRepository.getPaperForPaperTime(time))
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
