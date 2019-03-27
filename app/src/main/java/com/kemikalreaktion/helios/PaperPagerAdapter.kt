package com.kemikalreaktion.helios

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * PagerAdapter for displaying Papers
 */
private const val MINIMUM_PAGE_COUNT = 1

class PaperPagerAdapter(fragmentManager: FragmentManager, private val paperViewModel: PaperViewModel)
    : FragmentPagerAdapter(fragmentManager) {

    // Returns total number of pages
    override fun getCount(): Int {
        paperViewModel.allPaper.value?.size?.let { count ->
            return count + MINIMUM_PAGE_COUNT
        }
        return MINIMUM_PAGE_COUNT
    }

    // Returns the fragment to display for that page
    override fun getItem(position: Int): Fragment {
        val fragment = PaperViewFragment()
        val count = paperViewModel.allPaper.value?.size
        if (count != null && position < count) {
            fragment.setPaper(paperViewModel.allPaper.value?.get(position))
        }
        return fragment
    }

    // Returns the page title for the top indicator
    override fun getPageTitle(position: Int): CharSequence? {
        return "Paper $position"
    }
}