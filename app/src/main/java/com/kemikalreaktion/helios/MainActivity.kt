package com.kemikalreaktion.helios

import android.Manifest.permission.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.content.pm.PackageManager
import android.widget.ImageView
import kotlinx.coroutines.runBlocking

private const val REQUEST_CODE_PERMISSIONS = 0
// TODO: figure out a better way to track the current wallpaper being changed
private const val REQUEST_CODE_CHOOSE_IMAGE_DAY = 1
private const val REQUEST_CODE_CHOOSE_IMAGE_NIGHT = 2
private const val REQUEST_CODE_CROP_IMAGE_DAY = 3
private const val REQUEST_CODE_CROP_IMAGE_NIGHT = 4

const val ACTION_APPLY_WALLPAPER = "com.kemikalreaktion.helios.ACTION_APPLY_WALLPAPER"
const val EXTRA_PAPER_TIME = "com.kemikalreaktion.helios.EXTRA_PAPER_TIME"

private val REQUIRED_PERMISSIONS = arrayOf(
    READ_EXTERNAL_STORAGE,
    ACCESS_COARSE_LOCATION,
    ACCESS_FINE_LOCATION
)

class MainActivity : AppCompatActivity() {
    private lateinit var mWallpaperHelper: WallpaperHelper
    private lateinit var mLocationHelper: LocationHelper
    private var hasPermissions = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mWallpaperHelper = WallpaperHelper(this)
        mLocationHelper = LocationHelper(this)

        // load last saved wallpaper
        val viewDay = findViewById<ImageView>(R.id.preview_day)
        val viewNight = findViewById<ImageView>(R.id.preview_night)
        runBlocking {
            val bmpDay = mWallpaperHelper.getSavedWallpaperAsync(PaperTime.DAY).await()
            val bmpNight = mWallpaperHelper.getSavedWallpaperAsync(PaperTime.NIGHT).await()
            bmpDay?.let {
                viewDay.setImageBitmap(bmpDay)
            }
            bmpNight?.let {
                viewNight.setImageBitmap(bmpNight)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // TODO: proper permissions handling
        // For now, this "just works"
        for (permission in REQUIRED_PERMISSIONS) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                hasPermissions = false
            }
        }
        if (!hasPermissions) requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    override fun onResume() {
        super.onResume()
        mLocationHelper.getLocation()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when(requestCode) {
            REQUEST_CODE_PERMISSIONS -> {
                if (resultCode == RESULT_OK) {
                    hasPermissions = true;
                }
            }
            REQUEST_CODE_CHOOSE_IMAGE_DAY -> {
                if (resultCode == RESULT_OK) {
                    val cropIntent = mWallpaperHelper.getCropIntent(intent?.data!!)
                    cropIntent?.let { startActivityForResult(cropIntent, REQUEST_CODE_CROP_IMAGE_DAY) }
                }
            }
            REQUEST_CODE_CHOOSE_IMAGE_NIGHT -> {
                if (resultCode == RESULT_OK) {
                    val cropIntent = mWallpaperHelper.getCropIntent(intent?.data!!)
                    cropIntent?.let { startActivityForResult(cropIntent, REQUEST_CODE_CROP_IMAGE_NIGHT) }
                }
            }
            REQUEST_CODE_CROP_IMAGE_DAY -> {
                if (resultCode == RESULT_OK && hasPermissions) {
                    val view = findViewById<ImageView>(R.id.preview_day)
                    view.setImageBitmap(mWallpaperHelper.updateWallpaper(PaperTime.DAY))
                    mWallpaperHelper.reset()
                }
            }
            REQUEST_CODE_CROP_IMAGE_NIGHT -> {
                if (resultCode == RESULT_OK && hasPermissions) {
                    val view = findViewById<ImageView>(R.id.preview_night)
                    view.setImageBitmap(mWallpaperHelper.updateWallpaper(PaperTime.NIGHT))
                    mWallpaperHelper.reset()
                }
            }
        }
    }

    fun onApplyClicked(view: View) {
        if (mWallpaperHelper.apply()) {
            Toast.makeText(this, "Wallpaper applied!", LENGTH_SHORT).show()
        }
    }

    fun onResetClicked(view: View) {
        if (mWallpaperHelper.reset()) {
            Toast.makeText(this, "Wallpaper reset!", LENGTH_SHORT).show()
        }
    }

    fun onChooseClicked(view: View) {
        when(view.id) {
            R.id.button_choose_day -> {
                val intent = Intent(ACTION_PICK)
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE_DAY)
            }
            R.id.button_choose_night -> {
                val intent = Intent(ACTION_PICK)
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE_NIGHT)
            }
        }
    }

    fun onTestClicked(view: View) {
        when(view.id) {
            R.id.button_test_day -> {
                val intent = Intent(this, WallpaperBroadcastReceiver::class.java).let {
                    intent -> intent.action = ACTION_APPLY_WALLPAPER
                    intent.putExtra(EXTRA_PAPER_TIME, PaperTime.DAY)
                }
                sendBroadcast(intent)
            }
            R.id.button_test_night -> {
                val intent = Intent(this, WallpaperBroadcastReceiver::class.java).let {
                        intent -> intent.action = ACTION_APPLY_WALLPAPER
                    intent.putExtra(EXTRA_PAPER_TIME, PaperTime.NIGHT)
                }
                sendBroadcast(intent)
            }
        }
    }
}
