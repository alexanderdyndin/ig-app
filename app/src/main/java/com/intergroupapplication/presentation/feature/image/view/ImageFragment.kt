package com.intergroupapplication.presentation.feature.image.view

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.isVisible
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.image.adapter.ImageAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_image.*
import javax.inject.Inject

class ImageFragment(): BaseFragment() {

    override fun layoutRes() = R.layout.fragment_image

    override fun getSnackBarCoordinator(): ViewGroup? = navigationCoordinator

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val images = arguments?.getParcelableArray("images")!!
        val currentItem = arguments?.getInt("selectedId") ?: 0
        ImageAdapter.imageClickListener = {
            if (toolbarUpstairs.isVisible()) {
                toolbarUpstairs.hide()
                bottomLayout.hide()
            } else {
                toolbarUpstairs.show()
                bottomLayout.show()
            }
        }
        pagerImage.apply {
            adapter = ImageAdapter(images.map { it as FileEntity }, imageLoadingDelegate)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setCurrentItem(currentItem, false)
            setOnClickListener {
                if (toolbarUpstairs.isVisible()) {
                toolbarUpstairs.hide()
                bottomLayout.hide()
                } else {
                    toolbarUpstairs.show()
                    bottomLayout.show()
                }
            }
        }
        TabLayoutMediator(tabDots, pagerImage) { _, _ -> }.attach()
        toolbarUpstairs.setNavigationIcon(R.drawable.ic_arrow_gray)
        toolbarUpstairs.setNavigationOnClickListener { findNavController().popBackStack() }
    }

}