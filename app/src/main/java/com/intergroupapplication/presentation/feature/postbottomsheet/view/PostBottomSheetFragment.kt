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

    lateinit var callback: Callback
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
                removeContent(chooseMedia.url)}
                CreatePostFragment.ACTIVATED_BOLD -> boldText.activated(true)
                CreatePostFragment.ACTIVATED_ITALIC -> italicText.activated(true)
                CreatePostFragment.ACTIVATED_UNDERLINE -> underlineText.activated(true)
                CreatePostFragment.ACTIVATED_STRIKETHROUGH -> strikeText.activated(true)
                CreatePostFragment.NOT_ACTIVATED_ALL_BUTTONS -> notActivatedAllButtons()
                CreatePostFragment.SET_JUSTIFY_LEFT -> leftGravityButton.isChecked = true
                CreatePostFragment.SET_JUSTIFY_CENTER -> centerGravityButton.isChecked = true
                CreatePostFragment.SET_JUSTIFY_RIGHT -> rightGravityButton.isChecked = true
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

    private fun notActivatedAllButtons() {
        boldText.activated(false)
        italicText.activated(false)
        underlineText.activated(false)
        strikeText.activated(false)
    }

    override fun gonePanelStyleText() {
        super.gonePanelStyleText()
        callback.changeHeight(calculateHeight())
    }

    override fun showPanelStyleText() {
        super.showPanelStyleText()
        val height = calculateHeight() + heightTextStylePanel
        callback.changeHeight(height)
    }

    override fun gonePanelGravityText() {
        super.gonePanelGravityText()
        callback.changeHeight(calculateHeight())
    }

    override fun showPanelGravityText() {
        super.showPanelGravityText()
        val height = calculateHeight() + heightTextStylePanel
        callback.changeHeight(height)
    }

    override fun calculateHeight() = heightIconPanel

    override fun setupBoldText() {
        callback.setUpBoldText()
    }

    override fun setupItalicText() {
        callback.setUpItalicText()
    }

    override fun setupStrikeText() {
        callback.setupStrikeText()
    }

    override fun setupUnderlineText() {
        callback.setupUnderlineText()
    }

    override fun changeTextColor(color: Int) {
        endChooseColorText()
        super.changeTextColor(color)
        callback.run {
            changeStateBottomSheet(BottomSheetBehavior.STATE_COLLAPSED)
            showKeyboard()
            changeTextColor(color)
        }
    }

    override fun setupLeftGravity() {
        callback.setAlignLeft()
    }

    override fun setupCenterGravity() {
        callback.setAlignCenter()
    }

    override fun setupRightGravity() {
        callback.setAlignRight()
    }

    override fun attachFileNotActivated() {
        callback.changeStateBottomSheet(BottomSheetBehavior.STATE_COLLAPSED)
    }

    override fun attachFileActivated() {
    }

    override fun attachGallery() {
        presenter.attachMedia(galleryAdapter.getChoosePhotosFromObservable(), presenter::loadImage,
        callback.getLoadingView())
    }

    override fun attachVideo() {
        presenter.attachMedia(videoAdapter.getChooseVideosFromObservable(), presenter::loadVideo,
        callback.getLoadingView())
    }

    override fun attachAudio() {
        presenter.attachMedia(audioAdapter.getChooseAudiosFromObservable(), presenter::loadAudio,
        callback.getLoadingView())
    }

    override fun attachFromCamera() {
        presenter.attachFromCamera()
    }

    override fun changeStateToHalfExpanded() {
        if (callback.getState() == BottomSheetBehavior.STATE_COLLAPSED){
            callback.changeStateBottomSheet(BottomSheetBehavior.STATE_HALF_EXPANDED)
        }
        panelAddFile.show()
    }

    override fun changeStateViewAfterAddMedia() {
        callback.changeStateBottomSheet(BottomSheetBehavior.STATE_COLLAPSED)
    }

    override fun closeKeyboard() {
        callback.closeKeyboard()
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
        callback.showImageUploadingStarted(chooseMedia)
    }

    override fun showImageUploadingProgress(progress: Float, path: String) {
        callback.showImageUploadingProgress(progress,path)
    }

    override fun showImageUploadingError(path: String) {
        callback.showImageUploadingError(path)
    }

    override fun showImageUploaded(path: String) {
        callback.showImageUploaded(path)
    }

    fun getPhotosUrl() = presenter.getPhotosUrl()
    fun getVideosUrl() = presenter.getVideosUrl()
    fun getAudiosUrl() = presenter.getAudiosUrl()

    fun addAudioInAudiosUrl(audios:List<AudioEntity>) = presenter.addAudioInAudiosUrl(audios)
    fun addVideoInVideosUrl(videos:List<FileEntity>) = presenter.addVideoInVideosUrl(videos)
    fun addImagesInPhotosUrl(images:List<FileEntity>) = presenter.addImagesInPhotosUrl(images)

    interface Callback:ImageUploadingView{
        fun changeHeight(height:Int)
        fun getState():Int
        fun changeStateBottomSheet(newState: Int)
        fun getLoadingView():Map<String,View?>
        fun closeKeyboard()
        fun showKeyboard()
        fun changeTextColor(color:Int)
        fun setUpBoldText()
        fun setUpItalicText()
        fun setupStrikeText()
        fun setupUnderlineText()
        fun setAlignLeft()
        fun setAlignCenter()
        fun setAlignRight()
    }
}