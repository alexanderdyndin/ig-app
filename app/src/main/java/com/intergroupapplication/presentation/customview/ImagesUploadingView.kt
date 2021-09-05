package com.intergroupapplication.presentation.customview

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import com.budiyev.android.circularprogressbar.CircularProgressBar
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.presentation.base.ImageUploadingView
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
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
            val imagePreview = it.findViewById<SimpleDraweeView>(R.id.imagePreview)
            imagePreview.controller = Fresco.newDraweeControllerBuilder()
                    .setAutoPlayAnimations(true)
                    .setOldController(imagePreview.controller)
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
        container.orientation = HORIZONTAL
        container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        views.forEach {
            val this_container = it?.findViewById<LinearLayout>(R.id.container)
            this_container?.layoutParams = LayoutParams(pxWidth/views.size, pxWidth/views.size)
            container.addView(it)
        }
        this.addView(container)
    }

    override fun showImageUploaded(chooseMedia: ChooseMedia) {
        loadingViews[chooseMedia.url]?.apply {
            val darkCard = findViewById<TextView>(R.id.darkCard)
            val stopUploading = findViewById<ImageView>(R.id.stopUploading)
            val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            val detachImage = findViewById<ImageView>(R.id.detachImage)
            darkCard.hide()
            stopUploading.hide()
            imageUploadingProgressBar.hide()
            detachImage.show()
        }
    }


    override fun showImageUploadingProgress(progress: Float, chooseMedia: ChooseMedia) {
        loadingViews[chooseMedia.url]?.apply {
            val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            imageUploadingProgressBar.progress = progress
        }
    }


    override fun showImageUploadingError(chooseMedia: ChooseMedia) {
        loadingViews[chooseMedia.url]?.apply {
            val darkCard = findViewById<TextView>(R.id.darkCard)
            val stopUploading = findViewById<ImageView>(R.id.stopUploading)
            val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            val detachImage = findViewById<ImageView>(R.id.detachImage)
            val refreshContainer = findViewById<LinearLayout>(R.id.refreshContainer)
            darkCard.show()
            detachImage.show()
            refreshContainer.show()
            imageUploadingProgressBar.hide()
            stopUploading.hide()
        }
    }

    private fun prepareListeners(uploadingView: View?, path: String) {
        uploadingView?.apply {
            val stopUploading = findViewById<ImageView>(R.id.stopUploading)
            val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            val detachImage = findViewById<ImageView>(R.id.detachImage)
            val refreshContainer = findViewById<LinearLayout>(R.id.refreshContainer)
            refreshContainer.setOnClickListener {
                imageUploadingProgressBar.progress = 0f
                retryListener.invoke(path)
                imageUploadingStarted(uploadingView)
            }
            stopUploading.setOnClickListener {
                cancelListener.invoke(path)
                detachImage(path)
            }
            detachImage.setOnClickListener {
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
            val darkCard = findViewById<TextView>(R.id.darkCard)
            val stopUploading = findViewById<ImageView>(R.id.stopUploading)
            val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            val detachImage = findViewById<ImageView>(R.id.detachImage)
            val refreshContainer = findViewById<LinearLayout>(R.id.refreshContainer)
            darkCard.show()
            imageUploadingProgressBar.show()
            stopUploading.show()
            detachImage.hide()
            refreshContainer.hide()
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
