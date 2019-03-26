package com.kemikalreaktion.helios

import android.Manifest.permission.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import kotlinx.coroutines.runBlocking
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private const val REQUEST_CODE_PERMISSIONS = 0
private const val REQUEST_CODE_CHOOSE_IMAGE = 1
private const val REQUEST_CODE_CROP_IMAGE = 2

private val REQUIRED_PERMISSIONS = arrayOf(
    // required to grab current background
    READ_EXTERNAL_STORAGE,
    // required for sunrise/sunset time
    ACCESS_COARSE_LOCATION,
    ACCESS_FINE_LOCATION
)

class MainActivity : AppCompatActivity() {
    private lateinit var paperViewModel: PaperViewModel
    private var hasPermissions = true

    private lateinit var mPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        paperViewModel = PaperViewModel(application)

        mPager = findViewById(R.id.viewpager)
        val pagerAdapter = PaperPagerAdapter(supportFragmentManager, paperViewModel)
        mPager.adapter = pagerAdapter
        paperViewModel.allPaper.observe(this, androidx.lifecycle.Observer {
            pagerAdapter.notifyDataSetChanged()
        })
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

        val df = SimpleDateFormat("HH:mm:ss z", Locale.US)
        val sunrise = paperViewModel.sunCalculator?.getSunrise()?.time
        val sunset = paperViewModel.sunCalculator?.getSunrise()?.time
        findViewById<TextView>(R.id.text_sunrise_time).text =
            getString(R.string.label_sunrise_time, if (sunrise != null) df.format(sunrise) else "")
        findViewById<TextView>(R.id.text_sunset_time).text =
            getString(R.string.label_sunset_time, if (sunset != null) df.format(sunset) else "")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            var allPermissionsGranted = true
            for (result in grantResults) {
                if (result == PERMISSION_DENIED) allPermissionsGranted = false
            }

            if (allPermissionsGranted) {
                // reinit PaperViewModel
                paperViewModel = PaperViewModel(application)
                hasPermissions = true
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when(requestCode) {
            REQUEST_CODE_CHOOSE_IMAGE -> {
                if (resultCode == RESULT_OK) {
                    val cropIntent = paperViewModel.getCropIntent(intent?.data!!)
                    cropIntent?.let { startActivityForResult(cropIntent, REQUEST_CODE_CROP_IMAGE) }
                }
            }
            REQUEST_CODE_CROP_IMAGE -> {
                if (resultCode == RESULT_OK && hasPermissions) {
                    // TODO: handle adding new Paper
                }
            }
        }
    }

    fun onChooseClicked(view: View) {
        when(view.id) {
            R.id.button_choose -> {
                val intent = Intent(ACTION_PICK)
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE)
            }
        }
    }

    // TODO: mode this logic into PaperViewModel
    fun onApplyClicked(view: View) {
        paperViewModel.apply()
    }
}
