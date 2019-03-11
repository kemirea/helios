package com.kemikalreaktion.helios

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import android.content.Intent
import android.content.Intent.ACTION_PICK


private const val REQUEST_CODE_CHOOSE_IMAGE = 0;
private const val REQUEST_CODE_CROP_IMAGE = 1;

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == REQUEST_CODE_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri = intent!!.data
            /*val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            mWallpaperHelper.set(bitmap)*/


            val cropIntent = mWallpaperHelper.getCropIntent(imageUri!!)
            startActivityForResult(cropIntent!!, REQUEST_CODE_CROP_IMAGE);
        } else if (requestCode == REQUEST_CODE_CROP_IMAGE && resultCode == Activity.RESULT_OK) {

        }
    }

    fun onApplyClicked(view: View) {
        mWallpaperHelper.set();
        Toast.makeText(this, "Wallpaper applied!", LENGTH_SHORT).show()
    }

    fun onRevertClicked(view: View) {
        mWallpaperHelper.revert()
        Toast.makeText(this, "Wallpaper reverted!", LENGTH_SHORT).show()
    }

    fun onChooseClicked(view: View) {
        val intent = Intent(ACTION_PICK)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE);
    }
}
