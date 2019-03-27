package com.kemikalreaktion.helios

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.kemikalreaktion.helios.data.Paper
import java.io.File

/**
 * Utility methods for handling mundane tasks
 */

const val DEBUG_TAG = "DEBUG_CAT"

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
    fun getBitmapForPaper(context: Context, paper: Paper?): Bitmap? {
        paper?.let{
            val file = File(context.filesDir, paper.filename)
            if (file.exists()) {
                return BitmapFactory.decodeFile(file.absolutePath)
            }
        }
        return null
    }

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
}
