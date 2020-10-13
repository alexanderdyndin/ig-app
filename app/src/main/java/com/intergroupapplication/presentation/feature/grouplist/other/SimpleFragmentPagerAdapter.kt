package com.intergroupapplication.presentation.feature.grouplist.other

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.view.View
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager


class SampleFragmentPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
    val PAGE_COUNT = 3
    private val tabTitles = arrayOf("Все группы", "Подписки", "Управление")
    var doOnFragmentViewCreated: (View) -> Unit = {}

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> AllGroupsFragment.newInstance(position + 1)
                    .apply { doOnViewCreated = doOnFragmentViewCreated }
            1 -> SubscribedGroupsFragment.newInstance(position + 1)
                    .apply { doOnViewCreated = doOnFragmentViewCreated }
            2 -> AdminGroupsFragment.newInstance(position + 1)
                    .apply { doOnViewCreated = doOnFragmentViewCreated }
            else -> AllGroupsFragment.newInstance((position + 1))
                    .apply { doOnViewCreated = doOnFragmentViewCreated }
            }
        }

    override fun getPageTitle(position: Int): CharSequence? {
        // генерируем заголовок в зависимости от позиции
        return tabTitles[position]
    }

}

class CircularViewPagerHandler(private val mViewPager: ViewPager) : ViewPager.OnPageChangeListener {
    private var mCurrentPosition = 0
    private var mScrollState = 0
    private var isScrolled = false

    var pageChanged: (id: Int) -> Unit = {}

    override fun onPageSelected(position: Int) {
        mCurrentPosition = position
        pageChanged.invoke(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
        handleScrollState(state)
        mScrollState = state
    }

    private fun handleScrollState(state: Int) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            setNextItemIfNeeded()
        }
    }

    private fun setNextItemIfNeeded() {
        if (!isScrollStateSettling) {
            handleSetNextItem()
        }
    }

    private val isScrollStateSettling: Boolean
        get() = mScrollState == ViewPager.SCROLL_STATE_SETTLING

    private fun handleSetNextItem() {
        val lastPosition = mViewPager.adapter!!.count - 1
        if (mCurrentPosition == 0) {
            if (isScrolled) {
                isScrolled = false
                return
            }
            isScrolled = true
            mViewPager.setCurrentItem(lastPosition, true)
        } else if (mCurrentPosition == lastPosition) {
            if (isScrolled) {
                isScrolled = false
                return
            }
            isScrolled = true
            mViewPager.setCurrentItem(0, true)
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
}