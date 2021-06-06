package com.intergroupapplication.presentation.customview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.intergroupapplication.R
import com.intergroupapplication.presentation.base.ImageUploadingState
import com.intergroupapplication.presentation.base.ImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import kotlinx.android.synthetic.main.layout_avatar.view.*

class AvatarImageUploadingView : FrameLayout, ImageUploadingView {

    enum class AvatarUploadingState {
        UPLOADING, UPLOADED, ERROR, NONE
    }

    var imageLoaderDelegate: ImageLoadingDelegate? = null
    var state = AvatarUploadingState.NONE
        private set

    var imageState: ImageUploadingState? = null
        set(value) {
            field = value
            value?.let {
                when (it) {
                    is ImageUploadingState.ImageUploadingStarted ->
                        showImageUploadingStarted(it.path)
                    is ImageUploadingState.ImageUploadingError ->
                        showImageUploadingStarted(it.path)
                    is ImageUploadingState.ImageUploaded ->
                        showImageUploadingStarted(it.path)
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
        errorView.hide()
        darkCard.hide()
        imageUploadingProgressBar.hide()
        imageUploadingProgressBar.progress = 0f
        state = AvatarUploadingState.NONE
    }

    fun showImageUploadingStartedWithoutFile() {
        errorView.hide()
        darkCard.show()
        imageUploadingProgressBar.show()
        imageUploadingProgressBar.progress = 0f
        state = AvatarUploadingState.UPLOADING
    }

    fun clearUploadingState(lastAvatar: String?) {
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

    override fun showImageUploadingStarted(path: String) {
        errorView.hide()
        darkCard.show()
        imageUploadingProgressBar.show()
        imageLoaderDelegate?.loadImageFromFile(path, avatar)
        imageUploadingProgressBar.progress = 0f
        state = AvatarUploadingState.UPLOADING
    }

    override fun showImageUploaded(path: String) {
        darkCard.hide()
        imageUploadingProgressBar.hide()
        imageUploadingProgressBar.progress = 0f
        state = AvatarUploadingState.UPLOADED
    }


    override fun showImageUploadingProgress(progress: Float, path: String) {
        imageUploadingProgressBar.progress = progress
    }


    override fun showImageUploadingError(path: String) {
        darkCard.show()
        errorView.show()
        imageUploadingProgressBar.hide()
        imageUploadingProgressBar.progress = 0f
        state = AvatarUploadingState.ERROR
    }

}
