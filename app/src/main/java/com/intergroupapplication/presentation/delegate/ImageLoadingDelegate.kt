package com.intergroupapplication.presentation.delegate

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.widget.ImageView
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.BaseDataSubscriber
import com.facebook.datasource.DataSource
import com.facebook.datasource.DataSources
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.Priority
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableBitmap
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
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

//    fun loadBitmapFromUrl(url: String): Bitmap? {
//        val uri = Uri.parse(url)
//        val pipeline = Fresco.getImagePipeline()
//        val request = ImageRequestBuilder
//                .newBuilderWithSource(uri)
//                .setRequestPriority(Priority.HIGH)
//                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
//                .build()
//        val ds = pipeline.fetchDecodedImage(request, null)
//        val ref = DataSources.waitForFinalResult(ds)
//        ref?.use { ref ->
//            val image = ref.get()
//            if (image is CloseableBitmap) {
//                return cropBitmap(image.underlyingBitmap)
//            }
//        }
//        return null
//    }
//
//    private fun cropBitmap(src: Bitmap): Bitmap {
//        lateinit var bitmap: Bitmap
//        if (src.width>src.height) {
//            bitmap = Bitmap.createBitmap(src, src.width/2-src.height/2,0, src.height, src.height)
//        } else {
//            bitmap = Bitmap.createBitmap(src, 0, src.height/2-src.width/2, src.width, src.width)
//        }
//        val matrix = Matrix()
//        matrix.postRotate(315f)
//        bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.width, bitmap.height, matrix, true)
//        return Bitmap.createScaledBitmap(bitmap,144,144,true)
//    }
}
