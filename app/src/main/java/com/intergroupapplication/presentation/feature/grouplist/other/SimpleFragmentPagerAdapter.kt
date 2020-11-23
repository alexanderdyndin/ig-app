package com.intergroupapplication.presentation.feature.grouplist.other

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter



class ViewPager2Circular(private val pager: ViewPager2,
                         private val swipeRefreshLayout: SwipeRefreshLayout) : ViewPager2.OnPageChangeCallback() {

    private var mCurrentPosition = 0
    private var mScrollState = 0
    private var isScrolled = false
    private val lastPosition = pager.adapter!!.itemCount - 1

    var pageChanged: (id: Int) -> Unit = {}

    override fun onPageSelected(position: Int) {
        mCurrentPosition = position
        pageChanged.invoke(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
        toggleRefreshing(state == ViewPager2.SCROLL_STATE_IDLE)
        if (isScrolled) {
            isScrolled = false
        } else {
            handleScrollState(state)
        }
        mScrollState = state
    }

    fun toggleRefreshing(enabled: Boolean) {
        swipeRefreshLayout.isEnabled = enabled
    }

    private fun handleScrollState(state: Int) {
        if (state == ViewPager2.SCROLL_STATE_IDLE && mScrollState == ViewPager2.SCROLL_STATE_DRAGGING) {
            isScrolled = true
            if (mCurrentPosition == 0) {
                pager.setCurrentItem(lastPosition, true)
            } else if (mCurrentPosition == lastPosition) {
                pager.setCurrentItem(0, true)
            }
        }
    }
}