package com.intergroupapplication.presentation.feature.createpost.view

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.CreateGroupPostEntity
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

    private val loadingViews: MutableMap<String, View?> = mutableMapOf()

    override fun getSnackBarCoordinator(): CoordinatorLayout = createPostCoordinator

    private var uploadingView: View? = null

    private lateinit var groupId: String

    override fun viewCreated() {
        groupId = arguments?.getString(GROUP_ID)!!
        presenter.groupId = groupId
        createAction.setOnClickListener {
            val post = postText.text.toString().trim()
            if (post.isEmpty() && postContainer.childCount == 1) {
                dialogDelegate.showErrorSnackBar(getString(R.string.post_should_contains_text))
            }
            else if (loadingViews.isNotEmpty()) {
                var isLoading = false
                loadingViews.forEach { (_, view) ->
                    if (view?.darkCard?.isVisible() != false) isLoading = true
                }
                if (isLoading) dialogDelegate.showErrorSnackBar(getString(R.string.image_still_uploading))
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
        onResultOk(/*postEntity.groupInPost.id*/)
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
        //detachImage()
        loadingViews[path] = layoutInflater.inflate(R.layout.layout_attach_image, postContainer, false)
        loadingViews[path]?.let {
            it.imagePreview?.let { draweeView ->
                imageLoadingDelegate.loadImageFromFile(path, draweeView)
            }
        }
        postContainer.addView(loadingViews[path])
        prepareListeners(loadingViews[path])
        imageUploadingStarted(loadingViews[path])
    }

//    override fun showImageUploaded() {
//        uploadingView?.apply {
//            darkCard?.hide()
//            stopUploading?.hide()
//            imageUploadingProgressBar?.hide()
//            detachImage?.show()
//        }
//    }

    override fun showImageUploaded(path: String) {
        loadingViews[path]?.apply {
            darkCard?.hide()
            stopUploading?.hide()
            imageUploadingProgressBar?.hide()
            detachImage?.show()
        }
    }

//    override fun showImageUploadingProgress(progress: Float) {
//        uploadingView?.apply {
//            imageUploadingProgressBar?.progress = progress
//        }
//    }

    override fun showImageUploadingProgress(progress: Float, path: String) {
        loadingViews[path]?.apply {
            imageUploadingProgressBar?.progress = progress
        }
    }

//    override fun showImageUploadingError() {
//        uploadingView?.apply {
//            darkCard?.show()
//            detachImage?.show()
//            refreshContainer?.show()
//            imageUploadingProgressBar?.hide()
//            stopUploading?.hide()
//        }
//    }

    override fun showImageUploadingError(path: String) {
        loadingViews[path]?.apply {
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
                //presenter.retryLoadImage()
                imageUploadingStarted(uploadingView)
            }
            stopUploading?.setOnClickListener {
                //presenter.stopImageUploading()
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

    private fun onResultOk(/*groupId: String*/) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(FRAGMENT_RESULT, BasePresenter.POST_CREATED)
        //findNavController().previousBackStackEntry?.savedStateHandle?.set(GROUP_ID_VALUE, groupId)
        findNavController().popBackStack()
    }

    private fun onResultCancel() {
        //findNavController().previousBackStackEntry?.savedStateHandle?.set("fragmentResult", Activity.RESULT_CANCELED)
        findNavController().popBackStack()
    }

}
