package com.intergroupapplication.presentation.base

import android.net.Uri
import com.facebook.common.util.UriUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import java.io.File


/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
class FrescoImageLoader : ImageLoader {

    override fun loadImageFromFile(filePath: String, target: SimpleDraweeView) {
        val request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(File(filePath)))
            .build()
        target.setImageRequest(request)
    }

    override fun loadCompressedImageFromFile(filePath: String, target: SimpleDraweeView) {
        val request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(File(filePath)))
            .setResizeOptions(ResizeOptions(80, 80))
            .build()
        target.controller = Fresco.newDraweeControllerBuilder()
            .setOldController(target.controller)
            .setImageRequest(request)
            .build()
    }

    override fun loadImageFromResources(resId: Int, target: SimpleDraweeView) {
        //todo может все таки лучше через контекст, чем через контроллер? Проверить
        val uri = Uri.Builder()
            .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
            .path(resId.toString())
            .build()
        val controller = Fresco.newDraweeControllerBuilder()
            .setUri(uri)
            .build()
        target.controller = controller
    }

    override fun loadImageFromUrl(url: String, target: SimpleDraweeView) {
        val controller = Fresco.newDraweeControllerBuilder()
            .setUri(Uri.parse(url))
            .setAutoPlayAnimations(true)
            .build()
        target.controller = controller
    }
}
