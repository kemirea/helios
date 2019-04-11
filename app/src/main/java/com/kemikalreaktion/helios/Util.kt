package com.kemikalreaktion.helios

import android.Manifest
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.kemikalreaktion.helios.data.Paper
import java.io.File

/**
 * Utility methods for handling mundane tasks
 */

const val DEBUG_TAG = "DEBUG_CAT"

const val REQUEST_CODE_PERMISSIONS = 0
const val REQUEST_CODE_CHOOSE_IMAGE = 1
const val REQUEST_CODE_CROP_IMAGE = 2

val REQUIRED_PERMISSIONS = arrayOf(
    // required to grab current background
    Manifest.permission.READ_EXTERNAL_STORAGE,
    // required for sunrise/sunset time
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION
)

// constants for intents
const val ACTION_APPLY_WALLPAPER = "com.kemikalreaktion.helios.ACTION_APPLY_WALLPAPER"
const val EXTRA_PAPER_TIME = "com.kemikalreaktion.helios.EXTRA_PAPER_TIME"

// STOP
// IT'S PAPERTIME
enum class PaperTime {
    SUNRISE,
    SUNSET,
    CUSTOM
}

object Util {
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bmp: Bitmap
        if (drawable is BitmapDrawable) {
            bmp = drawable.bitmap
        } else {
            bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
        return bmp
    }

    fun drawableToBitmapGlide(context: Context, drawable: Drawable): Bitmap {
        return Glide.with(context).asBitmap().load(drawable).submit().get()
    }

    fun getDisplayRatioString(context: Context): String {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val ratioFloat: Float = displayMetrics.widthPixels.toFloat() / displayMetrics.heightPixels.toFloat()
        return "W,$ratioFloat"
    }
}
