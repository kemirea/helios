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
import kotlinx.coroutines.runBlocking
import java.util.*

private const val REQUEST_CODE_PERMISSIONS = 0
// TODO: figure out a better way to track the current wallpaper being changed
private const val REQUEST_CODE_CHOOSE_IMAGE_DAY = 1
private const val REQUEST_CODE_CHOOSE_IMAGE_NIGHT = 2
private const val REQUEST_CODE_CROP_IMAGE_DAY = 3
private const val REQUEST_CODE_CROP_IMAGE_NIGHT = 4

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        paperViewModel = PaperViewModel(application)
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

        // load last saved wallpaper
        runBlocking {
            val bmpDay = paperViewModel.getBitmapForPaperTimeAsync(PaperTime.SUNRISE).await()
            val bmpNight = paperViewModel.getBitmapForPaperTimeAsync(PaperTime.SUNSET).await()
            bmpDay?.let { findViewById<ImageView>(R.id.preview_day).setImageBitmap(bmpDay) }
            bmpNight?.let { findViewById<ImageView>(R.id.preview_night).setImageBitmap(bmpNight) }
        }

        findViewById<TextView>(R.id.text_sunrise_time).text =
            getString(R.string.label_sunrise_time, paperViewModel.sunCalculator?.getSunrise()?.time)
        findViewById<TextView>(R.id.text_sunset_time).text =
            getString(R.string.label_sunset_time, paperViewModel.sunCalculator?.getSunset()?.time)
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
            REQUEST_CODE_CHOOSE_IMAGE_DAY -> {
                if (resultCode == RESULT_OK) {
                    val cropIntent = paperViewModel.getCropIntent(intent?.data!!)
                    cropIntent?.let { startActivityForResult(cropIntent, REQUEST_CODE_CROP_IMAGE_DAY) }
                }
            }
            REQUEST_CODE_CHOOSE_IMAGE_NIGHT -> {
                if (resultCode == RESULT_OK) {
                    val cropIntent = paperViewModel.getCropIntent(intent?.data!!)
                    cropIntent?.let { startActivityForResult(cropIntent, REQUEST_CODE_CROP_IMAGE_NIGHT) }
                }
            }
            REQUEST_CODE_CROP_IMAGE_DAY -> {
                if (resultCode == RESULT_OK && hasPermissions) {
                    val view = findViewById<ImageView>(R.id.preview_day)
                    paperViewModel.addPaperForPaperTime(PaperTime.SUNRISE)?.let { bmp -> view.setImageBitmap(bmp) }
                }
            }
            REQUEST_CODE_CROP_IMAGE_NIGHT -> {
                if (resultCode == RESULT_OK && hasPermissions) {
                    val view = findViewById<ImageView>(R.id.preview_night)
                    paperViewModel.addPaperForPaperTime(PaperTime.SUNSET)?.let { bmp -> view.setImageBitmap(bmp) }
                }
            }
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

    // TODO: mode this logic into PaperViewModel
    fun onApplyClicked(view: View) {
        paperViewModel.apply()
    }

    // TODO: once alarms are confirmed working, we should update this to immediately preview/apply the selected wallpaper
    fun onTestClicked(view: View) {
        when(view.id) {
            R.id.button_test_day -> {
                // Test intent for setting day wallpaper. Intent is sent 5 seconds later.
                val sunriseIntent = paperViewModel.getIntentForTime(PaperTime.SUNRISE).let { intent ->
                    PendingIntent.getBroadcast(this, 0, intent, FLAG_UPDATE_CURRENT) }
                val mAlarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                mAlarmManager.set(AlarmManager.RTC, Calendar.getInstance().timeInMillis+5000, sunriseIntent)
            }
            R.id.button_test_night -> {
                // Test intent for setting night wallpaper. Intent is sent 5 seconds later.
                val sunriseIntent = paperViewModel.getIntentForTime(PaperTime.SUNSET).let { intent ->
                    PendingIntent.getBroadcast(this, 0, intent, FLAG_UPDATE_CURRENT) }
                val mAlarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                mAlarmManager.set(AlarmManager.RTC, Calendar.getInstance().timeInMillis+5000, sunriseIntent)
            }
        }
    }
}
