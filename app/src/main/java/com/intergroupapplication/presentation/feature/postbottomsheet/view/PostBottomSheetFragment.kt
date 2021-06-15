package com.intergroupapplication.presentation.feature.postbottomsheet.view

import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.databinding.FragmentEditPostBottomSheetBinding
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.base.BaseBottomSheetFragment
import com.intergroupapplication.presentation.base.ImageUploadingView
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostFragment
import com.intergroupapplication.presentation.feature.postbottomsheet.presenter.PostBottomSheetPresenter
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

class PostBottomSheetFragment:BaseBottomSheetFragment(),PostBottomSheetView {

    lateinit var callback: Callback
    private val editPostBottomBinding by viewBinding(FragmentEditPostBottomSheetBinding::bind)

    @Inject
    @InjectPresenter
    lateinit var presenter: PostBottomSheetPresenter

    @ProvidePresenter
    fun providePresenter(): PostBottomSheetPresenter = presenter

    override fun layoutRes() = R.layout.fragment_edit_post_bottom_sheet

    override fun getSnackBarCoordinator() = editPostBottomBinding.bottomSheetCoordinator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnAdd.isEnabled = false
        parentFragmentManager.setFragmentResultListener(CreatePostFragment.MEDIA_INTERACTION_REQUEST_CODE, this) { _, bundle ->
            val chooseMedia: ChooseMedia = bundle.getParcelable(CreatePostFragment.CHOOSE_MEDIA_KEY)?: ChooseMedia("")
            when (bundle.getInt(CreatePostFragment.METHOD_KEY)){
                CreatePostFragment.RETRY_LOADING_METHOD_CODE-> presenter.retryLoading(chooseMedia)
                CreatePostFragment.CANCEL_LOADING_METHOD_CODE -> presenter.cancelUploading(chooseMedia.url)
                CreatePostFragment.REMOVE_CONTENT_METHOD_CODE -> {presenter.removeContent(chooseMedia.url)
                }
            }
        }
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

    override fun changeStateWhenAttachFile() {
        if (callback.getState() == BottomSheetBehavior.STATE_COLLAPSED){
            callback.changeStateBottomSheet(BottomSheetBehavior.STATE_HALF_EXPANDED)
        }
    }

    override fun changeStateViewAfterAddMedia() {
        callback.changeStateBottomSheet(BottomSheetBehavior.STATE_COLLAPSED)
    }

    override fun closeKeyboard() {
        callback.closeKeyboard()
    }

    override fun stateSettling() {
    }

    override fun stateExpanded() {
    }

    override fun stateHalfExpanded() {
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
        fun getState():Int
        fun changeStateBottomSheet(newState: Int)
        fun getLoadingView():Map<String,View?>
        fun closeKeyboard()
    }
}