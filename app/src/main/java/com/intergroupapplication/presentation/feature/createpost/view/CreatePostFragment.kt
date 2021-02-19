package com.intergroupapplication.presentation.feature.createpost.view

import android.net.Uri
import android.view.View
import android.webkit.MimeTypeMap
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.fragment.findNavController
import com.facebook.common.util.UriUtil
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.TEXT
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.isVisible
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.exstension.showKeyboard
import com.intergroupapplication.presentation.feature.createpost.presenter.CreatePostPresenter
import com.intergroupapplication.presentation.feature.group.view.GroupFragment.Companion.FRAGMENT_RESULT
import io.reactivex.exceptions.CompositeException
import kotlinx.android.synthetic.main.fragment_create_post.*
import kotlinx.android.synthetic.main.layout_attach_image.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

class CreatePostFragment : BaseFragment(), CreatePostView {


    @Inject
    @InjectPresenter
    lateinit var presenter: CreatePostPresenter

    @ProvidePresenter
    fun providePresenter(): CreatePostPresenter = presenter

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_create_post

    private val uploadingViews: MutableMap<String, View?> = mutableMapOf()

    override fun getSnackBarCoordinator(): CoordinatorLayout = createPostCoordinator

    private lateinit var groupId: String

    override fun viewCreated() {
        groupId = arguments?.getString(GROUP_ID)!!
        presenter.groupId = groupId
        createAction.setOnClickListener {
            val post = postText.text.toString().trim()
            if (post.isEmpty() && postContainer.childCount == 1) {
                dialogDelegate.showErrorSnackBar(getString(R.string.post_should_contains_text))
            }
            else if (uploadingViews.isNotEmpty()) {
                var isLoading = false
                uploadingViews.forEach { (_, view) ->
                    if (view?.darkCard?.isVisible() != false) isLoading = true
                }
                if (isLoading)
                    dialogDelegate.showErrorSnackBar(getString(R.string.image_still_uploading))
                else
                    presenter.createPostWithImage(postText.text.toString().trim(), groupId)
            }
            else presenter.createPostWithImage(postText.text.toString().trim(), groupId)
        }

        attachPhoto.setOnClickListener {
            dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                    mapOf(R.id.fromCamera to { presenter.attachFromCamera() }, R.id.fromGallery to { presenter.attachFromGallery() }))
        }

        attachVideo.setOnClickListener {
            presenter.attachVideo()
        }

        attachAudio.setOnClickListener {
            presenter.attachAudio()
        }
        exitAction.setOnClickListener { findNavController().popBackStack() }
//        postContainer.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
//            override fun onChildViewRemoved(parent: View?, child: View?) {
//                manageClipVisibility()
//            }
//
//            override fun onChildViewAdded(parent: View?, child: View?) {
//                manageClipVisibility()
//            }
//        })
        postText.showKeyboard()
        setErrorHandler()
    }

    override fun postCreateSuccessfully(postEntity: GroupPostEntity) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(POST_ID, postEntity.id)
        findNavController().popBackStack()
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
        uploadingViews[path] = layoutInflater.inflate(R.layout.layout_attach_image, postContainer, false)
        uploadingViews[path]?.let {
            it.imagePreview?.let { draweeView ->
                val type = MimeTypeMap.getFileExtensionFromUrl(path)
                val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(type) ?: ""
                if (mime in listOf("audio/mpeg", "audio/aac", "audio/wav")) {
                    imageLoadingDelegate.loadImageFromResources(R.drawable.variant_10, draweeView)
                    it.nameView?.text = path.substring(path.lastIndexOf("/") + 1)
                }
                else
                    imageLoadingDelegate.loadImageFromFile(path, draweeView)
            }
        }
        postContainer.addView(uploadingViews[path])
        prepareListeners(uploadingViews[path], path)
        imageUploadingStarted(uploadingViews[path])
    }


    override fun showImageUploaded(path: String) {
        uploadingViews[path]?.apply {
            darkCard?.hide()
            stopUploading?.hide()
            imageUploadingProgressBar?.hide()
            detachImage?.show()
        }
    }


    override fun showImageUploadingProgress(progress: Float, path: String) {
        uploadingViews[path]?.apply {
            imageUploadingProgressBar?.progress = progress
        }
    }


    override fun showImageUploadingError(path: String) {
        uploadingViews[path]?.apply {
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

    private fun detachImage(path: String) {
        postContainer.removeView(uploadingViews[path])
        uploadingViews.remove(path)
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

    private fun prepareListeners(uploadingView: View?, path: String) {
        uploadingView?.apply {
            refreshContainer.setOnClickListener {
                this.imageUploadingProgressBar?.progress = 0f
                presenter.retryLoading(path)
                imageUploadingStarted(uploadingView)
            }
            stopUploading?.setOnClickListener {
                presenter.cancelUploading(path)
                detachImage(path)
            }
            detachImage?.setOnClickListener {
                presenter.removeContent(path)
                detachImage(path)
            }
        }
    }


}
