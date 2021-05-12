package com.intergroupapplication.presentation.customview

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.children
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.base.ImageUploadingView
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import kotlinx.android.synthetic.main.layout_attach_image.view.*
import java.io.File

class ImagesUploadingView @JvmOverloads constructor(context: Context,
                                                   private val attrs: AttributeSet? = null, private val defStyleAttr: Int = 0):
        LinearLayout(context, attrs, defStyleAttr), ImageUploadingView {

    companion object {
        var pxWidth = 1080
    }

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
        val displayMetrics = resources.displayMetrics
        ImageGalleryView.pxWidth = displayMetrics.widthPixels - 30
    }

    private val loadingViews: MutableMap<String, View> = mutableMapOf()

    var retryListener:(patch: String)-> Unit = {}
    var cancelListener:(patch: String)-> Unit = {}
    var detachListener:(patch: String)-> Unit = {}

    override fun showImageUploadingStarted(chooseMedia: ChooseMedia) {
        loadingViews[chooseMedia.url] = LayoutInflater.from(context).inflate(R.layout.layout_attach_image, this, false)
        loadingViews[chooseMedia.url]?.let {
            val request: ImageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(File(chooseMedia.url)))
                    .setResizeOptions(ResizeOptions(500, 500))
                    .build()
            it.imagePreview.controller = Fresco.newDraweeControllerBuilder()
                    .setAutoPlayAnimations(true)
                    .setOldController(it.imagePreview.controller)
                    .setImageRequest(request)
                    .build()
        }
        prepareListeners(loadingViews[chooseMedia.url], chooseMedia.url)
        imageUploadingStarted(loadingViews[chooseMedia.url])
        parseViewsList(loadingViews.values.toList())
    }

    private fun parseViewsList(loadingViews: List<View?>) {
        this.removeAllViews()
        if (loadingViews.size > 3) {
            for (i in 0 until loadingViews.size / 3) {
                createContainer(loadingViews.subList(i * 3, i * 3 + 3))
            }
            when (loadingViews.size % 3){
                2 -> createContainer(loadingViews.subList(loadingViews.size - 2, loadingViews.size))
                1 -> createContainer(loadingViews.subList(loadingViews.size - 1, loadingViews.size))
            }
        } else if (loadingViews.isNotEmpty()) {
            createContainer(loadingViews)
        }
    }

    private fun createContainer(views: List<View?>) {
        val container = LinearLayout(context, attrs, defStyleAttr)
        container.orientation = LinearLayout.HORIZONTAL
        container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        views.forEach {
            it?.container?.layoutParams = LayoutParams(pxWidth/views.size, pxWidth/views.size)
            container.addView(it)
        }
        this.addView(container)
    }

    override fun showImageUploaded(path: String) {
        loadingViews[path]?.apply {
            darkCard?.hide()
            stopUploading?.hide()
            imageUploadingProgressBar?.hide()
            detachImage?.show()
        }
    }


    override fun showImageUploadingProgress(progress: Float, path: String) {
        loadingViews[path]?.apply {
            imageUploadingProgressBar?.progress = progress
        }
    }


    override fun showImageUploadingError(path: String) {
        loadingViews[path]?.apply {
            darkCard?.show()
            detachImage?.show()
            refreshContainer?.show()
            imageUploadingProgressBar?.hide()
            stopUploading?.hide()
        }
    }

    private fun prepareListeners(uploadingView: View?, path: String) {
        uploadingView?.apply {
            refreshContainer.setOnClickListener {
                this.imageUploadingProgressBar?.progress = 0f
                retryListener.invoke(path)
                imageUploadingStarted(uploadingView)
            }
            stopUploading?.setOnClickListener {
                cancelListener.invoke(path)
                detachImage(path)
            }
            detachImage?.setOnClickListener {
                detachListener.invoke(path)
                detachImage(path)
            }
        }
    }

    private fun detachImage(path: String) {
        this.children.forEach { viewGroup ->
            if (viewGroup is ViewGroup)
                viewGroup.children.forEach {
                    if (it == loadingViews[path])
                        viewGroup.removeView(it)
                }
        }
        loadingViews.remove(path)
        parseViewsList(loadingViews.values.toList())
    }

    private fun imageUploadingStarted(uploadingView: View?) {
        uploadingView?.apply {
            darkCard?.show()
            imageUploadingProgressBar?.show()
            stopUploading?.show()
            detachImage?.hide()
            refreshContainer?.hide()
        }
    }

    override fun removeAllViews() {
        this.children.forEach {
            if (it is ViewGroup)
                it.removeAllViews()
        }
        super.removeAllViews()
    }


}
