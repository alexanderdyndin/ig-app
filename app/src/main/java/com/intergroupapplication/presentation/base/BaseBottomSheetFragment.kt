package com.intergroupapplication.presentation.base

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
import androidx.annotation.DimenRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.AudioInAddFileModel
import com.intergroupapplication.data.model.GalleryModel
import com.intergroupapplication.data.model.VideoModel
import com.intergroupapplication.domain.gateway.AddLocalMediaGateway
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.view.PreviewDialog
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

abstract class BaseBottomSheetFragment:BaseFragment(),MediaCallback,ImageUploadingView{


    @Inject
    lateinit var addLocalMediaGateway: AddLocalMediaGateway

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var galleryAdapter: GalleryAdapter

    @Inject
    lateinit var audioAdapter: AudioAdapter

    @Inject
    lateinit var videoAdapter: VideoAdapter

    @Inject
    lateinit var playlistAdapter: PlaylistAdapter

    @NotEmpty(messageResId = R.string.comment_should_contain_text)
    lateinit var commentEditText: AppCompatEditText

    private val permissions by lazy { RxPermissions(this) }

    lateinit var mediaRecyclerView:RecyclerView
    lateinit var icAttachFile:ImageView
    lateinit var galleryButton:TextView
    lateinit var musicButton: TextView
    lateinit var videoButton: TextView
    lateinit var playlistButton: TextView
    lateinit var btnAdd: TextView
    lateinit var amountFiles: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mediaRecyclerView = view.findViewById(R.id.mediaRecyclerView)
        icAttachFile = view.findViewById(R.id.icAttachFile)
        galleryButton = view.findViewById(R.id.galleryButton)
        musicButton = view.findViewById(R.id.musicButton)
        videoButton = view.findViewById(R.id.videoButton)
        playlistButton = view.findViewById(R.id.playlistButton)
        btnAdd = view.findViewById(R.id.btnAdd)
        amountFiles = view.findViewById(R.id.amountFiles)
        mediaRecyclerView.apply {
            addItemDecoration(ItemOffsetDecoration(context, R.dimen.lb_page_indicator_arrow_shadow_offset))
        }
        childFragmentManager.setFragmentResultListener(PreviewDialog.ADD_REQUEST_CODE, this) { _, bundle ->
            val isPhoto = bundle.getBoolean(PreviewDialog.IS_PHOTO_KEY)
            val result = bundle.getString(PreviewDialog.ADD_URI_KEY)
            val isChoose = bundle.getBoolean(PreviewDialog.IS_CHOOSE_KEY)
            result?.let { result->
                if (isPhoto){
                    galleryAdapter.photos.mapIndexed { index, galleryModel ->
                        if (galleryModel.url == result){
                            galleryModel.isChoose = isChoose
                            galleryAdapter.notifyItemChanged(index)
                        }
                    }
                    changeCountChooseImage()
                }
                else{
                    videoAdapter.videos.mapIndexed { index, videoGallery->
                        if (videoGallery.url == result){
                            videoGallery.isChoose = isChoose
                            videoAdapter.notifyItemChanged(index)
                        }
                    }
                    changeCountChooseVideo()
                }
            }
        }
        setUpAddFilePanel()
    }


    protected fun convertDpToPixel(dp: Int): Int {
        val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return px.roundToInt()
    }

    protected fun convertPixelToDp(pixel: Int): Int {
        val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
        val dp = pixel/ (metrics.densityDpi / 160f)
        return dp.roundToInt()
    }

    private fun setUpAddFilePanel() {
        icAttachFile.apply {
            setOnClickListener {
                requestPermission()
                closeKeyboard()
                changeStateWhenAttachFile()
                if (isActivated) {
                    activated(false)
                    attachFileNotActivated()
                    galleryButton.changeActivated(false, musicButton, videoButton, playlistButton)
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
            mediaRecyclerView.apply {
                adapter = galleryAdapter
                layoutManager = GridLayoutManager(context, 3)
            }
            setVisibilityForAddFiles()
            (it as TextView).changeActivated(true, musicButton, videoButton, playlistButton)
            changeCountChooseImage()
        }

        musicButton.setOnClickListener {
            mediaRecyclerView.apply {
                adapter = audioAdapter
                layoutManager = LinearLayoutManager(context)
            }
            setVisibilityForAddFiles()
            (it as TextView).changeActivated(true, galleryButton, videoButton, playlistButton)
            changeCountChooseAudio()
        }

        videoButton.setOnClickListener {
            mediaRecyclerView.apply {
                adapter = videoAdapter
                layoutManager = GridLayoutManager(context, 3)
            }
            setVisibilityForAddFiles()
            (it as TextView).changeActivated(true, galleryButton, musicButton, playlistButton)
            changeCountChooseVideo()
        }

        btnAdd.setOnClickListener {
            when (mediaRecyclerView.adapter) {
                is GalleryAdapter -> {
                    galleryAdapter.apply {
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
                is VideoAdapter -> {
                    videoAdapter.apply {
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
                is AudioAdapter -> {
                    audioAdapter.apply {
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
            galleryButton.changeActivated(false, videoButton, musicButton, playlistButton)
            changeStateViewAfterAddMedia()
        }

        playlistButton.setOnClickListener {
            Toast.makeText(requireContext(), "Пока недоступно", Toast.LENGTH_SHORT).show()
        }
    }

    abstract fun attachFileNotActivated()

    abstract fun attachFileActivated()

    abstract fun attachGallery()

    abstract fun attachVideo()

    abstract fun attachAudio()

    abstract fun attachFromCamera()

    abstract fun changeStateWhenAttachFile()

    abstract fun changeStateViewAfterAddMedia()

    abstract fun closeKeyboard()

    @SuppressLint("CheckResult")
    private fun requestPermission() {
        permissions.request(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe({
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
        chooseMedias.clear()
        galleryAdapter.apply {
            photos.addAll(addGalleryUri())
        }
        audioAdapter.apply {
            audios.addAll(addAudioUri())
        }
        videoAdapter.apply {
            videos.addAll(addVideoUri())
        }
    }

    private fun addGalleryUri():MutableList<GalleryModel> {
       return addLocalMediaGateway.addGalleryUri()
    }

    private fun addVideoUri():MutableList<VideoModel>{
      return addLocalMediaGateway.addVideoUri()
    }

    private fun addAudioUri():MutableList<AudioInAddFileModel>{
       return addLocalMediaGateway.addAudioUri()
    }

    fun changeState(state: Int){
        when(state){
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
        galleryButton.changeActivated(false, musicButton, videoButton, playlistButton)
        icAttachFile.activated(false)
        btnAdd.isEnabled = false
        Timber.tag("tut_state").d("_COLLAPSED")
    }

    protected fun changeBottomConstraintForRecyclerView(id:Int) {
        val params = mediaRecyclerView.layoutParams as ConstraintLayout.LayoutParams
        params.bottomToTop = id
        mediaRecyclerView.layoutParams = params
    }

    class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {
        constructor(context: Context, @DimenRes itemOffsetId: Int) : this(context.resources.getDimensionPixelSize(itemOffsetId))


        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset)
        }
    }

    @SuppressLint("CheckResult")
    override fun changeCountChooseImage() {
        Single.just(galleryAdapter.photos.countChoose()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { count->
                    if (count>0){
                        btnAdd.isEnabled = true
                        val text = when(count) {
                            1 -> "Выбрано $count фотография"
                            in 2..4 -> "Выбрано $count фотографии"
                            else -> "Выбрано $count фотографий"
                        }
                        amountFiles.text =text
                    }
                    else{
                        btnAdd.isEnabled = false
                        amountFiles.text ="Выберите фото"
                    }
                }
    }

    @SuppressLint("CheckResult")
    override fun changeCountChooseVideo() {
        Single.just(videoAdapter.videos.countChoose()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { count->
                    if (count>0){
                        btnAdd.isEnabled = true
                        val text = "Выбрано $count видео"
                        amountFiles.text =text
                    }
                    else{
                        btnAdd.isEnabled = false
                        amountFiles.text ="Выберите видео"
                    }
                }
    }

    @SuppressLint("CheckResult")
    override fun changeCountChooseAudio() {
        Single.just(audioAdapter.audios.countChoose()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { count->
                    if (count>0){
                        btnAdd.isEnabled = true
                        val text = "Выбрано $count аудио"
                        amountFiles.text =text
                    }
                    else{
                        btnAdd.isEnabled = false
                        amountFiles.text ="Выберите аудио"
                    }
                }
    }

    override fun attachPhoto() {
        attachFromCamera()
        changeStateViewAfterAddMedia()
    }

}
