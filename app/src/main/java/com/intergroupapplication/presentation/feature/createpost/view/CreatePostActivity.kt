package com.intergroupapplication.presentation.feature.createpost.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.CreateGroupPostEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.TEXT
import com.intergroupapplication.presentation.base.BaseActivity
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.isVisible
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.exstension.showKeyboard
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsActivity.Companion.GROUP_ID_VALUE
import com.intergroupapplication.presentation.feature.createpost.presenter.CreatePostPresenter
import com.intergroupapplication.presentation.feature.navigation.di.NavigationViewModule.Companion.GROUP_ID
import io.reactivex.exceptions.CompositeException
import kotlinx.android.synthetic.main.activity_create_post.*
import kotlinx.android.synthetic.main.layout_attach_image.view.*
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import javax.inject.Inject

class CreatePostActivity : BaseActivity(), CreatePostView {

    companion object {

        fun getIntent(context: Context?, groupId: String) =
                Intent(context, CreatePostActivity::class.java).apply {
                    putExtra(GROUP_ID, groupId)
                }

    }

    @Inject
    @InjectPresenter
    lateinit var presenter: CreatePostPresenter

    @ProvidePresenter
    fun providePresenter(): CreatePostPresenter = presenter

    @Inject
    override lateinit var navigator: SupportAppNavigator

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @LayoutRes
    override fun layoutRes() = R.layout.activity_create_post

    override fun getSnackBarCoordinator(): CoordinatorLayout = createPostCoordinator

    private var uploadingView: View? = null

    override fun viewCreated() {
        createAction.setOnClickListener {
            val post = postText.text.toString().trim()
            if (post.isEmpty() && postContainer.childCount == 1) {
                dialogDelegate.showErrorSnackBar(getString(R.string.post_should_contains_text))
            } else if (uploadingView != null && uploadingView?.darkCard?.isVisible() != false) {
                dialogDelegate.showErrorSnackBar(getString(R.string.image_still_uploading))
            } else {
                if (postContainer.childCount == 1) {
                    presenter.createPost(CreateGroupPostEntity(postText.text.toString().trim(), null),
                            intent.getStringExtra(GROUP_ID)!!)
                } else {
                    presenter.createPostWithImage(postText.text.toString().trim(), intent.getStringExtra(GROUP_ID)!!)
                }
            }
        }

        attachPhoto.setOnClickListener {
            dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                    mapOf(R.id.fromCamera to { presenter.attachFromCamera() }, R.id.fromGallery to { presenter.attachFromGallery() }))
        }
        exitAction.setOnClickListener { onResultCancel() }
        postContainer.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewRemoved(parent: View?, child: View?) {
                manageClipVisibility()
            }

            override fun onChildViewAdded(parent: View?, child: View?) {
                manageClipVisibility()
            }
        })
        postText.showKeyboard()
        setErrorHandler()
    }

    override fun postCreateSuccessfully(postEntity: GroupPostEntity) {
        onResultOk(postEntity.groupInPost.id)
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            createAction.hide()
            createProgress.show()
        } else {
            createProgress.hide()
            createAction.show()
        }
    }

    override fun showImageUploadingStarted(path: String) {
        detachImage()
        uploadingView = layoutInflater.inflate(R.layout.layout_attach_image, postContainer, false)
        uploadingView?.let {
            it.imagePreview?.let { draweeView ->
                imageLoadingDelegate.loadImageFromFile(path, draweeView)
            }
        }
        postContainer.addView(uploadingView)
        prepareListeners(uploadingView)
        imageUploadingStarted(uploadingView)
    }

    override fun showImageUploaded() {
        uploadingView?.apply {
            darkCard?.hide()
            stopUploading?.hide()
            imageUploadingProgressBar?.hide()
            detachImage?.show()
        }
    }

    override fun showImageUploadingProgress(progress: Float) {
        uploadingView?.apply {
            imageUploadingProgressBar?.progress = progress
        }
    }

    override fun showImageUploadingError() {
        uploadingView?.apply {
            darkCard?.show()
            detachImage?.show()
            refreshContainer?.show()
            imageUploadingProgressBar?.hide()
            stopUploading?.hide()
        }
    }

    private fun setErrorHandler() {
        errorHandler.on(CompositeException::class.java) { throwable, _ ->
            run {
                (throwable as? CompositeException)?.exceptions?.forEach { ex ->
                    (ex as? FieldException)?.let {
                        if (it.field == TEXT) {
                            showErrorMessage(it.message.orEmpty())
                        }
                    }
                }
            }
        }
    }

    private fun detachImage() {
        if (postContainer.childCount > 1) {
            postContainer.removeViewAt(1)
        }
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

    private fun prepareListeners(uploadingView: View?) {
        uploadingView?.apply {
            refreshContainer.setOnClickListener {
                this.imageUploadingProgressBar?.progress = 0f
                presenter.retryLoadImage()
                imageUploadingStarted(uploadingView)
            }
            stopUploading?.setOnClickListener {
                presenter.stopImageUploading()
                detachImage()
            }
            detachImage?.setOnClickListener {
                detachImage()
            }
        }
    }

    private fun manageClipVisibility() {
        if (postContainer.childCount > 1) {
            attachPhoto.hide()
        } else {
            attachPhoto.show()
        }
    }

    private fun onResultOk(groupId: String) {
        val newIntent = Intent().apply {
            putExtra(GROUP_ID_VALUE, groupId)
        }
        setResult(Activity.RESULT_OK, newIntent)
        finish()
    }

    private fun onResultCancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

}
