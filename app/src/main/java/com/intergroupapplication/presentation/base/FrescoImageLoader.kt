package com.intergroupapplication.presentation.base

import android.app.Activity
import android.net.Uri
import com.facebook.common.util.UriUtil
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequestBuilder
import java.io.File


/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
class FrescoImageLoader(private val callerContext: Activity) : ImageLoader {

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
