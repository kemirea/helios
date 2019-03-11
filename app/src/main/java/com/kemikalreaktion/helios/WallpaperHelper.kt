package com.kemikalreaktion.helios

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri

import java.io.IOException

class WallpaperHelper(private val mContext: Context) {
    private val mWallpaperManager: WallpaperManager
            = mContext.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
    private var mCurrentWallpaper: Drawable? = null
    private var mNewWallpaper: Drawable? = null

    // Try to set the wallpaper.
    // Return true if successful.
    fun set(): Boolean {
        mNewWallpaper?.let {
            return set(mNewWallpaper)
        }
        return false
    }

    fun set(drawable: Drawable?): Boolean {
        drawable?.let {
            try {
                mWallpaperManager.setBitmap(Util.drawableToBitmap(drawable))
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
        return set(mCurrentWallpaper)
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

    fun addWallpaperAndReset(): Drawable? {
        mNewWallpaper = mWallpaperManager.drawable
        reset()
        return mNewWallpaper
    }
}
