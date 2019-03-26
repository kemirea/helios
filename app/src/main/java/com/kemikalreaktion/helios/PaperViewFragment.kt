package com.kemikalreaktion.helios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.kemikalreaktion.helios.Util.getBitmapForPaper
import com.kemikalreaktion.helios.data.Paper

class PaperViewFragment() : Fragment() {
    private var paper: Paper? = null

    fun setPaper(paper: Paper?) {
        this.paper = paper
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.paper_view_item, container, false)
        val currentContext = context
        val currentPaper = paper
        currentContext?.let {
            getBitmapForPaper(currentContext, currentPaper)?.let { bitmap ->
                view.findViewById<ImageView>(R.id.wallpaper).setImageBitmap(bitmap)
            }
        }
        currentPaper?.paperTime?.let {
            view.findViewById<Spinner>(R.id.spinner_time).setSelection(currentPaper.paperTime.ordinal)
        }
        return view
    }
}
