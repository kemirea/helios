package com.kemikalreaktion.helios

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * PagerAdapter for displaying Papers
 */
class PaperPagerAdapter(fragmentManager: FragmentManager, private val paperViewModel: PaperViewModel)
    : FragmentPagerAdapter(fragmentManager) {

    // Returns total number of pages
    override fun getCount(): Int {
        return paperViewModel.allPaper.value?.size ?: 0
    }

    // Returns the fragment to display for that page
    override fun getItem(position: Int): Fragment {
        val fragment = PaperViewFragment()
        fragment.setPaper(paperViewModel.allPaper.value?.get(position))
        return fragment
    }

    // Returns the page title for the top indicator
    override fun getPageTitle(position: Int): CharSequence? {
        return "Paper $position"
    }
}