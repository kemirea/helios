package com.kemikalreaktion.helios

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT

class MainActivity : AppCompatActivity() {
    private lateinit var mWallpaperHelper : WallpaperHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mWallpaperHelper = WallpaperHelper(this)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    fun onApplyClicked(view: View) {
        mWallpaperHelper.setWallpaper();
        Toast.makeText(this, "Wallpaper applied!", LENGTH_SHORT).show()
    }

    fun onRevertClicked(view: View) {
        Toast.makeText(this, "Wallpaper reverted!", LENGTH_SHORT).show()
    }
}
