package com.intergroupapplication.presentation.customview

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.setPadding
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ImageDecodeOptionsBuilder
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.github.florent37.shapeofview.shapes.CutCornerView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import kotlinx.android.synthetic.main.layout_2pic.view.*
import kotlinx.android.synthetic.main.layout_3pic.view.*
import kotlinx.android.synthetic.main.layout_expand.view.*
import kotlinx.android.synthetic.main.layout_hide.view.*
import kotlinx.android.synthetic.main.layout_pic.view.*
import kotlin.math.exp
import kotlin.math.roundToInt


class PostGalleryView @JvmOverloads constructor(context: Context,
                                                private val attrs: AttributeSet? = null, private val defStyleAttr: Int = 0):
        LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        var pxWidth = 1080
    }

    var imageClick: (List<FileEntity>, Int) -> Unit = { _: List<FileEntity>, _: Int -> }
    var expand: (isExpanded: Boolean) -> Unit = {}

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
        val displayMetrics = resources.displayMetrics
        pxWidth = displayMetrics.widthPixels - 10
    }

    private var uris: List<FileEntity> = emptyList()

    private var isExpanded: Boolean = false

    fun setImages(uris: List<FileEntity>, isExpanded: Boolean = false) {
        this.uris = uris
        this.isExpanded = isExpanded
        parseUrl(uris, isExpanded)
    }

    private fun parseUrl(urls: List<FileEntity>, isExpanded: Boolean) {
        this.removeAllViews()
        if (isExpanded && urls.size > 3) {
            for (i in 0 until urls.size / 3) {
                createContainer(urls.subList(i * 3, i * 3 + 3))
            }
            when (urls.size % 3){
                2 -> createContainer(urls.subList(urls.size - 2, urls.size))
                1 -> createContainer(urls.subList(urls.size - 1, urls.size))
            }
            val hider = LayoutInflater.from(context).inflate(R.layout.layout_hide, this, false)
            hider.btnHide.setOnClickListener {
                this.isExpanded = false
                parseUrl(uris, this.isExpanded)
                expand.invoke(this.isExpanded)
            }
            this.addView(hider)
        } else if (!isExpanded && urls.size > 3) {
            createContainer(urls.subList(0, 3))
            val expander = LayoutInflater.from(context).inflate(R.layout.layout_expand, this, false)
            expander.btnExpand.setOnClickListener {
                this.isExpanded = true
                parseUrl(uris, this.isExpanded)
                expand.invoke(this.isExpanded)
            }
            this.addView(expander)
        } else if (urls.isNotEmpty()) {
            createContainer(urls)
        }
    }

    private fun createContainer(urls: List<FileEntity>) {
        when (urls.size) {
            3 -> {
                val view = LayoutInflater.from(context).inflate(R.layout.layout_3pic, this, false)
                view.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, pxWidth / urls.size)
//                var controller = Fresco.newDraweeControllerBuilder()
//                        .setUri(Uri.parse(urls[0].file))
//                        .setAutoPlayAnimations(true)
//                        .build()
//                view.pic11.controller = controller
//                controller = Fresco.newDraweeControllerBuilder()
//                        .setUri(Uri.parse(urls[1].file))
//                        .setAutoPlayAnimations(true)
//                        .build()
//                view.pic22.controller = controller
//                controller = Fresco.newDraweeControllerBuilder()
//                        .setUri(Uri.parse(urls[2].file))
//                        .setAutoPlayAnimations(true)
//                        .build()
//                view.pic33.controller = controller
                view.pic11.setImageURI(Uri.parse(urls[0].file), context)
                view.pic22.setImageURI(Uri.parse(urls[1].file), context)
                view.pic33.setImageURI(Uri.parse(urls[2].file), context)
                view.pic11.setOnClickListener { imageClick.invoke(uris, uris.indexOf(urls[0])) }
                view.pic22.setOnClickListener { imageClick.invoke(uris, uris.indexOf(urls[1])) }
                view.pic33.setOnClickListener { imageClick.invoke(uris, uris.indexOf(urls[2])) }
                this@PostGalleryView.addView(view)
            }
            2 -> {
//                val container = LinearLayout(context, attrs, defStyleAttr)
//                container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, pxWidth / urls.size)
//                container.orientation = HORIZONTAL
//                container.setPadding(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(2))
//                urls.forEach {
//                    container.addView(createPic(it))
//                }
//                this@PostGalleryView.addView(container)
                val view = LayoutInflater.from(context).inflate(R.layout.layout_2pic, this, false)
                view.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, pxWidth / urls.size)
//                var controller = Fresco.newDraweeControllerBuilder()
//                        .setUri(Uri.parse(urls[0].file))
//                        .setAutoPlayAnimations(true)
//                        .build()
//                view.pic1.controller = controller
//                controller = Fresco.newDraweeControllerBuilder()
//                        .setUri(Uri.parse(urls[1].file))
//                        .setAutoPlayAnimations(true)
//                        .build()
//                view.pic2.controller = controller
                view.pic1.setImageURI(Uri.parse(urls[0].file), context)
                view.pic2.setImageURI(Uri.parse(urls[1].file), context)
                view.pic1.setOnClickListener { imageClick.invoke(uris, uris.indexOf(urls[0])) }
                view.pic2.setOnClickListener { imageClick.invoke(uris, uris.indexOf(urls[1])) }
                this@PostGalleryView.addView(view)
            }
            1 -> { //костыль если фото одно
//            val imageCut = CutCornerView(context, attrs, defStyleAttr)
//            imageCut.bottomLeftCutSizeDp = 8f
//            imageCut.bottomRightCutSizeDp = 8f
//            imageCut.topLeftCutSizeDp = 8f
//            imageCut.topRightCutSizeDp = 8f
//            imageCut.setPadding(1)
//            val image = WrapContentDraweeView(context)
//            image.setImageURI(Uri.parse(urls[0].file), context)
//            image.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
//            imageCut.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
//            image.setOnClickListener { imageClick.invoke(uris, 0) }
//            imageCut.addView(image)
                val view = LayoutInflater.from(context).inflate(R.layout.layout_pic, this, false)
                view.pic.setImageURI(Uri.parse(urls[0].file), context)
                view.pic.setOnClickListener { imageClick.invoke(uris, uris.indexOf(urls[0])) }
                this@PostGalleryView.addView(view)
            }
        }
    }

    private fun createPic(img: FileEntity): View {
        val imageCut = CutCornerView(context, attrs, defStyleAttr)
        imageCut.bottomLeftCutSizeDp = 8f
        imageCut.bottomRightCutSizeDp = 8f
        imageCut.topLeftCutSizeDp = 8f
        imageCut.topRightCutSizeDp = 8f
        imageCut.setPadding(1)
        val image = SimpleDraweeView(context, attrs, defStyleAttr)
        image.aspectRatio = 1f
        //программная установка плейсхолдера вызывает фризы
//        image.hierarchy.setPlaceholderImage(R.drawable.variant_10)
        val controller = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.parse(img.file))
                .setAutoPlayAnimations(true)
                .build()
        image.controller = controller
        image.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        imageCut.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        image.setOnClickListener { imageClick.invoke(uris, uris.indexOf(img)) }
        imageCut.addView(image)
        return imageCut
    }

    fun destroy() {
//        this.children.forEach { container ->
//            if (container is ViewGroup) {
//                container.children.forEach { cornerView ->
//                    if (cornerView is ViewGroup) {
//                        cornerView.children.forEach {
//                            if (it is SimpleDraweeView)
//                                it.controller = null
//                        }
//                    } else if (cornerView is SimpleDraweeView){
//                        cornerView.controller = null
//                    }
//                }
//            }
//        }
    }

    private fun dpToPx(dp: Int) = (dp * context.resources.displayMetrics.density).roundToInt()


}