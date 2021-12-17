package com.intergroupapplication.presentation.customview

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity

class ImageGalleryView @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null, private val defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr) {

    var imageClick: (List<FileEntity>, Int) -> Unit = { _: List<FileEntity>, _: Int -> }
    var expand: (isExpanded: Boolean) -> Unit = {}

    private val pxWidth by lazy(LazyThreadSafetyMode.NONE) { layoutParams.width }

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
    }

    private var uris: List<FileEntity> = emptyList()
    private var allUrls: List<FileEntity> = emptyList()
    private var isExpanded: Boolean = false

    fun setImages(uris: List<FileEntity>, isExpanded: Boolean = false, allUrls: List<FileEntity>) {
        this.uris = uris
        this.isExpanded = isExpanded
        this.allUrls = allUrls
        parseUrl(uris, isExpanded)
    }

    private fun parseUrl(urls: List<FileEntity>, isExpanded: Boolean) {
        this.removeAllViews()
        if (isExpanded && urls.size > 3) {
            for (i in 0 until urls.size / 3) {
                createContainer(urls.subList(i * 3, i * 3 + 3))
            }
            when (urls.size % 3) {
                2 -> createContainer(urls.subList(urls.size - 2, urls.size))
                1 -> createContainer(urls.subList(urls.size - 1, urls.size))
            }
            val hider = LayoutInflater.from(context).inflate(R.layout.layout_hide, this, false)
            val btnHide = hider.findViewById<FrameLayout>(R.id.btnHide)
            btnHide.setOnClickListener {
                this.isExpanded = false
                parseUrl(uris, this.isExpanded)
                expand.invoke(this.isExpanded)
            }
            this.addView(hider)
        } else if (!isExpanded && urls.size > 3) {
            createContainer(urls.subList(0, 3))
            val expander = LayoutInflater.from(context).inflate(R.layout.layout_expand, this, false)
            val btnExpand = expander.findViewById<FrameLayout>(R.id.btnExpand)
            btnExpand.setOnClickListener {
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
        val container = LinearLayout(context, attrs, defStyleAttr)
        container.orientation = HORIZONTAL
        container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        urls.forEach {
            container.addView(createPic(it, pxWidth / urls.size))
        }
        when (urls.size) {
            1 -> container.addView(createPic(urls[0], pxWidth))
            2 -> container.addView(createTwoPic(urls[0], urls[1]))
            3 -> container.addView(createThreePic(urls[0], urls[1], urls[2]))
        }
        this.addView(container)
    }

    private fun createPic(img: FileEntity, width: Int): View {
        val image = LayoutInflater.from(context).inflate(R.layout.layout_pic, this, false)
        image.layoutParams = LayoutParams(width, LayoutParams.WRAP_CONTENT)
        val request: ImageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(img.file))
            .setResizeOptions(ResizeOptions(500, 500))
            .build()
        val pic = image.findViewById<SimpleDraweeView>(R.id.pic)
        pic.controller = Fresco.newDraweeControllerBuilder()
            .setAutoPlayAnimations(true)
            .setOldController(pic.controller)
            .setImageRequest(request)
            .build()
        pic.setOnClickListener { imageClick.invoke(allUrls, allUrls.indexOf(img)) }
        return image
    }

    private fun createTwoPic(firstImage: FileEntity, secondImage: FileEntity): View {
        val image = LayoutInflater.from(context).inflate(R.layout.layout_2pic, this, false)
        image.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        val request1: ImageRequest =
            ImageRequestBuilder.newBuilderWithSource(Uri.parse(firstImage.file))
                .setResizeOptions(ResizeOptions(500, 500))
                .build()
        val request2: ImageRequest =
            ImageRequestBuilder.newBuilderWithSource(Uri.parse(secondImage.file))
                .setResizeOptions(ResizeOptions(500, 500))
                .build()
        image.findViewById<SimpleDraweeView>(R.id.pic1).apply {
            controller = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setOldController(controller)
                .setImageRequest(request1)
                .build()
            setOnClickListener { imageClick.invoke(allUrls, allUrls.indexOf(firstImage)) }
        }
        image.findViewById<SimpleDraweeView>(R.id.pic2).apply {
            controller = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setOldController(controller)
                .setImageRequest(request2)
                .build()
            setOnClickListener { imageClick.invoke(allUrls, allUrls.indexOf(secondImage)) }
        }
        return image
    }

    private fun createThreePic(
        firstImage: FileEntity,
        secondImage: FileEntity,
        thirdImage: FileEntity
    ): View {
        val image = LayoutInflater.from(context).inflate(R.layout.layout_3pic, this, false)
        image.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        val request1: ImageRequest =
            ImageRequestBuilder.newBuilderWithSource(Uri.parse(firstImage.file))
                .setResizeOptions(ResizeOptions(500, 500))
                .build()
        val request2: ImageRequest =
            ImageRequestBuilder.newBuilderWithSource(Uri.parse(secondImage.file))
                .setResizeOptions(ResizeOptions(500, 500))
                .build()
        val request3: ImageRequest =
            ImageRequestBuilder.newBuilderWithSource(Uri.parse(thirdImage.file))
                .setResizeOptions(ResizeOptions(500, 500))
                .build()
        image.findViewById<SimpleDraweeView>(R.id.pic11).apply {
            controller = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setOldController(controller)
                .setImageRequest(request1)
                .build()
            setOnClickListener { imageClick.invoke(allUrls, allUrls.indexOf(firstImage)) }
        }
        image.findViewById<SimpleDraweeView>(R.id.pic22).apply {
            controller = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setOldController(controller)
                .setImageRequest(request2)
                .build()
            setOnClickListener { imageClick.invoke(allUrls, allUrls.indexOf(secondImage)) }
        }
        image.findViewById<SimpleDraweeView>(R.id.pic33).apply {
            controller = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setOldController(controller)
                .setImageRequest(request3)
                .build()
            setOnClickListener { imageClick.invoke(allUrls, allUrls.indexOf(thirdImage)) }
        }
        return image
    }
}
