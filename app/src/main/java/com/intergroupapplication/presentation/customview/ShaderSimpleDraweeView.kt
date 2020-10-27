package com.intergroupapplication.presentation.customview

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import com.facebook.drawee.generic.GenericDraweeHierarchy
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ImageDecodeOptions
import com.facebook.imagepipeline.common.RotationOptions
import com.facebook.imagepipeline.request.BasePostprocessor
import com.facebook.imagepipeline.request.ImageRequest.RequestLevel
import com.facebook.imagepipeline.request.ImageRequestBuilder


class ShaderSimpleDraweeView: SimpleDraweeView {
    constructor(context: Context?, hierarchy: GenericDraweeHierarchy?) : super(context, hierarchy)
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)


    override fun setImageURI(uri: Uri?, callerContext: Any?) {
        setImageURIShader(uri, callerContext)
    }

    override fun setImageURI(uriString: String?, callerContext: Any?) {
        setImageURIShader(Uri.parse(uriString), callerContext)
    }


    private fun setImageURIShader(uri: Uri?, callerContext: Any?) {
        var adcb = controllerBuilder
        adcb = adcb.setOldController(controller).setCallerContext(callerContext)

        val imageDecodeOptions = ImageDecodeOptions.newBuilder().build()
        /** Build a custom ImageRequest to modify the Bitmap to be rendered */
        /** Build a custom ImageRequest to modify the Bitmap to be rendered  */
        val imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(uri)
                .setImageDecodeOptions(imageDecodeOptions)
                .setRotationOptions(RotationOptions.autoRotate())
                .setLocalThumbnailPreviewsEnabled(true)
                .setLowestPermittedRequestLevel(RequestLevel.FULL_FETCH)
                .setProgressiveRenderingEnabled(true)
        /** Set Bitmap processor */
        /** Set Bitmap processor  */
        imageRequestBuilder.postprocessor = object : BasePostprocessor() {
            override fun process(bitmap: Bitmap) {
                super.process(bitmap)
//                val canvas = Canvas(bitmap)
//                lateinit var btm: Bitmap
//                btm = bitmap.mask(BitmapFactory.decodeResource(context.resources, R.drawable.ava_g))
//                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
//                if (bitmap.width >= bitmap.height) {
//                    btm = Bitmap.createScaledBitmap(
//                            btm,
//                            bitmap.height,
//                            bitmap.height,
//                            true
//                    )
//                    canvas.drawBitmap(btm,bitmap.width,0f,null)
//                } else {
//                    btm = Bitmap.createScaledBitmap(
//                            btm,
//                            bitmap.width,
//                            bitmap.width,
//                            true
//                    )
//                    canvas.drawBitmap(btm,0f,bitmap.height/4f,null)
//                }




            }
        }

        val imageRequest = imageRequestBuilder.build()
        //noinspection unchecked
        adcb.imageRequest = imageRequest

        val controller: DraweeController = adcb.build()

        setController(controller)
    }

    fun Bitmap.mask(mask:Bitmap):Bitmap{
        val bitmap = Bitmap.createBitmap(
                mask.width,mask.height, Bitmap.Config.ARGB_8888
        )

        // paint to mask
        val paint = Paint().apply {
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }

        Canvas(bitmap).apply {
            // draw source bitmap on canvas
            drawBitmap(this@mask,0f,0f,null)
            // mask bitmap
            drawBitmap(mask,0f,0f,paint)
        }

        return bitmap
    }

}