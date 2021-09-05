package com.intergroupapplication.presentation.customview

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.children
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity

class ImageGalleryView @JvmOverloads constructor(context: Context,
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
        pxWidth = displayMetrics.widthPixels - 30
    }

    private var uris: List<FileEntity> = emptyList()
    private var allUrls:List<FileEntity> = emptyList()
    private var isExpanded: Boolean = false

    fun setImages(uris: List<FileEntity>, isExpanded: Boolean = false, allUrls:List<FileEntity>) {
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
            when (urls.size % 3){
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
            container.addView(createPic(it, pxWidth/urls.size))
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

    fun destroy() {
        this.children.forEach { container ->
            if (container is ViewGroup) {
                container.children.forEach { cornerView ->
                    if (cornerView is ViewGroup) {
                        cornerView.children.forEach {
                            if (it is SimpleDraweeView)
                                it.controller = null
                        }
                    }
                }
            }
        }
    }
}