package com.intergroupapplication.presentation.base

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.facebook.common.util.UriUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.intergroupapplication.presentation.customview.zoomable.ZoomableDraweeView
import java.io.File


/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
class FrescoImageLoader(private val callerContext: Any?) : ImageLoader {

    override fun loadImageFromFile(filePath: String, target: SimpleDraweeView) {
        val request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(File(filePath)))
                .build()
        target.setImageRequest(request)
        //target.setImageURI(Uri.fromFile(File(filePath)), callerContext)
    }

    override fun loadImageFromResources(resId: Int, target: SimpleDraweeView) {
        //todo может все таки лучше через контекст, чем через контроллер? Проверить
        val uri = Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                .path(resId.toString())
                .build()
        val controller = Fresco.newDraweeControllerBuilder()
            .setCallerContext(callerContext)
            .setUri(uri)
            .build()
        target.controller = controller
    }

    override fun loadImageFromUrl(url: String, target: ImageView) {
        when (target) {
            is SimpleDraweeView -> {
                val controller = Fresco.newDraweeControllerBuilder()
                    .setCallerContext(callerContext)
                    .setUri(Uri.parse(url))
                    .setAutoPlayAnimations(true)
                    .build()
                target.controller = controller
            }
            is ZoomableDraweeView -> {
                val controller = Fresco.newDraweeControllerBuilder()
                    .setCallerContext(callerContext)
                    .setUri(Uri.parse(url))
                    .setAutoPlayAnimations(true)
                    .build()
                target.controller = controller
            }
        }
    }

}
