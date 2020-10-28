package com.intergroupapplication.presentation.base

import android.widget.ImageView
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.presentation.customview.ShaderSimpleDraweeView

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
interface ImageLoader {
    fun loadImageFromFile(filePath: String, target: SimpleDraweeView)
    fun loadImageFromResources(resId: Int, target: SimpleDraweeView)
    fun loadImageFromUrl(url: String, target: SimpleDraweeView)
}
