package com.intergroupapplication.presentation.delegate

import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.presentation.base.ImageLoader
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
class ImageLoadingDelegate @Inject constructor(private val imageLoader: ImageLoader) {

    fun loadImageFromFile(filePath: String, target: SimpleDraweeView) {
        imageLoader.loadImageFromFile(filePath, target)
    }

    fun loadImageFromResources(resId: Int, target: SimpleDraweeView) {
        imageLoader.loadImageFromResources(resId, target)
    }

    fun loadImageFromUrl(url: String, target: SimpleDraweeView) {
        imageLoader.loadImageFromUrl(url, target)
    }
}
