package com.kemikalreaktion.helios

import android.Manifest.permission.SET_WALLPAPER
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

const val SET_WALLPAPER_PERMISSION_REQUEST = 0
class MainActivity : AppCompatActivity() {
    private lateinit var mWallpaperHelper : WallpaperHelper
    private var hasPermissions = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mWallpaperHelper = WallpaperHelper(this)
    }

    override fun onStart() {
        super.onStart()
        if (checkSelfPermission(SET_WALLPAPER) != PackageManager.PERMISSION_GRANTED) {
            // Request permission here
            /*if (shouldShowRequestPermissionRationale(SET_WALLPAPER)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {*/
            // No explanation needed, we can request the permission.
            requestPermissions(arrayOf(SET_WALLPAPER), SET_WALLPAPER_PERMISSION_REQUEST)

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            //}
        } else {
            hasPermissions = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasPermissions) {
            mWallpaperHelper.setWallpaper();
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            SET_WALLPAPER_PERMISSION_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    hasPermissions = true;
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }
    }
}
