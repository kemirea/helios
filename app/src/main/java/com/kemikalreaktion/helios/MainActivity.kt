package com.kemikalreaktion.helios

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import com.kemikalreaktion.helios.data.Paper

class MainActivity : AppCompatActivity(), OnBindCallback {
    private lateinit var paperViewModel: PaperViewModel
    private lateinit var snapHelper: PositionalSnapHelper
    private var hasPermissions = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val factory = PaperViewModelFactory(this)
        paperViewModel = ViewModelProviders.of(this, factory).get(PaperViewModel::class.java)

        val paperListAdapter = PaperListAdapter(paperViewModel)
        paperListAdapter.callback = this
        val viewManager = LinearLayoutManager(this, HORIZONTAL, false)
        val recyclerView = findViewById<RecyclerView>(R.id.viewpager).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = paperListAdapter
        }
        snapHelper = PositionalSnapHelper(recyclerView)
        snapHelper.attachToRecyclerView(recyclerView)
        paperViewModel.allPaper.observe(this, androidx.lifecycle.Observer {
            paperListAdapter.notifyDataSetChanged()
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

    override fun onViewHolderBound(viewHolder: PaperListAdapter.PaperViewHolder, position: Int) {
        viewHolder.chooseButton.setOnClickListener { onButtonClicked(viewHolder.chooseButton) }
        viewHolder.deleteButton.setOnClickListener { onButtonClicked(viewHolder.deleteButton) }
    }

    fun onButtonClicked(view: View) {
        when(view.id) {
            R.id.button_choose -> {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE)
            }
            R.id.button_delete -> {
                paperViewModel.deletePaper(getCurrentPaper())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when(requestCode) {
            REQUEST_CODE_CHOOSE_IMAGE -> {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    paperViewModel.getCropIntent(intent?.data!!)?.let { cropIntent ->
                        startActivityForResult(cropIntent, REQUEST_CODE_CROP_IMAGE)
                    }
                }
            }
            REQUEST_CODE_CROP_IMAGE -> {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    val img = paperViewModel.addOrUpdatePaper(getCurrentPaper())
                    snapHelper.getCurrentView()?.findViewById<ImageView>(R.id.wallpaper)?.setImageBitmap(img)
                }
            }
        }
    }

    private fun getCurrentPaper(): Paper {
        val position = snapHelper.getPosition()
        paperViewModel.allPaper.value?.let { list ->
            if (position < list.size) {
                return list[position]
            }
        }
        return Paper(position)
    }

    // TODO: mode this logic into PaperViewModel
    fun onApplyClicked(view: View) {
        paperViewModel.apply()
    }
}
