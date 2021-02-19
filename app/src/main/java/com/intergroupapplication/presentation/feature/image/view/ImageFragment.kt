package com.intergroupapplication.presentation.feature.image.view

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.feature.image.adapter.imageAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_image.*

class ImageFragment(): BaseFragment() {

    override fun layoutRes() = R.layout.fragment_image

    override fun getSnackBarCoordinator(): ViewGroup? = navigationCoordinator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val images = arguments?.getParcelableArray("images")!!
        val currentItem = arguments?.getInt("selectedId") ?: 0
        pagerImage.apply {
            adapter = imageAdapter(images.map { it as FileEntity })
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setCurrentItem(currentItem, false)
        }
        TabLayoutMediator(tabDots, pagerImage) { _, _ -> }.attach()
    }

}