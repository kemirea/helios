package com.kemikalreaktion.helios

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.content.pm.PackageManager
import android.widget.ImageView


private const val REQUEST_CODE_PERMISSIONS = 0
private const val REQUEST_CODE_CHOOSE_IMAGE = 1
private const val REQUEST_CODE_CROP_IMAGE = 2

class MainActivity : AppCompatActivity() {
    private lateinit var mWallpaperHelper: WallpaperHelper
    private var hasPermissions = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mWallpaperHelper = WallpaperHelper(this)
    }

    override fun onStart() {
        super.onStart()
        hasPermissions = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (!hasPermissions) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when(requestCode) {
            REQUEST_CODE_PERMISSIONS -> {
                if (resultCode == RESULT_OK) {
                    hasPermissions = true;
                }
            }
            REQUEST_CODE_CHOOSE_IMAGE -> {
                if (resultCode == RESULT_OK) {
                    val cropIntent = mWallpaperHelper.getCropIntent(intent?.data!!)
                    cropIntent?.let { startActivityForResult(cropIntent, REQUEST_CODE_CROP_IMAGE) }
                }
            }
            REQUEST_CODE_CROP_IMAGE -> {
                if (resultCode == RESULT_OK && hasPermissions) {
                    val view = findViewById<ImageView>(R.id.preview_day)
                    view.setImageDrawable(mWallpaperHelper.addWallpaperAndReset())
                }
            }
        }
    }

    fun onApplyClicked(view: View) {
        if (mWallpaperHelper.set()) {
            Toast.makeText(this, "Wallpaper applied!", LENGTH_SHORT).show()
        }
    }

    fun onResetClicked(view: View) {
        if (mWallpaperHelper.reset()) {
            Toast.makeText(this, "Wallpaper reverted!", LENGTH_SHORT).show()
        }
    }

    fun onChooseClicked(view: View) {
        val intent = Intent(ACTION_PICK)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE);
    }
}
