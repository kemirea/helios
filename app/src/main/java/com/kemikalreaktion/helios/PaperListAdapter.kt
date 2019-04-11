package com.kemikalreaktion.helios

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kemikalreaktion.helios.data.Paper
import java.io.File

/**
 * PagerAdapter for displaying Papers
 */
private const val MINIMUM_PAGE_COUNT = 1

interface OnBindCallback {
    fun onViewHolderBound(viewHolder: PaperListAdapter.PaperViewHolder, position: Int)
}

class PaperListAdapter(private val paperViewModel: PaperViewModel)
    : RecyclerView.Adapter<PaperListAdapter.PaperViewHolder>() {
    var callback: OnBindCallback? = null

    class PaperViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var paper: Paper = Paper(-1)
        val paperView: ImageView = view.findViewById(R.id.wallpaper)
        val spinnerView: Spinner = view.findViewById(R.id.spinner_time)
        val chooseButton: Button = view.findViewById(R.id.button_choose)
        val deleteButton: Button = view.findViewById(R.id.button_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaperViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.paper_view_item, parent, false)

        val set = ConstraintSet()
        set.clone(view as ConstraintLayout)
        set.setDimensionRatio(R.id.wallpaper, Util.getDisplayRatioString(parent.context))
        set.applyTo(view)
        return PaperViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaperViewHolder, position: Int) {
        val count = paperViewModel.allPaper.value?.size
        var paper = Paper(position)
        if (count != null && position < count) {
            paperViewModel.allPaper.value?.get(position)?.let { p ->
                paper = p
            }
        }
        holder.paper = paper
        holder.paperView.setImageBitmap(paperViewModel.getBitmapForPaper(holder.paper))
        holder.spinnerView.setSelection(holder.paper.paperTime.ordinal)
        holder.spinnerView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.v(DEBUG_TAG, "nothing selected in spinner")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                paper.paperTime = PaperTime.values()[position]
                paperViewModel.updatePaper(paper)
            }
        }
        callback?.onViewHolderBound(holder, position)
    }

    override fun getItemCount(): Int {
        paperViewModel.allPaper.value?.size?.let { count ->
            return count + MINIMUM_PAGE_COUNT
        }
        return MINIMUM_PAGE_COUNT
    }
}