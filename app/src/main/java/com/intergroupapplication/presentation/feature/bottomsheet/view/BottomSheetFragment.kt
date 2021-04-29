package com.intergroupapplication.presentation.feature.bottomsheet.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.Scroller
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.AudioInAddFileModel
import com.intergroupapplication.data.model.GalleryModel
import com.intergroupapplication.data.model.VideoModel
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.bottomsheet.adapter.*
import com.intergroupapplication.presentation.feature.bottomsheet.presenter.BottomSheetPresenter
import com.intergroupapplication.presentation.listeners.RightDrawableListener
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*
import kotlinx.android.synthetic.main.layout_attach_image.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt


class BottomSheetFragment: BaseFragment(), BottomSheetView, Validator.ValidationListener,
            MediaCallback{

    lateinit var callback: Callback

    @Inject
    lateinit var imageLoadingDelegate:ImageLoadingDelegate

    @Inject
    lateinit var validator: Validator

    @Inject
    lateinit var rightDrawableListener: RightDrawableListener

    @Inject
    lateinit var galleryAdapter: GalleryAdapter

    @Inject
    lateinit var audioAdapter: AudioAdapter

    @Inject
    lateinit var videoAdapter: VideoAdapter

    @Inject
    lateinit var playlistAdapter: PlaylistAdapter

    @Inject
    @InjectPresenter
    lateinit var presenter: BottomSheetPresenter

   // @Inject
   // lateinit var modelFactory: ViewModelProvider.Factory

    @NotEmpty(messageResId = R.string.comment_should_contain_text)
    lateinit var commentEditText: AppCompatEditText

    private val heightView by lazy {convertDpToPixel(100)}
    private val heightEditTextWithFiveLine by lazy {convertDpToPixel(156)}
    private val heightEditText by lazy { convertDpToPixel(53) }
    private val heightLineInEditText by lazy { convertDpToPixel(16) }

    private val permissions by lazy { RxPermissions(this) }

    @ProvidePresenter
    fun providePresenter(): BottomSheetPresenter = presenter

    private val loadingViews: MutableMap<String, View?> = mutableMapOf()

    override fun layoutRes() = R.layout.fragment_bottom_sheet

    override fun getSnackBarCoordinator() = bottom_sheet_coordinator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        commentEditText = view.findViewById(R.id.commentEditText)
        mediaRecyclerView.apply {
            addItemDecoration(ItemOffsetDecoration(context, R.dimen.lb_page_indicator_arrow_shadow_offset))
        }
        prepareEditText()
        controlCommentEditTextChanges()
        setUpAddFilePanel()
    }

    private fun convertDpToPixel(dp: Int): Int {
        val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return px.roundToInt()
    }

    private fun convertPixelToDp(pixel: Int): Int {
        val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
        val dp = pixel/ (metrics.densityDpi / 160f)
        return dp.roundToInt()
    }

    private fun setUpAddFilePanel() {
        icAttachFile.apply {
            setOnClickListener {
                requestPermission()
                closeKeyboard()
                if (callback.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    callback.changeStateBottomSheet(BottomSheetBehavior.STATE_HALF_EXPANDED)
                }
                if (isActivated) {
                    activated(false)
                    commentEditText.show()
                    panelAddFile.gone()
                    btnAdd.gone()
                    amountFiles.gone()
                    postContainer.show()
                    galleryButton.changeActivated(false, musicButton, videoButton, playlistButton)
                    galleryAdapter.photos.cancelChoose()
                    videoAdapter.videos.cancelChoose()
                    audioAdapter.audios.cancelChoose()
                    mediaRecyclerView.gone()
                } else {
                    activated(true)
                    postContainer.gone()
                    commentEditText.gone()
                    panelAddFile.show()
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

        amountFiles.setOnClickListener {

        }

        btnAdd.setOnClickListener {
            when (mediaRecyclerView.adapter) {
                is GalleryAdapter -> {
                    galleryAdapter.apply {
                        presenter.attachMedia(getChoosePhotosFromObservable(), presenter::loadImage,
                                loadingViews)
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
                        presenter.attachMedia(getChooseVideosFromObservable(), presenter::loadVideo,
                                loadingViews)
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
                        presenter.attachMedia(getChooseAudiosFromObservable(), presenter::loadAudio,
                                loadingViews)
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
            mediaRecyclerView.gone()
            icAttachFile.activated(false)
            panelAddFile.gone()
            postContainer.show()
            amountFiles.gone()
            it.gone()
            commentEditText.show()
        }

        playlistButton.setOnClickListener {
            Toast.makeText(requireContext(), "Пока недоступно", Toast.LENGTH_SHORT).show()
        }
    }

    private fun closeKeyboard() {
        try {
            val imm: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(commentEditText.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("CheckResult")
    private fun requestPermission() {
        permissions.request(Manifest.permission.READ_EXTERNAL_STORAGE).subscribe({
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
        chooseMedia.clear()
        galleryAdapter.apply {
            photos.addAll(addGalleryUri(MediaStore.Images.ImageColumns.DATA,
                    uriConstants = MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        }
        audioAdapter.apply {
            audios.addAll(addAudioUri(MediaStore.Audio.AudioColumns.DATA,
                    MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                    MediaStore.Audio.AudioColumns.DURATION,
                    uriConstants = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI))
        }
        videoAdapter.apply {
            videos.addAll(addVideoUri(MediaStore.Video.VideoColumns.DATA,
                    MediaStore.Video.VideoColumns.DURATION,
                    uriConstants = MediaStore.Video.Media.EXTERNAL_CONTENT_URI))
        }
    }

    private fun addGalleryUri(mediaConstants: String, uriConstants: Uri):MutableList<GalleryModel> {
        val listUrlImage = mutableListOf<GalleryModel>()
        try {
            val cursor = context?.contentResolver?.query(uriConstants,
                    arrayOf(mediaConstants),
                    null,
                    null,
                    null)
            val size: Int = cursor?.count ?: 0
            if (size != 0) {
                while (cursor?.moveToNext() == true) {
                    val fileColumnIndex: Int = cursor.getColumnIndexOrThrow(mediaConstants)
                    val path: String = cursor.getString(fileColumnIndex)
                    listUrlImage.add(GalleryModel(path, false))
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        listUrlImage.reverse()
        return listUrlImage
    }

    private fun addVideoUri(vararg mediaConstants: String, uriConstants: Uri):MutableList<VideoModel>{
        val listUrlImage = mutableListOf<VideoModel>()
        try {
            val cursor = context?.contentResolver?.query(uriConstants,
                    mediaConstants,
                    null,
                    null,
                    null)
            val size: Int = cursor?.count ?: 0
            if (size != 0) {
                while (cursor?.moveToNext() == true) {
                    val fileColumnIndex: Int = cursor.getColumnIndexOrThrow(mediaConstants[0])
                    val path: String = cursor.getString(fileColumnIndex)
                    val duration = cursor.getLong(cursor.getColumnIndexOrThrow(mediaConstants[1]))
                    val calendar = GregorianCalendar()
                    calendar.time = Date(duration)
                    val seconds = calendar.get(GregorianCalendar.SECOND)
                    val minutes = calendar.get(GregorianCalendar.MINUTE)
                    val minute = if (minutes < 10) "0$minutes" else "$minutes"
                    val second = if (seconds < 10) "0$seconds" else "$seconds"
                    listUrlImage.add(VideoModel(path, "$minute:$second", false))
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        listUrlImage.reverse()
        return listUrlImage
    }

    private fun addAudioUri(vararg mediaConstants: String, uriConstants: Uri)
            :MutableList<AudioInAddFileModel>{
        val listUrlAudio = mutableListOf<AudioInAddFileModel>()
        try {
            val cursor = context?.contentResolver?.query(uriConstants,
                    mediaConstants,
                    null,
                    null,
                    null)
            val size: Int = cursor?.count ?: 0
            if (size != 0) {
                while (cursor?.moveToNext() == true) {
                    val fileColumnIndex: Int = cursor.getColumnIndexOrThrow(mediaConstants[0])
                    val nameColumnIndex: Int = cursor.getColumnIndexOrThrow(mediaConstants[1])
                    val duration = cursor.getLong(cursor.getColumnIndexOrThrow(mediaConstants[2]))
                    val path: String = cursor.getString(fileColumnIndex)
                    val name = (cursor.getString(nameColumnIndex) ?: "").replace(".mp3", "")
                    val calendar = GregorianCalendar()
                    calendar.time = Date(duration)
                    val seconds = calendar.get(GregorianCalendar.SECOND)
                    val minutes = calendar.get(GregorianCalendar.MINUTE)
                    val minute = if (minutes < 10) "0$minutes" else "$minutes"
                    val second = if (seconds < 10) "0$seconds" else "$seconds"
                    listUrlAudio.add(AudioInAddFileModel(path, name, "$minute:$second", false))
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        listUrlAudio.reverse()
        return listUrlAudio
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

    private fun stateSettling() {
        Timber.tag("tut_state").d("SETTING")
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
    }

    private fun stateExpanded() {
        //TODO убрать максимальное количество линий или придумать как сделать их нормально
        commentEditText.maxLines = 40
        Timber.tag("tut_state").d("EXPANDED")
    }

    private fun stateHalfExpanded() {
        //TODO убрать максимальное количество линий или придумать как сделать их нормально
        commentEditText.maxLines = 20
        Timber.tag("tut_state").d("HALF_EXPANDED")
    }

    private fun stateDragging() {
        Timber.tag("tut_state").d("DRAGGING")
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
    }

    private fun stateHidden() {
        Timber.tag("tut_state").d("HIDDEN")
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
    }

    private fun stateCollapsed() {
        commentEditText.maxLines = 5
        var height = if (commentEditText.lineCount > 5) {
            heightEditTextWithFiveLine
        } else {
            commentEditText.height + iconPanel.height + pushUpDown.height / 2
        }
        if (loadingViews.isNotEmpty()) {
            height += heightView
        }
        panelAddFile.gone()
        commentEditText.show()
        postContainer.show()
        mediaRecyclerView.gone()
        galleryButton.changeActivated(false, musicButton, videoButton, playlistButton)
        galleryAdapter.run {
            photos.cancelChoose()
        }
        videoAdapter.run{
            videos.cancelChoose()
        }
        audioAdapter.run {
            audios.cancelChoose()
        }
        btnAdd.gone()
        btnAdd.isEnabled=false
        amountFiles.gone()
        callback.addHeightContainer(height)
        icAttachFile.activated(false)
        Timber.tag("tut_state").d("_COLLAPSED")
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_down)
        chooseMedia.clear()
        chooseMedia.addAll(loadingViews.keys)
    }


    private fun controlCommentEditTextChanges() {
        RxTextView.afterTextChangeEvents(commentEditText)
                .subscribe {
                    val view = it.view()
                    if (view.text.trim().isEmpty() && loadingViews.isEmpty()) {
                        view.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                null, null)
                    } else {
                        view.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                ContextCompat.getDrawable(requireContext(), R.drawable.ic_send), null)
                    }

                    if (commentEditText.isFocused) {
                        var height = if (commentEditText.lineCount<=5)iconPanel.height + pushUpDown.height / 2 + heightEditText + heightLineInEditText * (commentEditText.lineCount - 1)
                            else heightEditTextWithFiveLine
                        if (loadingViews.isNotEmpty()) {
                            height += heightView
                        }
                        callback.addHeightContainer(height)
                    }
                }
                .let(compositeDisposable::add)
        setUpRightDrawableListener()
    }

    private fun setUpRightDrawableListener() {
        commentEditText.setOnTouchListener(rightDrawableListener)
        rightDrawableListener.clickListener = {
            validator.validate()
            loadingViews.clear()
            chooseMedia.clear()
            postContainer.removeAllViews()
            commentEditText.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    null, null)
        }
    }

    override fun commentCreated(commentEntity: CommentEntity) {
        callback.commentCreated(commentEntity)
    }

    override fun answerToCommentCreated(commentEntity: CommentEntity) {
       callback.answerToCommentCreated(commentEntity)
    }

    override fun showCommentUploading(show: Boolean) {
        callback.showCommentUploading(show)
    }

    override fun hideSwipeLayout() {
       callback.hideSwipeLayout()
    }

    override fun showImageUploadingStarted(path: String) {
        loadingViews[path] = layoutInflater.inflate(R.layout.layout_attach_image, postContainer, false)
        loadingViews[path]?.let {
            it.imagePreview?.let { draweeView ->
                val type = MimeTypeMap.getFileExtensionFromUrl(path)
                val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(type) ?: ""
                if (mime in listOf("audio/mpeg", "audio/aac", "audio/wav")) {
                    imageLoadingDelegate.loadImageFromResources(R.drawable.variant_10, draweeView)
                    it.nameView?.text = path.substring(path.lastIndexOf("/") + 1)
                }
                else
                    imageLoadingDelegate.loadImageFromFile(path, draweeView)
            }
        }

        postContainer.addView(loadingViews[path])
        prepareListeners(loadingViews[path], path)
        imageUploadingStarted(loadingViews[path])
    }

    override fun showImageUploaded(path: String) {
        loadingViews[path]?.apply {
            darkCard?.hide()
            stopUploading?.hide()
            imageUploadingProgressBar?.hide()
            detachImage?.show()
            commentEditText.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_send), null)
            setUpRightDrawableListener()
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


    private fun imageUploadingStarted(uploadingView: View?) {
        uploadingView?.apply {
            darkCard?.show()
            imageUploadingProgressBar?.show()
            stopUploading?.show()
            detachImage?.hide()
            refreshContainer?.hide()
        }
    }

    private fun prepareListeners(uploadingView: View?, path: String) {
        uploadingView?.apply {
            refreshContainer.setOnClickListener {
                this.imageUploadingProgressBar?.progress = 0f
                presenter.retryLoading(path)
                imageUploadingStarted(uploadingView)
            }
            stopUploading?.setOnClickListener {
                presenter.cancelUploading(path)
                detachImage(path)
            }
            detachImage?.setOnClickListener {
                presenter.removeContent(path)
                detachImage(path)
            }
        }
    }

    private fun detachImage(path: String) {
        postContainer.removeView(loadingViews[path])
        loadingViews.remove(path)
        chooseMedia.remove(path)
        if(loadingViews.isEmpty()){
            commentEditText.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    null, null)
            val height = commentEditText.height + iconPanel.height + pushUpDown.height/2
            callback.addHeightContainer(height)
        }
    }

    private fun prepareEditText() {
        commentEditText.isVerticalScrollBarEnabled = true
        commentEditText.maxLines = 5
        commentEditText.setScroller(Scroller(requireContext()))
        commentEditText.movementMethod = ScrollingMovementMethod()
    }

    override fun onValidationSucceeded() {
        callback.createComment(commentEditText.text.toString().trim(), presenter)
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>) {
        if (loadingViews.isNotEmpty()){
            callback.createComment(commentEditText.text.toString().trim(), presenter)
            return
        }
        for (error in errors) {
            val message = error.getCollatedErrorMessage(requireContext())
            dialogDelegate.showErrorSnackBar(message)
        }
    }

    class ItemOffsetDecoration(private val mItemOffset: Int) : ItemDecoration() {
        constructor(context: Context, @DimenRes itemOffsetId: Int) : this(context.resources.getDimensionPixelSize(itemOffsetId))


        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset)
        }
    }

    interface Callback{
        fun createComment(textComment: String, bottomPresenter: BottomSheetPresenter)
        fun changeStateBottomSheet(newState: Int)
        fun getState():Int
        fun addHeightContainer(height: Int)
        fun commentCreated(commentEntity: CommentEntity)
        fun answerToCommentCreated(commentEntity: CommentEntity)
        fun showCommentUploading(show: Boolean)
        fun hideSwipeLayout()
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

}