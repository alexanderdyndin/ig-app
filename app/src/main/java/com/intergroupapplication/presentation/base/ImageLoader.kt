package com.intergroupapplication.presentation.base

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.BytesRange
import java.io.File

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
interface ImageLoader {
    fun loadImageFromFile(filePath: String, target: SimpleDraweeView)
    fun loadCompressedImageFromFile(filePath: String, target: SimpleDraweeView)
    fun loadImageFromResources(resId: Int, target: SimpleDraweeView)
    fun loadImageFromUrl(url: String, target: SimpleDraweeView)
}
