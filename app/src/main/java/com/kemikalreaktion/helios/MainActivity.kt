package com.kemikalreaktion.helios

import android.Manifest.permission.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.util.Log
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var paperViewModel: PaperViewModel
    private var hasPermissions = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val factory = PaperViewModelFactory(this)
        paperViewModel = ViewModelProviders.of(this, factory).get(PaperViewModel::class.java)

        val pagerAdapter = PaperPagerAdapter(supportFragmentManager, paperViewModel)
        findViewById<ViewPager>(R.id.viewpager).adapter = pagerAdapter
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
        val sunset = paperViewModel.sunCalculator?.getSunset()?.time
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

    // TODO: mode this logic into PaperViewModel
    fun onApplyClicked(view: View) {
        paperViewModel.apply()
    }
}
