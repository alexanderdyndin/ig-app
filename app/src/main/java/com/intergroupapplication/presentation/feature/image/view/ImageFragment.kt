package com.intergroupapplication.presentation.feature.image.view

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentImageBinding
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.isVisible
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.image.adapter.ImageAdapter
import javax.inject.Inject

class ImageFragment(): BaseFragment() {

    private val viewBinding by viewBinding(FragmentImageBinding::bind)

    override fun layoutRes() = R.layout.fragment_image

    override fun getSnackBarCoordinator(): ViewGroup? = null

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImageAdapter.imageClickListener = {
            if (viewBinding.toolbarUpstairs.isVisible()) {
                viewBinding.toolbarUpstairs.hide()
                viewBinding.bottomLayout.hide()
            } else {
                viewBinding.toolbarUpstairs.show()
                viewBinding.bottomLayout.show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val images = arguments?.getParcelableArray("images")!!
        val currentItem = arguments?.getInt("selectedId") ?: 0
        viewBinding.pagerImage.apply {
            adapter = ImageAdapter(images.map { it as FileEntity }, imageLoadingDelegate)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setCurrentItem(currentItem, false)
            setOnClickListener {
                if (viewBinding.toolbarUpstairs.isVisible()) {
                viewBinding.toolbarUpstairs.hide()
                    viewBinding.bottomLayout.hide()
                } else {
                    viewBinding.toolbarUpstairs.show()
                    viewBinding.bottomLayout.show()
                }
            }
        }
        TabLayoutMediator(viewBinding.tabDots, viewBinding.pagerImage) { _, _ -> }.attach()
        viewBinding.toolbarUpstairs.setNavigationIcon(R.drawable.ic_arrow_gray)
        viewBinding.toolbarUpstairs.setNavigationOnClickListener { findNavController().popBackStack() }
    }

    override fun onDestroy() {
        ImageAdapter.imageClickListener = null
        super.onDestroy()
    }

}