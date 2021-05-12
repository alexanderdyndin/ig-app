package com.intergroupapplication.presentation.feature.createpost.view

import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.View
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.TEXT
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.customview.AutoCloseBottomSheetBehavior
import com.intergroupapplication.presentation.customview.ImagesUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.chooseMedias
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.removeChooseMedia
import com.intergroupapplication.presentation.feature.createpost.presenter.CreatePostPresenter
import com.intergroupapplication.presentation.feature.editpostbottomsheet.view.EditPostBottomSheetFragment
import com.intergroupapplication.presentation.feature.group.view.GroupFragment
import io.reactivex.exceptions.CompositeException
import kotlinx.android.synthetic.main.creategroup_toolbar_layout.*
import kotlinx.android.synthetic.main.fragment_create_post.*
import kotlinx.android.synthetic.main.fragment_create_post.postContainer
import kotlinx.android.synthetic.main.layout_attach_image.view.*
import kotlinx.android.synthetic.main.layout_attach_image.view.detachImage
import kotlinx.android.synthetic.main.layout_attach_image.view.imageUploadingProgressBar
import kotlinx.android.synthetic.main.layout_attach_image.view.refreshContainer
import kotlinx.android.synthetic.main.layout_attach_image.view.stopUploading
import kotlinx.android.synthetic.main.layout_attach_image.view.darkCard
import kotlinx.android.synthetic.main.layout_audio_in_create_post.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

class CreatePostFragment : BaseFragment(), CreatePostView,EditPostBottomSheetFragment.Callback {

    companion object{
        const val MEDIA_INTERACTION_REQUEST_CODE = "media_interaction_request_code"
        const val METHOD_KEY = "method_key"
        const val CHOOSE_MEDIA_KEY = "choose_media_key"
        const val RETRY_LOADING_METHOD_CODE = 0
        const val CANCEL_LOADING_METHOD_CODE = 1
        const val REMOVE_CONTENT_METHOD_CODE = 2
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: CreatePostPresenter

    @ProvidePresenter
    fun providePresenter(): CreatePostPresenter = presenter

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    private lateinit var bottomSheetBehaviour: AutoCloseBottomSheetBehavior<View>

    private val bottomFragment by lazy { EditPostBottomSheetFragment() }

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_create_post

    private val loadingViews: MutableMap<String, View?> = mutableMapOf()

    override fun getSnackBarCoordinator(): CoordinatorLayout = createPostCoordinator

    private lateinit var uploadingView: ImagesUploadingView

    private lateinit var groupId: String

    private fun convertDpToPixel(dp: Int): Int {
        val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return px.roundToInt()
    }

    override fun viewCreated() {
        groupId = arguments?.getString(GROUP_ID)!!
        presenter.groupId = groupId
        publishBtn.show()
        publishBtn.setOnClickListener {
            val post = postText.text.toString().trim()
            if (post.isEmpty() && postContainer.childCount == 1 && audioContainer.childCount == 1) {
                dialogDelegate.showErrorSnackBar(getString(R.string.post_should_contains_text))
            }
            else if (loadingViews.isNotEmpty()) {
                var isLoading = false
                loadingViews.forEach { (key, view) ->
                    if (view?.darkCard?.isVisible() != false) {
                        isLoading = true
                        Timber.tag("tut_view").d(key)
                    }
                }
                if (isLoading)
                    dialogDelegate.showErrorSnackBar(getString(R.string.image_still_uploading))
                else {
                    presenter.createPostWithImage(postText.text.toString().trim(), groupId,
                            bottomFragment.getPhotosUrl(),bottomFragment.getVideosUrl(),
                            bottomFragment.getAudiosUrl())
                }
            }
            else {
                presenter.createPostWithImage(postText.text.toString().trim(), groupId,
                        bottomFragment.getPhotosUrl(),bottomFragment.getVideosUrl(),
                        bottomFragment.getAudiosUrl())
            }
        }

        try {
            childFragmentManager.beginTransaction().replace(R.id.containerBottomSheet, bottomFragment).commit()
            bottomFragment.callback = this
            bottomSheetBehaviour = BottomSheetBehavior.from(containerBottomSheet) as AutoCloseBottomSheetBehavior<View>
            bottomSheetBehaviour.run {
                peekHeight = convertDpToPixel(35)
                halfExpandedRatio = 0.6f
                isFitToContents = false
                addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                            chooseMedias.clear()
                            chooseMedias.addAll(loadingViews.keys.map {
                                ChooseMedia(it)
                            })
                        }
                        bottomFragment.changeState(newState)
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {

                    }
                })
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        toolbarBackAction.setOnClickListener { onResultCancel() }
        //postText.showKeyboard()
        setErrorHandler()
    }

    override fun postCreateSuccessfully(postEntity: GroupPostEntity.PostEntity) {
        onResultOk(postEntity.id)
    }

    override fun showLoading(show: Boolean) {
        publishBtn.isEnabled = show
    }

    override fun showImageUploadingStarted(chooseMedia: ChooseMedia) {
        if (chooseMedia.url.contains(".mp3") || chooseMedia.url.contains(".mpeg") || chooseMedia.url.contains(".wav")){
            loadingViews[chooseMedia.url] = layoutInflater.inflate(R.layout.layout_audio_in_create_post,audioContainer, false)
            loadingViews[chooseMedia.url]?.let {
                it.trackName?.text = chooseMedia.trackName
            }
            audioContainer.addView(loadingViews[chooseMedia.url])
        }
        else {
            loadingViews[chooseMedia.url] = layoutInflater.inflate(R.layout.layout_attach_image, postContainer, false)
            loadingViews[chooseMedia.url]?.let {
                it.imagePreview?.let { draweeView ->
                    imageLoadingDelegate.loadImageFromFile(chooseMedia.url, draweeView)
                }
            }
            postContainer.addView(loadingViews[chooseMedia.url])
        }
        prepareListeners(loadingViews[chooseMedia.url], chooseMedia)
        imageUploadingStarted(loadingViews[chooseMedia.url])
        //uploadingView.showImageUploadingStarted(path)
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
        postContainer.removeView(loadingViews[path])
        audioContainer.removeView(loadingViews[path])
        loadingViews.remove(path)
        chooseMedias.removeChooseMedia(path)
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

    private fun prepareListeners(uploadingView: View?, chooseMedia: ChooseMedia) {
        uploadingView?.apply {
            refreshContainer.setOnClickListener {
                this.imageUploadingProgressBar?.progress = 0f
                childFragmentManager.setFragmentResult(MEDIA_INTERACTION_REQUEST_CODE,
                        bundleOf(METHOD_KEY to RETRY_LOADING_METHOD_CODE,
                        CHOOSE_MEDIA_KEY to chooseMedia))
                imageUploadingStarted(uploadingView)
            }
            stopUploading?.setOnClickListener {
                childFragmentManager.setFragmentResult(MEDIA_INTERACTION_REQUEST_CODE,
                        bundleOf(METHOD_KEY to  CANCEL_LOADING_METHOD_CODE,
                        CHOOSE_MEDIA_KEY to chooseMedia))
                detachImage(chooseMedia.url)
            }
            detachImage?.setOnClickListener {
                childFragmentManager.setFragmentResult(MEDIA_INTERACTION_REQUEST_CODE,
                        bundleOf(METHOD_KEY to REMOVE_CONTENT_METHOD_CODE,
                        CHOOSE_MEDIA_KEY to chooseMedia))
                detachImage(chooseMedia.url)
            }
        }
    }

//    private fun manageClipVisibility() {
//        if (postContainer.childCount > 1) {
//            attachPhoto.hide()
//        } else {
//            attachPhoto.show()
//        }
//    }

    private fun onResultOk(postId: String) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(GroupFragment.POST_ID, postId)
        findNavController().popBackStack()
    }

    private fun onResultCancel() {
        findNavController().popBackStack()
    }

    override fun getState() = bottomSheetBehaviour.state

    override fun changeStateBottomSheet(newState: Int) {
        bottomSheetBehaviour.state = newState
    }

    override fun getLoadingView() = loadingViews
    override fun closeKeyboard() {
        try {
            postText.dismissKeyboard()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

}
