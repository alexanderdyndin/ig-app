package com.intergroupapplication.presentation.feature.createpost.view

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.budiyev.android.circularprogressbar.CircularProgressBar
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.databinding.FragmentCreatePostBinding
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.TEXT
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.customview.AutoCloseBottomSheetBehavior
import com.intergroupapplication.presentation.customview.CreatePostCustomView
import com.intergroupapplication.presentation.customview.ImagesUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.chooseMedias
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.removeChooseMedia
import com.intergroupapplication.presentation.feature.createpost.presenter.CreatePostPresenter
import com.intergroupapplication.presentation.feature.editpostbottomsheet.view.EditPostBottomSheetFragment
import com.intergroupapplication.presentation.feature.group.view.GroupFragment
import io.reactivex.exceptions.CompositeException
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

open class CreatePostFragment : BaseFragment(), CreatePostView,EditPostBottomSheetFragment.Callback {

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

    private lateinit var bottomSheetBehaviour: AutoCloseBottomSheetBehavior<FrameLayout>

    protected val bottomFragment by lazy { EditPostBottomSheetFragment() }

    private val createPostBinding by viewBinding(FragmentCreatePostBinding::bind)

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_create_post

    protected val loadingViews: MutableMap<String, View?> = mutableMapOf()

    override fun getSnackBarCoordinator(): CoordinatorLayout = createPostBinding.createPostCoordinator

    protected lateinit var createPostCustomView:CreatePostCustomView
    protected lateinit var publishBtn:TextView

    private lateinit var uploadingView: ImagesUploadingView

    private lateinit var groupId: String

    override fun viewCreated() {
        createPostCustomView = createPostBinding.createPostCustomView
        publishBtn = createPostBinding.navigationToolbar.publishBtn
        chooseMedias.clear()
        groupId = arguments?.getString(GROUP_ID)!!
        presenter.groupId = groupId
        publishBtn.show()
        publishBtn.setOnClickListener {
            val post = createPostCustomView.createFinalText()
            if (post.isEmpty()){
                dialogDelegate.showErrorSnackBar(getString(R.string.post_should_contains_text))
            }
            else if (loadingViews.isNotEmpty()) {
                var isLoading = false
                loadingViews.forEach { (key, view) ->
                    val darkCard = view?.findViewById<TextView>(R.id.darkCard)
                    if (darkCard?.isVisible() != false) {
                        isLoading = true
                    }
                }
                if (isLoading)
                    dialogDelegate.showErrorSnackBar(getString(R.string.image_still_uploading))
                else {
                    createPost(post)
                }
            }
            else {
                createPost(post)
            }
        }

        try {
            childFragmentManager.beginTransaction().replace(R.id.containerBottomSheet, bottomFragment).commit()
            bottomFragment.callback = this
            bottomSheetBehaviour = BottomSheetBehavior.from(createPostBinding.containerBottomSheet) as AutoCloseBottomSheetBehavior<FrameLayout>
            bottomSheetBehaviour.run {
                peekHeight = requireContext().dpToPx(35)
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
        firstCreateView()
        createPostBinding.navigationToolbar.
            toolbarBackAction.setOnClickListener { onResultCancel() }
        setErrorHandler()
    }

    protected open fun createPost(post:String) {
        presenter.createPostWithImage(
                post,
                groupId,
                bottomFragment.getPhotosUrl(), bottomFragment.getVideosUrl(),
                bottomFragment.getAudiosUrl())
    }

    override fun postCreateSuccessfully(postEntity: GroupPostEntity.PostEntity) {
        onResultOk(postEntity.id)
    }

    override fun showLoading(show: Boolean) {
        publishBtn.isEnabled = show
    }

    override fun showImageUploadingStarted(chooseMedia: ChooseMedia) {
        if (chooseMedia.url.contains(".mp3") || chooseMedia.url.contains(".mpeg") || chooseMedia.url.contains(".wav")){
            loadingViews[chooseMedia.url] = layoutInflater.inflate(R.layout.layout_audio_in_create_post,
                    createPostCustomView.audioContainer, false)
            loadingViews[chooseMedia.url]?.let {
                val trackName = view?.findViewById<TextView>(R.id.trackName)
                trackName?.text = chooseMedia.name
            }
            createPostCustomView.addMusic(chooseMedia.name,loadingViews[chooseMedia.url])
        }
        else {
            loadingViews[chooseMedia.url] = layoutInflater.inflate(R.layout.layout_attach_image,
                    createPostCustomView.imageContainer, false)
            loadingViews[chooseMedia.url]?.let {
                val imagePreview = it.findViewById<SimpleDraweeView>(R.id.imagePreview)
                imageLoadingDelegate.loadImageFromFile(chooseMedia.url, imagePreview)
            }
            createPostCustomView.addImageOrVideo(chooseMedia.url.substringAfterLast("/"),
                    loadingViews[chooseMedia.url])
        }
        prepareListeners(loadingViews[chooseMedia.url], chooseMedia)
        imageUploadingStarted(loadingViews[chooseMedia.url])
        if (loadingViews.size == chooseMedias.size && createPostCustomView.currentContainerIsLast()){
            createPostCustomView.createAllMainView()
        }
    }

    protected open fun firstCreateView() {
        createPostCustomView.createAllMainView()
    }

    override fun showImageUploaded(path: String) {
        loadingViews[path]?.apply {
            val darkCard = findViewById<TextView>(R.id.darkCard)
            val stopUploading = findViewById<ImageView>(R.id.stopUploading)
            val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            val detachImage = findViewById<ImageView>(R.id.detachImage)
            darkCard?.hide()
            stopUploading?.hide()
            imageUploadingProgressBar?.hide()
            detachImage?.show()
        }
    }

    override fun showImageUploadingProgress(progress: Float, path: String) {
        loadingViews[path]?.apply {
            val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            imageUploadingProgressBar?.progress = progress
        }
    }

    override fun showImageUploadingError(path: String) {
        loadingViews[path]?.apply {
            val darkCard = findViewById<TextView>(R.id.darkCard)
            val stopUploading = findViewById<ImageView>(R.id.stopUploading)
            val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            val detachImage = findViewById<ImageView>(R.id.detachImage)
            val refreshContainer = findViewById<LinearLayout>(R.id.refreshContainer)
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

    protected fun detachImage(path: String) {
        createPostCustomView.listImageContainers.forEach {container->
            container.removeView(loadingViews[path])
        }
        createPostCustomView.listAudioContainers.forEach {container->
            container.removeView(loadingViews[path])
        }
        createPostCustomView.detachName(loadingViews[path])
        loadingViews.remove(path)
        chooseMedias.removeChooseMedia(path)
    }

    private fun imageUploadingStarted(uploadingView: View?) {
        uploadingView?.apply {
            val darkCard = findViewById<TextView>(R.id.darkCard)
            val stopUploading = findViewById<ImageView>(R.id.stopUploading)
            val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            val detachImage = findViewById<ImageView>(R.id.detachImage)
            val refreshContainer = findViewById<LinearLayout>(R.id.refreshContainer)
            darkCard?.show()
            imageUploadingProgressBar?.show()
            stopUploading?.show()
            detachImage?.hide()
            refreshContainer?.hide()
        }
    }

    private fun prepareListeners(uploadingView: View?, chooseMedia: ChooseMedia) {
        uploadingView?.apply {
            val stopUploading = findViewById<ImageView>(R.id.stopUploading)
            val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            val detachImage = findViewById<ImageView>(R.id.detachImage)
            val refreshContainer = findViewById<LinearLayout>(R.id.refreshContainer)
            refreshContainer.setOnClickListener {
                imageUploadingProgressBar?.progress = 0f
                childFragmentManager.setFragmentResult(MEDIA_INTERACTION_REQUEST_CODE,
                        bundleOf(METHOD_KEY to RETRY_LOADING_METHOD_CODE,
                        CHOOSE_MEDIA_KEY to chooseMedia))
                createPostCustomView.listImageContainers.forEach {container->
                    container.removeView(uploadingView)
                }
                createPostCustomView.listAudioContainers.forEach {container->
                    container.removeView(uploadingView)
                }
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
            createPostCustomView.listEditText.forEach { editText->
                editText.dismissKeyboard()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        chooseMedias.clear()
    }
}
