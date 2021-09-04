package com.intergroupapplication.presentation.customview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.budiyev.android.circularprogressbar.CircularProgressBar
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.presentation.base.ImageUploadingState
import com.intergroupapplication.presentation.base.ImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*

class AvatarImageUploadingView : FrameLayout, ImageUploadingView {

    enum class AvatarUploadingState {
        UPLOADING, UPLOADED, ERROR, NONE
    }

    var imageLoaderDelegate: ImageLoadingDelegate? = null
    var state = AvatarUploadingState.NONE
        private set

    private val avatar by lazy <SimpleDraweeView> { findViewById(R.id.avatar) }

    var imageState: ImageUploadingState? = null
        set(value) {
            field = value
            value?.let {
                when (it) {
                    is ImageUploadingState.ImageUploadingStarted -> {
                        if (it.path.isNotEmpty())
                            showImageUploadingStarted(it.path)
                        else
                            showImageUploadingStartedWithoutFile()
                    }
                    is ImageUploadingState.ImageUploadingError -> {
                        clearUploadingState()
                        showImageUploadingError(it.path)
                    }
                    is ImageUploadingState.ImageUploaded -> {
                        showAvatar(it.path)
                        showImageUploaded(it.path)
                    }
                    is ImageUploadingState.ImageUploadingProgress ->
                        showImageUploadingProgress(it.progress, it.path)
                }
            }

        }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        inflate(context, R.layout.layout_avatar, this)
    }

    fun showAvatar(url: String) {
        imageLoaderDelegate?.loadImageFromUrl(url, avatar)
    }

    fun showAvatar(res: Int) {
        imageLoaderDelegate?.loadImageFromResources(res, avatar)
    }

    fun clearUploadingState() {
        val errorView = findViewById<TextView>(R.id.errorView)
        val darkCard = findViewById<TextView>(R.id.darkCard)
        val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
        errorView.hide()
        darkCard.hide()
        imageUploadingProgressBar.hide()
        imageUploadingProgressBar.progress = 0f
        state = AvatarUploadingState.NONE
    }

    fun showImageUploadingStartedWithoutFile() {
        val errorView = findViewById<TextView>(R.id.errorView)
        val darkCard = findViewById<TextView>(R.id.darkCard)
        val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
        errorView.hide()
        darkCard.show()
        imageUploadingProgressBar.show()
        imageUploadingProgressBar.progress = 0f
        state = AvatarUploadingState.UPLOADING
    }

    fun clearUploadingState(lastAvatar: String?) {
        val errorView = findViewById<TextView>(R.id.errorView)
        val darkCard = findViewById<TextView>(R.id.darkCard)
        val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
        errorView.hide()
        darkCard.hide()
        imageUploadingProgressBar.hide()
        imageUploadingProgressBar.progress = 0f
      /*  lastAvatar.apply {
            ifNull { showAvatar(R.drawable.application_logo) }
            ifNotNull { showAvatar(it) }
        }*/
        doOrIfNull(lastAvatar, { showAvatar(it) }, { showAvatar(R.drawable.variant_10) })
        state = AvatarUploadingState.NONE
    }

    override fun showImageUploadingStarted(chooseMedia: ChooseMedia) {
        val errorView = findViewById<TextView>(R.id.errorView)
        val darkCard = findViewById<TextView>(R.id.darkCard)
        val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
        errorView.hide()
        darkCard.show()
        imageUploadingProgressBar.show()
        imageLoaderDelegate?.loadImageFromFile(chooseMedia.url, avatar)
        imageUploadingProgressBar.progress = 0f
        state = AvatarUploadingState.UPLOADING
    }

    fun showImageUploadingStarted(path: String) {
        val errorView = findViewById<TextView>(R.id.errorView)
        val darkCard = findViewById<TextView>(R.id.darkCard)
        val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
        errorView.hide()
        darkCard.show()
        imageUploadingProgressBar.show()
        imageLoaderDelegate?.loadImageFromFile(path, avatar)
        imageUploadingProgressBar.progress = 0f
        state = AvatarUploadingState.UPLOADING
    }

    override fun showImageUploaded(chooseMedia: ChooseMedia) {
        val darkCard = findViewById<TextView>(R.id.darkCard)
        val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
        darkCard.hide()
        imageUploadingProgressBar.hide()
        imageUploadingProgressBar.progress = 0f
        state = AvatarUploadingState.UPLOADED
    }


    override fun showImageUploadingProgress(progress: Float, chooseMedia: ChooseMedia) {
        val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
        imageUploadingProgressBar.progress = progress
    }


    override fun showImageUploadingError(chooseMedia: ChooseMedia) {
        val errorView = findViewById<TextView>(R.id.errorView)
        val darkCard = findViewById<TextView>(R.id.darkCard)
        val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
        darkCard.show()
        errorView.show()
        imageUploadingProgressBar.hide()
        imageUploadingProgressBar.progress = 0f
        state = AvatarUploadingState.ERROR
    }

}
