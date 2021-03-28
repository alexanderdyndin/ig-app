package com.intergroupapplication.presentation.customview

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.*
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ImageDecodeOptionsBuilder
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.github.florent37.shapeofview.shapes.CutCornerView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
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
        pxWidth = displayMetrics.widthPixels - 30
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
        val container = LinearLayout(context, attrs, defStyleAttr)
        container.orientation = LinearLayout.HORIZONTAL
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
        image.pic.controller = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setOldController(image.pic.controller)
                .setImageRequest(request)
                .build()
        image.pic.setOnClickListener { imageClick.invoke(uris, uris.indexOf(img)) }
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

    private fun dpToPx(dp: Int) = (dp * context.resources.displayMetrics.density).roundToInt()


}