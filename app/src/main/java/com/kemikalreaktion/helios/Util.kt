package com.kemikalreaktion.helios

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide

/**
 * Utility methods for handling mundane tasks
 */

const val DEBUG_TAG = "DEBUG_CAT"

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
}
