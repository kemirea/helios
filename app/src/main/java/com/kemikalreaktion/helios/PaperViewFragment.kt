package com.kemikalreaktion.helios

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.kemikalreaktion.helios.Util.getBitmapForPaper
import com.kemikalreaktion.helios.data.Paper
import android.widget.AdapterView

class PaperViewFragment() : Fragment() {
    private var paper: Paper = Paper(-1)
    private lateinit var paperViewModel: PaperViewModel

    fun setPaper(paper: Paper) {
        this.paper = paper
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        paperViewModel = activity?.run {
            ViewModelProviders.of(this).get(PaperViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.paper_view_item, container, false)
        val currentContext = context
        currentContext?.let {
            getBitmapForPaper(currentContext, paper)?.let { bitmap ->
                view.findViewById<ImageView>(R.id.wallpaper).setImageBitmap(bitmap)
            }
        }
        view.findViewById<Spinner>(R.id.spinner_time).setSelection(paper.paperTime.ordinal)
        view.findViewById<Spinner>(R.id.spinner_time)?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.v(DEBUG_TAG, "nothing selected in spinner")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                paper.paperTime = PaperTime.values()[position]
                paperViewModel.updatePaper(paper)
            }
        }

        view.findViewById<Button>(R.id.button_choose).setOnClickListener { v -> onButtonClicked(v) }
        view.findViewById<Button>(R.id.button_delete).setOnClickListener { v -> onButtonClicked(v) }
        return view
    }

    private fun onButtonClicked(view: View) {
        when(view.id) {
            R.id.button_choose -> {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE)
            }
            R.id.button_delete -> {
                paperViewModel.deletePaper(paper)
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
                    val paper = paperViewModel.addOrUpdatePaper(paper)
                    view?.findViewById<ImageView>(R.id.wallpaper)?.setImageBitmap(paper)
                }
            }
        }
    }
}
