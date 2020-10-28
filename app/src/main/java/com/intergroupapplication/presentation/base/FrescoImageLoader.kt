package com.intergroupapplication.presentation.base

import android.graphics.drawable.Animatable
import android.net.Uri
import com.facebook.common.util.UriUtil
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequestBuilder
import java.io.File
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.imagepipeline.image.ImageInfo
import com.intergroupapplication.presentation.customview.ShaderSimpleDraweeView
import kotlinx.android.synthetic.main.item_group_post.view.*


/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
class FrescoImageLoader(private val callerContext: BaseActivity) : ImageLoader {

    override fun loadImageFromFile(filePath: String, target: SimpleDraweeView) {
        val request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(File(filePath)))
                .build()
        target.setImageRequest(request)
        //target.setImageURI(Uri.fromFile(File(filePath)), callerContext)
    }

    override fun loadImageFromResources(resId: Int, target: SimpleDraweeView) {
        val uri = Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                .path(resId.toString())
                .build()
        target.setImageURI(uri, callerContext)
    }

    override fun loadImageFromUrl(url: String, target: SimpleDraweeView) {
        target.setImageURI(Uri.parse(url), callerContext)
    }

}
