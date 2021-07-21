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
import com.intergroupapplication.data.model.ProgressMediaModel
import com.intergroupapplication.databinding.FragmentPostBottomSheetBinding
import com.intergroupapplication.domain.entity.*
import com.intergroupapplication.presentation.base.BaseBottomSheetFragment
import com.intergroupapplication.presentation.dialogs.progress.view.ProgressDialog
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostFragment
import com.intergroupapplication.presentation.feature.postbottomsheet.presenter.PostBottomSheetPresenter
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

class PostBottomSheetFragment : BaseBottomSheetFragment(), PostBottomSheetView {

    companion object {
        const val VIEW_CHANGE_REQUEST_CODE = "view_change_request_code"
        const val METHOD_KEY = "method_key"
        const val DATA_KEY = "data_key"
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
        const val DELETE_MEDIA_CODE = 111
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
            CreatePostFragment.MEDIA_INTERACTION_REQUEST_CODE, viewLifecycleOwner
        ) { _, bundle ->
            val chooseMedia: ChooseMedia = bundle.getParcelable(CreatePostFragment.CHOOSE_MEDIA_KEY)
                ?: ChooseMedia("",name = "",type = MediaType.IMAGE)
            when (bundle.getInt(CreatePostFragment.METHOD_KEY)) {
                CreatePostFragment.RETRY_LOADING_METHOD_CODE -> presenter.retryLoading(chooseMedia)
                CreatePostFragment.CANCEL_LOADING_METHOD_CODE -> {
                    presenter.cancelUploading(chooseMedia)
                }
                CreatePostFragment.REMOVE_CONTENT_METHOD_CODE -> {
                    presenter.removeContent(chooseMedia)
                }
                CreatePostFragment.IC_EDIT_ALIGN_METHOD_CODE -> {
                    val isActivated = bundle.getBoolean(CreatePostFragment.ACTIVATED_KEY)
                    icEditAlign.activated(isActivated)
                }
                CreatePostFragment.IC_EDIT_TEXT_METHOD_CODE -> {
                    val isActivated = bundle.getBoolean(CreatePostFragment.ACTIVATED_KEY)
                    icEditText.activated(isActivated)
                }
                CreatePostFragment.IC_EDIT_COLOR_METHOD_CODE -> {
                    changeStateToHalfExpanded()
                    icEditColor.hideKeyboard()
                    startChooseColorText()
                }
                CreatePostFragment.IC_ATTACH_FILE_METHOD_CODE -> {
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
        childFragmentManager.setFragmentResultListener(
            ProgressDialog.CALLBACK_METHOD_KEY,
            viewLifecycleOwner){ _, result:Bundle->
            when(result.getInt(ProgressDialog.METHOD_KEY)){
                ProgressDialog.RETRY_LOADING_CODE -> {
                    result.getParcelable<ChooseMedia>(ProgressDialog.DATA_KEY)?.let {
                        presenter.retryLoading(it)
                    }
                }
                ProgressDialog.CANCEL_UPLOADING_CODE -> {
                    result.getParcelable<ChooseMedia>(ProgressDialog.DATA_KEY)?.let {
                        presenter.cancelUploading(it)
                        deleteMediaFromEditor(it)
                    }
                }
                ProgressDialog.REMOVE_CONTENT_CODE -> {
                    result.getParcelable<ChooseMedia>(ProgressDialog.DATA_KEY)?.let {
                        presenter.removeContent(it)
                        deleteMediaFromEditor(it)
                    }
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
        setFragmentResult(
            bundleOf(
                METHOD_KEY to CHANGE_STATE_METHOD_CODE,
                DATA_KEY to BottomSheetBehavior.STATE_COLLAPSED
            )
        )
        setFragmentResult(bundleOf(METHOD_KEY to SHOW_KEYBOARD_METHOD_CODE))
        setFragmentResult(
            bundleOf(
                METHOD_KEY to CHANGE_TEXT_COLOR_METHOD_CODE,
                DATA_KEY to color
            )
        )
    }

    override fun startChooseColorText() {
        super.startChooseColorText()
        setFragmentResult(bundleOf(METHOD_KEY to GONE_PANEL_GRAVITY_METHOD_CODE))
        setFragmentResult(bundleOf(METHOD_KEY to GONE_PANEL_STYLE_METHOD_CODE))
    }

    override fun attachFileNotActivated() {
        setFragmentResult(
            bundleOf(
                METHOD_KEY to CHANGE_STATE_METHOD_CODE,
                DATA_KEY to BottomSheetBehavior.STATE_COLLAPSED
            )
        )
    }

    override fun attachFileActivated() {
        super.attachFileActivated()
        setFragmentResult(bundleOf(METHOD_KEY to GONE_PANEL_GRAVITY_METHOD_CODE))
        setFragmentResult(bundleOf(METHOD_KEY to GONE_PANEL_STYLE_METHOD_CODE))
    }

    override fun attachGallery() {
        presenter.attachMedia(presenter::loadImage)
    }

    override fun attachVideo() {
        presenter.attachMedia(presenter::loadVideo)
    }

    override fun attachAudio() {
        presenter.attachMedia(presenter::loadAudio)
    }

    override fun attachFromCamera() {
        presenter.attachFromCamera()
    }

    override fun changeStateToHalfExpanded() {
        if (currentState == BottomSheetBehavior.STATE_COLLAPSED) {
            setFragmentResult(
                bundleOf(
                    METHOD_KEY to CHANGE_STATE_METHOD_CODE,
                    DATA_KEY to BottomSheetBehavior.STATE_HALF_EXPANDED
                )
            )
        }
        panelAddFile.show()
    }

    override fun changeStateViewAfterAddMedia() {
        setFragmentResult(bundleOf(METHOD_KEY to CHANGE_STATE_AFTER_ADD_MEDIA_METHOD_CODE))
        dialogDelegate.showProgressDialog()
    }

    override fun deleteMediaFromEditor(chooseMedia: ChooseMedia) {
        when (chooseMedia.type) {
            MediaType.AUDIO -> {
                setFragmentResult(
                    bundleOf(
                    METHOD_KEY to DELETE_MEDIA_CODE, DATA_KEY to
                    "${ParseConstants.START_AUDIO}${chooseMedia.url}${ParseConstants.END_AUDIO}"))
            }
            MediaType.IMAGE -> {
                setFragmentResult(
                    bundleOf(
                    METHOD_KEY to DELETE_MEDIA_CODE, DATA_KEY to
                    "${ParseConstants.START_IMAGE}${chooseMedia.url}${ParseConstants.END_IMAGE}"))
            }
            MediaType.VIDEO -> {
                setFragmentResult(
                    bundleOf(
                    METHOD_KEY to DELETE_MEDIA_CODE, DATA_KEY to
                    "${ParseConstants.START_VIDEO}${chooseMedia.url}${ParseConstants.END_VIDEO}"))

            }
        }
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
        setFragmentResult(
            bundleOf(
                METHOD_KEY to STARTED_UPLOADED_METHOD_CODE,
                DATA_KEY to chooseMedia
            )
        )
        setLoadStateResult(ProgressMediaModel(chooseMedia,LoadMediaType.START))
    }

    override fun showImageUploadingProgress(progress: Float, chooseMedia: ChooseMedia) {
        setFragmentResult(
            bundleOf(
                METHOD_KEY to PROGRESS_UPLOADED_METHOD_CODE,
                PROGRESS_KEY to progress, DATA_KEY to chooseMedia.url
            )
        )
        setLoadStateResult(ProgressMediaModel(chooseMedia,LoadMediaType.PROGRESS.apply {
            this.progress = progress
        }))
    }

    override fun showImageUploadingError(chooseMedia: ChooseMedia) {
        setFragmentResult(
            bundleOf(
                METHOD_KEY to ERROR_UPLOADED_METHOD_CODE,
                DATA_KEY to chooseMedia.url
            )
        )
        setLoadStateResult(ProgressMediaModel(chooseMedia,LoadMediaType.ERROR))
    }

    override fun showImageUploaded(chooseMedia: ChooseMedia) {
        setFragmentResult(
            bundleOf(
                METHOD_KEY to UPLOAD_METHOD_CODE,
                DATA_KEY to chooseMedia.url
            )
        )
        setLoadStateResult(ProgressMediaModel(chooseMedia,LoadMediaType.UPLOAD))
    }

    fun getPhotosUrl() = presenter.getPhotosUrl()
    fun getVideosUrl() = presenter.getVideosUrl()
    fun getAudiosUrl() = presenter.getAudiosUrl()

    fun addAudioInAudiosUrl(audios: List<AudioEntity>) = presenter.addAudioInAudiosUrl(audios)
    fun addVideoInVideosUrl(videos: List<FileEntity>) = presenter.addVideoInVideosUrl(videos)
    fun addImagesInPhotosUrl(images: List<FileEntity>) = presenter.addImagesInPhotosUrl(images)

    override fun setFragmentResult(bundle: Bundle) {
        parentFragmentManager.setFragmentResult(
            VIEW_CHANGE_REQUEST_CODE,
            bundle
        )
    }
}