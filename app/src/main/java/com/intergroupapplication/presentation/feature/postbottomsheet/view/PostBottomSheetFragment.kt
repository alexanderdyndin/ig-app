package com.intergroupapplication.presentation.feature.postbottomsheet.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.databinding.FragmentPostBottomSheetBinding
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.base.BaseBottomSheetFragment
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostFragment
import com.intergroupapplication.presentation.feature.postbottomsheet.presenter.PostBottomSheetPresenter
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject
class PostBottomSheetFragment:BaseBottomSheetFragment(),PostBottomSheetView {

    companion object{
        const val VIEW_CHANGE_REQUEST_CODE = "view_change_request_code"
        const val METHOD_KEY = "method_key"
        const val NEW_STATE_KEY = "new_state_key"
        const val COLOR_KEY = "color_key"
        const val CHOOSE_MEDIA_KEY = "choose_media_key"
        const val PATH_KEY = "path_key"
        const val PROGRESS_KEY = "progress_key"
        const val CHANGE_STATE_AFTER_ADD_MEDIA_METHOD_CODE = 99
        const val CHANGE_STATE_METHOD_CODE = 100
        const val SHOW_KEYBOARD_METHOD_CODE = 101
        const val CHANGE_TEXT_COLOR_METHOD_CODE = 102
        const val SHOW_PANEL_STYLE_METHOD_CODE = 103
        const val GONE_PANEL_STYLE_METHOD_CODE = 104
        const val SHOW_PANEL_GRAVITY_METHOD_CODE = 105
        const val GONE_PANEL_GRAVITY_METHOD_CODE = 106
        const val STARTED_UPLOADED_METHOD_CODE = 107
        const val PROGRESS_UPLOADED_METHOD_CODE = 108
        const val ERROR_UPLOADED_METHOD_CODE = 109
        const val UPLOAD_METHOD_CODE = 110
    }

    private val postBottomBinding by viewBinding(FragmentPostBottomSheetBinding::bind)

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
                    icEditColor.hideKeyboard()
                    startChooseColorText()
                }
                CreatePostFragment.IC_ATTACH_FILE_METHOD_CODE->{
                    requestPermission()
                    changeStateToHalfExpanded()
                    icAttachFile.run {
                        hideKeyboard()
                        activated(true)
                    }
                    icEditText.activated(false)
                    icEditAlign.activated(false)
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

    override fun gonePanelStyleText() {
        setFragmentResult(bundleOf(METHOD_KEY to GONE_PANEL_STYLE_METHOD_CODE))
    }

    override fun showPanelStyleText() {
        super.showPanelStyleText()
        setFragmentResult(bundleOf(METHOD_KEY to SHOW_PANEL_STYLE_METHOD_CODE))
    }

    override fun gonePanelGravityText() {
        setFragmentResult(bundleOf(METHOD_KEY to GONE_PANEL_GRAVITY_METHOD_CODE))
    }

    override fun showPanelGravityText() {
        super.showPanelGravityText()
        setFragmentResult(bundleOf(METHOD_KEY to SHOW_PANEL_GRAVITY_METHOD_CODE))
    }

    override fun calculateHeight() = heightIconPanel



    override fun changeTextColor(color: Int) {
        endChooseColorText()
        super.changeTextColor(color)
        setFragmentResult(bundleOf(METHOD_KEY to CHANGE_STATE_METHOD_CODE,
            NEW_STATE_KEY to BottomSheetBehavior.STATE_COLLAPSED))
        setFragmentResult(bundleOf(METHOD_KEY to SHOW_KEYBOARD_METHOD_CODE))
        setFragmentResult(bundleOf(METHOD_KEY to CHANGE_TEXT_COLOR_METHOD_CODE,
            COLOR_KEY to color))
    }

    override fun startChooseColorText() {
        super.startChooseColorText()
        setFragmentResult(bundleOf(METHOD_KEY to GONE_PANEL_GRAVITY_METHOD_CODE))
        setFragmentResult(bundleOf(METHOD_KEY to GONE_PANEL_STYLE_METHOD_CODE))
    }

    override fun attachFileNotActivated() {
        setFragmentResult(bundleOf(METHOD_KEY to CHANGE_STATE_METHOD_CODE,
            NEW_STATE_KEY to BottomSheetBehavior.STATE_COLLAPSED))
    }

    override fun attachFileActivated() {
        super.attachFileActivated()
        setFragmentResult(bundleOf(METHOD_KEY to GONE_PANEL_GRAVITY_METHOD_CODE))
        setFragmentResult(bundleOf(METHOD_KEY to GONE_PANEL_STYLE_METHOD_CODE))
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
        if (currentState == BottomSheetBehavior.STATE_COLLAPSED){
            setFragmentResult(bundleOf(METHOD_KEY to CHANGE_STATE_METHOD_CODE,
                NEW_STATE_KEY to BottomSheetBehavior.STATE_HALF_EXPANDED))
        }
        panelAddFile.show()
    }

    override fun changeStateViewAfterAddMedia() {
        setFragmentResult(bundleOf(METHOD_KEY to CHANGE_STATE_AFTER_ADD_MEDIA_METHOD_CODE))
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
        setFragmentResult(bundleOf(METHOD_KEY to STARTED_UPLOADED_METHOD_CODE,
            CHOOSE_MEDIA_KEY to chooseMedia))
    }

    override fun showImageUploadingProgress(progress: Float, path: String) {
        setFragmentResult(bundleOf(METHOD_KEY to PROGRESS_UPLOADED_METHOD_CODE,
            PROGRESS_KEY to progress, PATH_KEY to path))
    }

    override fun showImageUploadingError(path: String) {
        setFragmentResult(bundleOf(METHOD_KEY to ERROR_UPLOADED_METHOD_CODE,
            PATH_KEY to path))
    }

    override fun showImageUploaded(path: String) {
        setFragmentResult(bundleOf(METHOD_KEY to UPLOAD_METHOD_CODE,
            PATH_KEY to path))
    }

    fun getPhotosUrl() = presenter.getPhotosUrl()
    fun getVideosUrl() = presenter.getVideosUrl()
    fun getAudiosUrl() = presenter.getAudiosUrl()

    fun addAudioInAudiosUrl(audios:List<AudioEntity>) = presenter.addAudioInAudiosUrl(audios)
    fun addVideoInVideosUrl(videos:List<FileEntity>) = presenter.addVideoInVideosUrl(videos)
    fun addImagesInPhotosUrl(images:List<FileEntity>) = presenter.addImagesInPhotosUrl(images)

    override fun setFragmentResult(bundle: Bundle){
        parentFragmentManager.setFragmentResult(
            VIEW_CHANGE_REQUEST_CODE,
            bundle)
    }
}