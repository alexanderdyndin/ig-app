package com.intergroupapplication.presentation.base

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.domain.entity.MediaType
import com.intergroupapplication.domain.gateway.AddLocalMediaGateway
import com.intergroupapplication.domain.gateway.ColorDrawableGateway
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.MediaAdapter
import com.intergroupapplication.presentation.widgets.PreviewDialog
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

abstract class BaseBottomSheetFragment : BaseFragment(), MediaAdapter.MediaCallback,
    ImageUploadingView {


    companion object {
        const val PROGRESS_KEY = "progress_key"
        const val PROGRESS_MODEL_KEY = "progress_model_key"
    }

    @Inject
    lateinit var addLocalMediaGateway: AddLocalMediaGateway

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var galleryAdapter: MediaAdapter.GalleryAdapter

    @Inject
    lateinit var audioAdapter: MediaAdapter.AudioAdapter

    @Inject
    lateinit var videoAdapter: MediaAdapter.VideoAdapter

    @Inject
    lateinit var playlistAdapter: MediaAdapter.PlaylistAdapter

    @Inject
    lateinit var colorAdapter: MediaAdapter.ColorAdapter

    @Inject
    lateinit var colorDrawableGateway: ColorDrawableGateway

    private val permissions by lazy { RxPermissions(this) }

    protected lateinit var mediaRecyclerView: RecyclerView
    protected lateinit var icAttachFile: ImageView
    protected lateinit var icEditText: ImageView
    protected lateinit var icEditColor: ImageView
    protected lateinit var icEditAlign: ImageView
    protected lateinit var panelAddFile: LinearLayout
    private lateinit var galleryButton: TextView
    private lateinit var musicButton: TextView
    private lateinit var videoButton: TextView
    private lateinit var playlistButton: TextView
    protected lateinit var btnAdd: TextView
    protected lateinit var amountFiles: TextView
    protected lateinit var horizontalGuideCenter: Guideline
    protected lateinit var horizontalGuideEnd: Guideline
    protected var currentState = BottomSheetBehavior.STATE_COLLAPSED
    protected val heightTextStylePanel by lazy { context?.dpToPx(40) ?: 0 }
    protected val heightIconPanel by lazy { context?.dpToPx(40) ?: 0 }
    private val recyclerPadding by lazy { context?.dpToPx(14) ?: 0 }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        childFragmentManager.setFragmentResultListener(
            PreviewDialog.ADD_REQUEST_CODE,
            viewLifecycleOwner
        ) { _, bundle ->
            val isPhoto = bundle.getBoolean(PreviewDialog.IS_PHOTO_KEY)
            val result = bundle.getString(PreviewDialog.ADD_URI_KEY)
            val isChoose = bundle.getBoolean(PreviewDialog.IS_CHOOSE_KEY)
            val urlPreview = bundle.getString(PreviewDialog.VIDEO_PREVIEW_KEY) ?: ""
            result?.let { url ->
                if (isPhoto) {
                    galleryAdapter.run {
                        photos.mapIndexed { index, galleryModel ->
                            if (galleryModel.url == url) {
                                galleryModel.isChoose = isChoose
                                notifyItemChanged(index)
                                if (isChoose) {
                                    choosePhoto.addChooseMedia(
                                        ChooseMedia(
                                            url = galleryModel.url,
                                            name = galleryModel.url.substringAfterLast("/"),
                                            type = MediaType.IMAGE
                                        )
                                    )
                                } else {
                                    choosePhoto.removeChooseMedia(galleryModel.url)
                                }
                            }
                        }
                    }
                    changeCountChooseImage()
                } else {
                    videoAdapter.run {
                        videos.mapIndexed { index, videoGallery ->
                            if (videoGallery.url == url) {
                                videoGallery.isChoose = isChoose
                                notifyItemChanged(index)
                                if (isChoose) {
                                    chooseVideo.addChooseMedia(
                                        ChooseMedia(
                                            url = videoGallery.url,
                                            name = videoGallery.url.substringAfterLast("/"),
                                            urlPreview = urlPreview,
                                            duration = videoGallery.duration,
                                            type = MediaType.IMAGE
                                        )
                                    )
                                } else {
                                    chooseVideo.removeChooseMedia(videoGallery.url)
                                }
                            }
                        }
                    }
                    changeCountChooseVideo()
                }
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mediaRecyclerView = view.findViewById(R.id.mediaRecyclerView)
        icAttachFile = view.findViewById(R.id.icAttachFile)
        icEditText = view.findViewById(R.id.icEditText)
        icEditColor = view.findViewById(R.id.icEditColor)
        icEditAlign = view.findViewById(R.id.icEditAlign)
        panelAddFile = view.findViewById(R.id.panelAddFile)
        galleryButton = view.findViewById(R.id.galleryButton)
        musicButton = view.findViewById(R.id.musicButton)
        videoButton = view.findViewById(R.id.videoButton)
        playlistButton = view.findViewById(R.id.playlistButton)
        btnAdd = view.findViewById(R.id.btnAdd)
        amountFiles = view.findViewById(R.id.amountFiles)
        horizontalGuideCenter = view.findViewById(R.id.horizontal_guide_center)
        horizontalGuideEnd = view.findViewById(R.id.horizontal_guide_end)
        mediaRecyclerView.run {
            addItemDecoration(
                ItemOffsetDecoration(
                    context,
                    R.dimen.lb_page_indicator_arrow_shadow_offset
                )
            )
        }
        setUpAddFilePanel()
    }

    private fun setUpAddFilePanel() {

        icEditText.run {
            setOnClickListener {
                if (isActivated) {
                    activated(false)
                    gonePanelStyleText()
                } else {
                    activated(true)
                    showPanelStyleText()
                }
            }
        }

        icEditColor.run {
            colorAdapter.colors.addAll(addLocalMediaGateway.addColors())
            setOnClickListener {
                changeStateToHalfExpanded()
                hideKeyboard()
                if (isActivated) {
                    endChooseColorText()
                } else {
                    startChooseColorText()
                }
            }
        }

        icEditAlign.run {
            setOnClickListener {
                if (isActivated) {
                    activated(false)
                    gonePanelGravityText()
                } else {
                    activated(true)
                    showPanelGravityText()
                }
            }
        }

        icAttachFile.run {
            setOnClickListener {
                requestPermission()
                hideKeyboard()
                changeStateToHalfExpanded()
                if (isActivated) {
                    activated(false)
                    attachFileNotActivated()
                    galleryButton.changeActivatedTextView(
                        false, musicButton,
                        videoButton, playlistButton
                    )
                    galleryAdapter.photos.cancelChoose()
                    videoAdapter.videos.cancelChoose()
                    audioAdapter.audios.cancelChoose()
                } else {
                    activated(true)
                    attachFileActivated()
                }

            }
        }

        galleryButton.setOnClickListener {
            mediaRecyclerView.run {
                adapter = galleryAdapter.apply {
                    choosePhoto.clear()
                }
                layoutManager = GridLayoutManager(context, 3)
                setPadding(recyclerPadding, 0, recyclerPadding, 0)
            }
            setVisibilityForAddFiles()
            (it as TextView).changeActivatedTextView(
                true, musicButton, videoButton,
                playlistButton
            )
            changeCountChooseImage()
        }

        musicButton.setOnClickListener {
            mediaRecyclerView.run {
                adapter = audioAdapter.apply {
                    chooseAudio.clear()
                }
                layoutManager = LinearLayoutManager(context)
                setPadding(0, 0, 0, 0)
            }
            setVisibilityForAddFiles()
            (it as TextView).changeActivatedTextView(
                true, galleryButton, videoButton,
                playlistButton
            )
            changeCountChooseAudio()
        }

        videoButton.setOnClickListener {
            mediaRecyclerView.run {
                adapter = videoAdapter.apply {
                    chooseVideo.clear()
                }
                layoutManager = GridLayoutManager(context, 3)
                setPadding(recyclerPadding, 0, recyclerPadding, 0)
            }
            setVisibilityForAddFiles()
            (it as TextView).changeActivatedTextView(
                true, galleryButton, musicButton,
                playlistButton
            )
            changeCountChooseVideo()
        }

        btnAdd.setOnClickListener {
            when (mediaRecyclerView.adapter) {
                is MediaAdapter.GalleryAdapter -> {
                    galleryAdapter.run {
                        attachGallery()
                        photos.forEachIndexed { index, galleryModel ->
                            if (galleryModel.isChoose) {
                                galleryModel.isChoose = false
                                notifyItemChanged(index)
                            }
                        }
                    }
                    changeCountChooseImage()
                }
                is MediaAdapter.VideoAdapter -> {
                    videoAdapter.run {
                        attachVideo()
                        videos.forEachIndexed { index, videoModel ->
                            if (videoModel.isChoose) {
                                videoModel.isChoose = false
                                notifyItemChanged(index)
                            }
                        }
                    }
                    changeCountChooseVideo()
                }
                is MediaAdapter.AudioAdapter -> {
                    audioAdapter.run {
                        attachAudio()
                        audios.forEachIndexed { index, audioInAddFileModel ->
                            if (audioInAddFileModel.isChoose) {
                                audioInAddFileModel.isChoose = false
                                notifyItemChanged(index)
                            }
                        }
                    }
                    changeCountChooseAudio()
                }
            }
            galleryButton.changeActivatedTextView(false, videoButton, musicButton, playlistButton)
            changeStateViewAfterAddMedia()
        }

        playlistButton.setOnClickListener {
            Toast.makeText(requireContext(), "Пока недоступно", Toast.LENGTH_SHORT).show()
        }
    }

    abstract fun gonePanelStyleText()

    protected open fun showPanelStyleText() {
        icEditAlign.activated(false)
        goneViewWhenShowPanelStyleText()
    }

    abstract fun gonePanelGravityText()

    protected open fun showPanelGravityText() {
        icEditText.activated(false)
        goneViewWhenShowPanelStyleText()
    }

    private fun goneViewWhenShowPanelStyleText() {
        mediaRecyclerView.gone()
        panelAddFile.gone()
        amountFiles.gone()
        btnAdd.gone()
        icAttachFile.activated(false)
        galleryButton.changeActivatedTextView(false, musicButton, videoButton, playlistButton)
    }

    abstract fun calculateHeight(): Int

    override fun changeTextColor(color: Int) {
        icEditColor.setImageDrawable(colorDrawableGateway.getDrawableByColor(color))
    }

    protected open fun startChooseColorText() {
        icEditColor.activated(true)
        changeStateToHalfExpanded()
        icEditText.activated(false)
        icEditAlign.activated(false)
        panelAddFile.gone()
        amountFiles.gone()
        btnAdd.gone()
        galleryButton.changeActivatedTextView(false, musicButton, videoButton, playlistButton)
        icAttachFile.activated(false)
        mediaRecyclerView.run {
            show()
            adapter = colorAdapter
            layoutManager = GridLayoutManager(context, 8)
            setPadding(0, 0, 0, 0)
        }
    }

    protected open fun endChooseColorText() {
        mediaRecyclerView.gone()
        icEditColor.activated(false)
    }

    abstract fun attachFileNotActivated()

    protected open fun attachFileActivated() {
        icEditText.activated(false)
        icEditAlign.activated(false)
        mediaRecyclerView.gone()
        icEditColor.activated(false)
    }

    abstract fun attachGallery()

    abstract fun attachVideo()

    abstract fun attachAudio()

    abstract fun attachFromCamera()

    abstract fun changeStateToHalfExpanded()

    abstract fun changeStateViewAfterAddMedia()

    abstract fun deleteMediaFromEditor(chooseMedia: ChooseMedia)

    @SuppressLint("CheckResult")
    protected fun requestPermission() {
        permissions.request(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).subscribe({
            if (it) {
                if (galleryAdapter.photos.size == 1) {
                    settingAdapter()
                }
            }
        }, { Timber.e(it) }
        )
    }

    private fun setVisibilityForAddFiles() {
        mediaRecyclerView.show()
        amountFiles.show()
        btnAdd.show()
    }

    private fun settingAdapter() {
        galleryAdapter.run {
            photos.addAll(addLocalMediaGateway.addGalleryUri())
        }
        audioAdapter.run {
            audios.addAll(addLocalMediaGateway.addAudioUri())
        }
        videoAdapter.run {
            videos.addAll(addLocalMediaGateway.addVideoUri())
        }
    }

    fun changeState(state: Int) {
        currentState = state
        when (state) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                stateExpanded()
            }
            BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                stateHalfExpanded()
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                stateCollapsed()
            }
            BottomSheetBehavior.STATE_HIDDEN -> {
                stateHidden()
            }
            BottomSheetBehavior.STATE_DRAGGING -> {
                stateDragging()
            }
            BottomSheetBehavior.STATE_SETTLING -> {
                stateSettling()
            }
        }
    }

    abstract fun stateSettling()

    abstract fun stateExpanded()
    abstract fun stateHalfExpanded()

    abstract fun stateDragging()

    abstract fun stateHidden()

    protected open fun stateCollapsed() {
        galleryAdapter.photos.cancelChoose()
        videoAdapter.videos.cancelChoose()
        audioAdapter.audios.cancelChoose()
        mediaRecyclerView.gone()
        btnAdd.gone()
        amountFiles.gone()
        galleryButton.changeActivatedTextView(false, musicButton, videoButton, playlistButton)
        icAttachFile.activated(false)
        btnAdd.isEnabled = false
    }

    protected open fun changeBottomConstraintForView(id: Int) {
        val params = mediaRecyclerView.layoutParams as ConstraintLayout.LayoutParams
        params.bottomToTop = id
        mediaRecyclerView.layoutParams = params
    }

    class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {
        constructor(
            context: Context,
            @DimenRes itemOffsetId: Int
        ) : this(context.resources.getDimensionPixelSize(itemOffsetId))


        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset)
        }
    }

    @SuppressLint("CheckResult")
    override fun changeCountChooseImage() {
        Single.just(galleryAdapter.photos.countChoose()).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { count ->
                if (count > 0) {
                    btnAdd.isEnabled = true
                    amountFiles.text = when (count) {
                        1 -> context?.getString(R.string.choose_one_photo)
                        in 2..4 -> context?.getString(R.string.choose_two_photo, count.toString())
                        else -> context?.getString(R.string.choose_five_photo, count.toString())
                    }
                } else {
                    btnAdd.isEnabled = false
                    amountFiles.text = context?.getString(R.string.choose_photo)
                }
            }
    }

    @SuppressLint("CheckResult")
    override fun changeCountChooseVideo() {
        Single.just(videoAdapter.videos.countChoose()).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { count ->
                if (count > 0) {
                    btnAdd.isEnabled = true
                    amountFiles.text =
                        context?.getString(R.string.choose_count_video, count.toString())
                } else {
                    btnAdd.isEnabled = false
                    amountFiles.text = context?.getString(R.string.choose_video)
                }
            }
    }

    @SuppressLint("CheckResult")
    override fun changeCountChooseAudio() {
        Single.just(audioAdapter.audios.countChoose()).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { count ->
                if (count > 0) {
                    btnAdd.isEnabled = true
                    amountFiles.text =
                        context?.getString(R.string.choose_count_audio, count.toString())
                } else {
                    btnAdd.isEnabled = false
                    amountFiles.text = context?.getString(R.string.choose_audio)
                }
            }
    }

    override fun attachPhoto() {
        attachFromCamera()
        changeStateViewAfterAddMedia()
        galleryButton.changeActivatedTextView(false, videoButton, musicButton, playlistButton)
    }
}
