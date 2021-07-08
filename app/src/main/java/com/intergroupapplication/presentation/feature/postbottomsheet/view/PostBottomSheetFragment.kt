package com.intergroupapplication.presentation.feature.postbottomsheet.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.databinding.FragmentPostBottomSheetBinding
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.base.BaseBottomSheetFragment
import com.intergroupapplication.presentation.base.ImageUploadingView
import com.intergroupapplication.presentation.exstension.activated
import com.intergroupapplication.presentation.exstension.dpToPx
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostFragment
import com.intergroupapplication.presentation.feature.postbottomsheet.presenter.PostBottomSheetPresenter
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

class PostBottomSheetFragment:BaseBottomSheetFragment(),PostBottomSheetView {

    var callback: Callback? = null
    private val postBottomBinding by viewBinding(FragmentPostBottomSheetBinding::bind)
    private val heightIconPanel by lazy { context?.dpToPx(40)?:0 }

    @Inject
    @InjectPresenter
    lateinit var presenter: PostBottomSheetPresenter

    @ProvidePresenter
    fun providePresenter(): PostBottomSheetPresenter = presenter

    override fun layoutRes() = R.layout.fragment_post_bottom_sheet

    override fun getSnackBarCoordinator() = postBottomBinding.bottomSheetCoordinator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        parentFragmentManager.setFragmentResultListener(
            CreatePostFragment.MEDIA_INTERACTION_REQUEST_CODE, viewLifecycleOwner) { _, bundle ->
            val chooseMedia: ChooseMedia = bundle.getParcelable(CreatePostFragment.CHOOSE_MEDIA_KEY)
                ?: ChooseMedia("")
            when (bundle.getInt(CreatePostFragment.METHOD_KEY)){
                CreatePostFragment.RETRY_LOADING_METHOD_CODE-> presenter.retryLoading(chooseMedia)
                CreatePostFragment.CANCEL_LOADING_METHOD_CODE -> {presenter.
                cancelUploading(chooseMedia.url)}
                CreatePostFragment.REMOVE_CONTENT_METHOD_CODE -> {presenter.
                    removeContent(chooseMedia.url)
                }
                CreatePostFragment.IC_EDIT_ALIGN_METHOD_CODE->{
                    val isActivated = bundle.getBoolean(CreatePostFragment.ACTIVATED_KEY)
                    icEditAlign.activated(isActivated)
                }
                CreatePostFragment.IC_EDIT_TEXT_METHOD_CODE->{
                    val isActivated = bundle.getBoolean(CreatePostFragment.ACTIVATED_KEY)
                    icEditText.activated(isActivated)
                }
                CreatePostFragment.IC_EDIT_COLOR_METHOD_CODE ->{
                    changeStateToHalfExpanded()
                    //closeKeyboard()
                    callback?.closeKeyboard()
                    startChooseColorText()
                }
                CreatePostFragment.IC_ATTACH_FILE_METHOD_CODE->{
                    requestPermission()
                    changeStateToHalfExpanded()
                    callback?.closeKeyboard()
                    icAttachFile.activated(true)
                }
                CreatePostFragment.CHANGE_COLOR -> {
                    val color = bundle.getInt(CreatePostFragment.COLOR_KEY)
                    icEditColor.setImageDrawable(colorDrawableGateway.getDrawableByColor(color))
                }
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun viewCreated() {
        super.viewCreated()
        btnAdd.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callback = null
    }

    override fun gonePanelStyleText() {
        callback?.gonePanelStyleText()
    }

    override fun showPanelStyleText() {
        super.showPanelStyleText()
        callback?.showPanelStyleText()
    }

    override fun gonePanelGravityText() {
        callback?.gonePanelGravity()
    }

    override fun showPanelGravityText() {
        super.showPanelGravityText()
        callback?.showPanelGravity()
    }

    override fun calculateHeight() = heightIconPanel



    override fun changeTextColor(color: Int) {
        endChooseColorText()
        super.changeTextColor(color)
        callback?.run {
            changeStateBottomSheet(BottomSheetBehavior.STATE_COLLAPSED)
            showKeyboard()
            changeTextColor(color)
        }
    }

    override fun startChooseColorText() {
        super.startChooseColorText()
        callback?.run {
            gonePanelStyleText()
            gonePanelGravity()
        }
    }

    override fun attachFileNotActivated() {
        callback?.changeStateBottomSheet(BottomSheetBehavior.STATE_COLLAPSED)
    }

    override fun attachFileActivated() {
    }

    override fun attachGallery() {
        presenter.attachMedia(galleryAdapter.getChoosePhotosFromObservable(), presenter::loadImage)
    }

    override fun attachVideo() {
        presenter.attachMedia(videoAdapter.getChooseVideosFromObservable(), presenter::loadVideo)
    }

    override fun attachAudio() {
        presenter.attachMedia(audioAdapter.getChooseAudiosFromObservable(), presenter::loadAudio)
    }

    override fun attachFromCamera() {
        presenter.attachFromCamera()
    }

    override fun changeStateToHalfExpanded() {
        if (callback?.getState() == BottomSheetBehavior.STATE_COLLAPSED){
            callback?.changeStateBottomSheet(BottomSheetBehavior.STATE_HALF_EXPANDED)
        }
        panelAddFile.show()
    }

    override fun changeStateViewAfterAddMedia() {
        callback?.changeStateBottomSheet(BottomSheetBehavior.STATE_COLLAPSED)
    }

    override fun stateSettling() {
        changeBottomConstraintForView(horizontalGuideEnd.id)
    }

    override fun stateExpanded() {
    }

    override fun stateHalfExpanded() {
        changeBottomConstraintForView(horizontalGuideCenter.id)
    }

    override fun stateDragging() {
    }

    override fun stateHidden() {
    }

    override fun showImageUploadingStarted(chooseMedia: ChooseMedia) {
        callback?.showImageUploadingStarted(chooseMedia)
    }

    override fun showImageUploadingProgress(progress: Float, path: String) {
        callback?.showImageUploadingProgress(progress,path)
    }

    override fun showImageUploadingError(path: String) {
        callback?.showImageUploadingError(path)
    }

    override fun showImageUploaded(path: String) {
        callback?.showImageUploaded(path)
    }

    fun getPhotosUrl() = presenter.getPhotosUrl()
    fun getVideosUrl() = presenter.getVideosUrl()
    fun getAudiosUrl() = presenter.getAudiosUrl()

    fun addAudioInAudiosUrl(audios:List<AudioEntity>) = presenter.addAudioInAudiosUrl(audios)
    fun addVideoInVideosUrl(videos:List<FileEntity>) = presenter.addVideoInVideosUrl(videos)
    fun addImagesInPhotosUrl(images:List<FileEntity>) = presenter.addImagesInPhotosUrl(images)

    interface Callback:ImageUploadingView{
        fun getState():Int
        fun changeStateBottomSheet(newState: Int)
        fun closeKeyboard()
        fun showKeyboard()
        fun changeTextColor(color:Int)
        fun showPanelStyleText()
        fun gonePanelStyleText()
        fun showPanelGravity()
        fun gonePanelGravity()
    }
}