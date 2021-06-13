package com.intergroupapplication.presentation.feature.commentsbottomsheet.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.budiyev.android.circularprogressbar.CircularProgressBar
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.databinding.FragmentCommentBottomSheetBinding
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.base.BaseBottomSheetFragment
import com.intergroupapplication.presentation.customview.*
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.presenter.BottomSheetPresenter
import com.intergroupapplication.presentation.feature.commentsdetails.viewmodel.CommentsViewModel
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadAudioPlayerView
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadVideoPlayerView
import com.intergroupapplication.presentation.listeners.RightDrawableListener
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import javax.inject.Inject
//TODO создавать аудио, видео вьюхи прям тут и добавлять их в контейнер вручную, а уже внутри сетать player
class CommentBottomSheetFragment: BaseBottomSheetFragment(),BottomSheetView,Validator.ValidationListener{

    companion object{
        const val CREATE_COMMENT_DATA = 0
        const val CHANGE_STATE_BOTTOM_SHEET_DATA = 1
        const val ADD_HEIGHT_CONTAINER = 2
        const val COMMENT_CREATED_DATA = 3
        const val ANSWER_COMMENT_CREATED_DATA = 4
        const val SHOW_COMMENT_UPLOADING_DATA = 5
        const val HIDE_SWIPE_DATA = 6
    }

    private val viewBinding by viewBinding(FragmentCommentBottomSheetBinding::bind)

    @Inject
    @InjectPresenter
    lateinit var presenter: BottomSheetPresenter

    @Inject
    lateinit var validator: Validator

    @Inject
    lateinit var rightDrawableListener: RightDrawableListener

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: CommentsViewModel

    private val loadingViews: MutableMap<String, View?> = mutableMapOf()

    private val heightEditTextWithFiveLine by lazy { requireContext().dpToPx(180) }
    private val heightEditText by lazy { requireContext().dpToPx(53) }
    private val heightLineInEditText by lazy { requireContext().dpToPx(16) }
    private val heightAnswerPanel by lazy { requireContext().dpToPx(35) }

    @ProvidePresenter
    fun providePresenter(): BottomSheetPresenter = presenter

    override fun layoutRes() = R.layout.fragment_comment_bottom_sheet

    override fun getSnackBarCoordinator() = viewBinding.bottomSheetCoordinator

    private lateinit var createCommentCustomView:CreatePostCustomView
    private lateinit var iconPanel:LinearLayout
    private lateinit var pushUpDown:Button
    private lateinit var answerLayout:LinearLayout
    private lateinit var panelAddFile:LinearLayout
    private lateinit var responseToUser:TextView
    private lateinit var textAnswer:TextView
    private lateinit var horizontalGuideCenter:LinearLayout
    private lateinit var horizontalGuideEnd:LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, modelFactory)[CommentsViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        createCommentCustomView = viewBinding.createCommentCustomView
        iconPanel = viewBinding.iconPanel
        pushUpDown = viewBinding.pushUpDown
        answerLayout = viewBinding.answerLayout
        panelAddFile = viewBinding.panelAddFile
        responseToUser = viewBinding.responseToUser
        textAnswer = viewBinding.textAnswer
        horizontalGuideCenter = viewBinding.horizontalGuideCenter
        horizontalGuideEnd = viewBinding.horizontalGuideEnd
        createCommentCustomView.createAllMainView()
        createCommentCustomView.textPost.hint = requireContext().getString(R.string.write_your_comment)
        controlFirstCommentEditTextChanges()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun controlFirstCommentEditTextChanges() {
        RxTextView.afterTextChangeEvents( createCommentCustomView.listEditText[0])
                .subscribe {
                    val view = it.view()
                    if (view.text.trim().isEmpty() && loadingViews.isEmpty()) {
                        view.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                null, null)
                    } else {
                        view.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                ContextCompat.getDrawable(requireContext(), R.drawable.ic_send), null)
                    }
                    if (createCommentCustomView.listEditText[0].isFocused ) {
                        var height = if (createCommentCustomView.listEditText[0].lineCount<=5)iconPanel.height + pushUpDown.height / 2 + heightEditText + heightLineInEditText * createCommentCustomView.listEditText[0].lineCount
                        else heightEditTextWithFiveLine
                        if (answerLayout.isVisible())
                            height += heightAnswerPanel
                        CommentsViewModel.publishSubject.onNext(Pair(ADD_HEIGHT_CONTAINER, height))
                    }
                }
                .let(compositeDisposable::add)
        setUpRightDrawableListener()
    }

    private fun controlCommentEditTextChanges(){
        compositeDisposable.clear()
        RxTextView.afterTextChangeEvents(createCommentCustomView.textPost).subscribe {
            val view = it.view()
            if (createCommentCustomView.allTextIsEmpty() && loadingViews.isEmpty()) {
                view.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        null, null)
            } else {
                view.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_send), null)
            }
        }.let(compositeDisposable::add)
        createCommentCustomView.textPost.setCompoundDrawablesWithIntrinsicBounds(null, null,
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_send), null)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpRightDrawableListener() {
        rightDrawableListener.clickListener = {
            CommentsViewModel.publishSubject
                    .onNext(Pair(CREATE_COMMENT_DATA,Pair(createCommentCustomView.createFinalText(),
                        presenter)))
            loadingViews.clear()
            chooseMedias.clear()
            with(createCommentCustomView){
                removeAllBesidesFirstView()
                createCommentCustomView.textPost.hint = requireContext()
                    .getString(R.string.write_your_comment)
                controlFirstCommentEditTextChanges()
                /*textPost.run {
                    hint = requireContext()
                        .getString(R.string.write_your_comment)
                    setCompoundDrawablesWithIntrinsicBounds(null, null,
                        null, null)
            }*/
            }
        }
        createCommentCustomView.textPost.setOnTouchListener(rightDrawableListener)
    }

    fun answerComment(comment:CommentEntity.Comment){
        answerLayout.show()
        answerLayout.activated(true)
        responseToUser
                .apply {
                    text = comment.commentOwner?.firstName
                            ?: getString(R.string.unknown_user)
                }
        textAnswer.text = comment.text.substringBefore("~~")
        val height = createCommentCustomView.listEditText[0].height + iconPanel.height + pushUpDown.height / 2+ heightAnswerPanel
        CommentsViewModel.publishSubject.onNext(Pair(ADD_HEIGHT_CONTAINER, height))
    }

    override fun attachFileNotActivated() {
        createCommentCustomView.show()
        panelAddFile.gone()
        btnAdd.gone()
        amountFiles.gone()
        if (answerLayout.isActivated){
            answerLayout.show()
        }
        mediaRecyclerView.gone()
    }

    override fun attachFileActivated() {
        createCommentCustomView.gone()
        answerLayout.gone()
        panelAddFile.show()
    }

    override fun attachGallery() {
        presenter.attachMedia(galleryAdapter.getChoosePhotosFromObservable(), presenter::loadImage,
                loadingViews)
    }

    override fun attachVideo() {
        presenter.attachMedia(videoAdapter.getChooseVideosFromObservable(), presenter::loadVideo,
                loadingViews)
    }

    override fun attachAudio() {
        presenter.attachMedia(audioAdapter.getChooseAudiosFromObservable(), presenter::loadAudio,
                loadingViews)
    }

    override fun attachFromCamera() {
        presenter.attachFromCamera()
    }

    override fun changeStateWhenAttachFile() {
        CommentsViewModel.publishSubject
                .onNext(Pair(CHANGE_STATE_BOTTOM_SHEET_DATA, BottomSheetBehavior.STATE_HALF_EXPANDED))
    }

    override fun changeStateViewAfterAddMedia() {
        mediaRecyclerView.gone()
        icAttachFile.activated(false)
        panelAddFile.gone()
        amountFiles.gone()
        btnAdd.gone()
        createCommentCustomView.show()
    }

    override fun closeKeyboard() {
        try {
            createCommentCustomView.listEditText.forEach {editText->
                editText.dismissKeyboard()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stateSettling() {
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
        changeBottomConstraintForRecyclerView(horizontalGuideEnd.id)
    }

    override fun stateExpanded() {

    }

    override fun stateHalfExpanded() {
        changeBottomConstraintForRecyclerView(horizontalGuideCenter.id)
    }

    override fun stateDragging() {
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
        changeBottomConstraintForRecyclerView(horizontalGuideEnd.id)
    }

    override fun stateHidden() {
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
    }

    override fun stateCollapsed() {
        val height = restoreHeightEditText()
        restoreAllViewForCollapsedState()
        CommentsViewModel.publishSubject.onNext(Pair(ADD_HEIGHT_CONTAINER, height))
        chooseMedias.clear()
        chooseMedias.addAll(loadingViews.keys.map {
            ChooseMedia(it)
        })
        super.stateCollapsed()
    }

    override fun commentCreated(commentEntity: CommentEntity) {
        CommentsViewModel.publishSubject.onNext(Pair(COMMENT_CREATED_DATA, commentEntity))
        CommentsViewModel.publishSubject.onNext(
                Pair(ADD_HEIGHT_CONTAINER, heightEditText + iconPanel.height + pushUpDown.height / 2))
        CommentsViewModel.publishSubject.onNext(Pair(CHANGE_STATE_BOTTOM_SHEET_DATA,BottomSheetBehavior.STATE_COLLAPSED))
    }

    override fun answerToCommentCreated(commentEntity: CommentEntity) {
        CommentsViewModel.publishSubject.onNext(Pair(ANSWER_COMMENT_CREATED_DATA, commentEntity))
        CommentsViewModel.publishSubject.onNext(
                Pair(ADD_HEIGHT_CONTAINER, heightEditText + iconPanel.height + pushUpDown.height / 2))
        CommentsViewModel.publishSubject.onNext(Pair(CHANGE_STATE_BOTTOM_SHEET_DATA, BottomSheetBehavior.STATE_COLLAPSED))
        answerLayout.gone()
        answerLayout.activated(false)
    }

    override fun showCommentUploading(show: Boolean) {
        CommentsViewModel.publishSubject.onNext(Pair(SHOW_COMMENT_UPLOADING_DATA,show))
    }

    override fun showImageUploadingStarted(chooseMedia: ChooseMedia) {
        if (chooseMedia.url.contains(".mp3") || chooseMedia.url.contains(".mpeg")
                || chooseMedia.url.contains(".wav")){
            val audioEntity = AudioEntity(0,chooseMedia.url,false,"",
                chooseMedia.name,chooseMedia.authorMusic,"",0,0,
                chooseMedia.duration)
            loadingViews[chooseMedia.url] = createAudioPlayerView(audioEntity)
            createCommentCustomView.addMusic(audioEntity,loadingViews[chooseMedia.url] as DownloadAudioPlayerView)
        }
        else if (chooseMedia.url.contains(".jpeg") || chooseMedia.url.contains(".jpg")
                || chooseMedia.url.contains(".png")){
            val fileEntity = FileEntity(0,chooseMedia.url,false,"",
                chooseMedia.url.substringAfterLast("/"),0,0)
            loadingViews[chooseMedia.url] =
                //createCommentCustomView.imageContainer.createPic(fileEntity,imageLoadingDelegate)
                    createImageView(fileEntity)
            loadingViews[chooseMedia.url]?.let { createCommentCustomView.addImage(fileEntity, it) }
        }
        else {
            val fileEntity = FileEntity(0,chooseMedia.url,false,"",
                chooseMedia.url.substringAfterLast("/"),0,0,
                chooseMedia.urlPreview, chooseMedia.duration)
            loadingViews[chooseMedia.url] = createVideoPlayerView(fileEntity)
            createCommentCustomView.addVideo(fileEntity,
                loadingViews[chooseMedia.url] as DownloadVideoPlayerView)
        }
        createCommentCustomView.textPost.setCompoundDrawablesWithIntrinsicBounds(null, null,
               null, null)
        prepareListeners(loadingViews[chooseMedia.url], chooseMedia)
        imageUploadingStarted(loadingViews[chooseMedia.url])

        if (loadingViews.size == chooseMedias.size && createCommentCustomView.currentContainerIsLast()){
            createCommentCustomView.createAllMainView()
            controlCommentEditTextChanges()
        }
    }

    private fun createAudioPlayerView(audioEntity: AudioEntity): DownloadAudioPlayerView {
        return DownloadAudioPlayerView(requireContext()).apply {
            trackName = audioEntity.song
            trackOwner = "Загрузил (ID:${audioEntity.owner})"
            durationTrack.text = if (audioEntity.duration != "") audioEntity.duration else "00:00"
        }
    }

    private fun createImageView(fileEntity: FileEntity): View {
        val image = LayoutInflater.from(context).inflate(R.layout.layout_create_pic, null)
        val pic = image.findViewById<SimpleDraweeView>(R.id.imagePreview)
        imageLoadingDelegate.loadImageFromFile(fileEntity.file,pic)
        return image
    }

    private fun createVideoPlayerView(fileEntity: FileEntity): DownloadVideoPlayerView {
        return DownloadVideoPlayerView(requireContext()).apply {
            imageLoadingDelegate.loadImageFromFile(fileEntity.preview,previewForVideo)
            durationVideo.text = if (fileEntity.duration != "") fileEntity.duration else "00:00"
            nameVideo.text = fileEntity.title
        }
    }

    override fun showImageUploadingProgress(progress: Float, path: String) {
        loadingViews[path]?.apply {
            val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            imageUploadingProgressBar.progress = progress
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
        var countUpload = false
        loadingViews.values.forEach{ view ->
            val imageUploadingProgressBar = view
                ?.findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            if (imageUploadingProgressBar?.progress?:0.0f<100){
                countUpload = true
                return@forEach
            }
        }
        if (!countUpload || loadingViews.isNotEmpty()){
            createCommentCustomView.textPost.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_send), null)
            setUpRightDrawableListener()
        }
    }

    private fun prepareListeners(uploadingView: View?, chooseMedia: ChooseMedia) {
        uploadingView?.apply {
            val stopUploading = findViewById<ImageView>(R.id.stopUploading)
            val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            val detachImage = findViewById<ImageView>(R.id.detachImage)
            val refreshContainer = findViewById<LinearLayout>(R.id.refreshContainer)
            refreshContainer.setOnClickListener {
                imageUploadingProgressBar.progress = 0f
                presenter.retryLoading(chooseMedia)
                imageUploadingStarted(uploadingView)
            }
            stopUploading?.setOnClickListener {
                presenter.cancelUploading(chooseMedia.url)
                detachMedia(chooseMedia.url)
            }
            detachImage?.setOnClickListener {
                presenter.removeContent(chooseMedia.url)
                detachMedia(chooseMedia.url)
            }
        }
    }

    override fun hideSwipeLayout() {
        CommentsViewModel.publishSubject.onNext(Pair(HIDE_SWIPE_DATA, null))
    }

    private fun detachMedia(path: String) {
        loadingViews[path].let {view->
            when(view){
                is DownloadVideoPlayerView->{
                    view.exoPlayer.player?.pause()
                    createCommentCustomView.listVideoContainers.forEach { container->
                        container.removeVideoView(loadingViews[path])
                    }
                }
                is DownloadAudioPlayerView->{
                    view.exoPlayer.player?.pause()
                    createCommentCustomView.listAudioContainers.forEach {container->
                        container.removeAudioView(loadingViews[path])
                    }
                }
                is FrameLayout ->{
                    createCommentCustomView.listImageContainers.forEach {container->
                        container.removeImageView(loadingViews[path])
                    }
                }
            }
        }
        createCommentCustomView.deleteName(loadingViews[path])
        loadingViews.remove(path)
        chooseMedias.removeChooseMedia(path)
        if(loadingViews.isEmpty() && createCommentCustomView.allTextIsEmpty()){
            createCommentCustomView.textPost.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    null, null)
        }
    }

    private fun restoreHeightEditText(): Int {
        var height = if (createCommentCustomView.listEditText[0].lineCount > 5) {
            heightEditTextWithFiveLine
        } else {
            createCommentCustomView.listEditText[0].height + iconPanel.height + pushUpDown.height / 2
        }
        if(answerLayout.isVisible()){
            height += heightAnswerPanel
        }
        return height
    }

    private fun restoreAllViewForCollapsedState() {
        panelAddFile.gone()
        createCommentCustomView.show()
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_down)
    }

    override fun onValidationSucceeded() {

    }

    override fun onValidationFailed(errors: MutableList<ValidationError>?) {
    }
}