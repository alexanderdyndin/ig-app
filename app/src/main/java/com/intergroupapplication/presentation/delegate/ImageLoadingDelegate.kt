package com.intergroupapplication.presentation.delegate

import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.presentation.base.ImageLoader
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
class ImageLoadingDelegate @Inject constructor(private val imageLoader: ImageLoader) {

    fun loadCompressedImageFromFile(filePath: String,target: SimpleDraweeView){
        imageLoader.loadCompressedImageFromFile(filePath, target)
    }

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
//                return image.underlyingBitmap
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
