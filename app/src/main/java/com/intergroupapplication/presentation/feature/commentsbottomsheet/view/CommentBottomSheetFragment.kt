package com.intergroupapplication.presentation.feature.commentsbottomsheet.view

import android.graphics.Color
import android.graphics.Typeface.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.budiyev.android.circularprogressbar.CircularProgressBar
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.model.TextType
import com.intergroupapplication.databinding.FragmentCommentBottomSheetBinding
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.base.BaseBottomSheetFragment
import com.intergroupapplication.presentation.customview.*
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.presenter.CommentBottomSheetPresenter
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentsAdapter
import com.intergroupapplication.presentation.feature.commentsdetails.viewmodel.CommentsViewModel
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadAudioPlayerView
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadVideoPlayerView
import com.intergroupapplication.presentation.listeners.RightDrawableListener
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import java.util.*
import javax.inject.Inject

class CommentBottomSheetFragment: BaseBottomSheetFragment(),BottomSheetView{

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
    lateinit var presenter: CommentBottomSheetPresenter

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
    private val heightTextStylePanel by lazy { requireContext().dpToPx(39) }

    @ProvidePresenter
    fun providePresenter(): CommentBottomSheetPresenter = presenter

    override fun layoutRes() = R.layout.fragment_comment_bottom_sheet

    override fun getSnackBarCoordinator() = viewBinding.bottomSheetCoordinator

    private lateinit var richEditor: RichEditor
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
        parentFragmentManager.setFragmentResultListener(CommentsAdapter.EDIT_COMMENT_REQUEST, this,
            { _, result ->
                val comment: CommentEntity.Comment? = result
                                                    .getParcelable(CommentsAdapter.COMMENT_KEY)
                if (comment != null){
                    setupEditComment(comment)
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        richEditor = viewBinding.richEditor.apply {
            setEditorFontSize(18)
            val padding = context.dpToPx(4)
            setEditorPadding(padding, padding, padding, padding)
            setEditorFontColor(ContextCompat.getColor(view.context, R.color.whiteTextColor))
            setPlaceholder(context.getString(R.string.add_photo_or_text))
            setBackgroundColor(Color.parseColor("#12161E"))
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            decorationStateListener = object :RichEditor.OnDecorationStateListener{

                override fun onStateChangeListener(text: String, types: List<TextType>) {
                    val buttons = mutableListOf(boldText,italicText,underlineText,strikeText)
                    types.forEach { type ->
                        when {
                            type.name.contains("FONT_COLOR") -> {

                            }
                            type.name.contains("BACKGROUND_COLOR") -> {

                            }
                            else -> {
                                when(type){
                                    TextType.BOLD -> {
                                        boldText.activated(true)
                                        buttons.remove(boldText)
                                    }
                                    TextType.ITALIC ->{
                                        italicText.activated(true)
                                        buttons.remove(italicText)
                                    }
                                    TextType.UNDERLINE ->{
                                        underlineText.activated(true)
                                        buttons.remove(underlineText)
                                    }
                                    TextType.STRIKETHROUGH ->{
                                        strikeText.activated(true)
                                        buttons.remove(strikeText)
                                    }
                                    TextType.JUSTIFYLEFT->{
                                        leftGravityButton.isChecked = true
                                    }
                                    TextType.JUSTIFYCENTER->{
                                        centerGravityButton.isChecked = true
                                    }
                                    TextType.JUSTIFYRIGHT->{
                                        rightGravityButton.isChecked = true
                                    }
                                }
                            }
                        }
                        buttons.forEach { button ->
                            button.activated(false)
                        }
                    }
                }
            }
        }
        iconPanel = viewBinding.iconPanel
        pushUpDown = viewBinding.pushUpDown
        answerLayout = viewBinding.answerLayout
        panelAddFile = viewBinding.panelAddFile
        responseToUser = viewBinding.responseToUser
        textAnswer = viewBinding.textAnswer
        horizontalGuideCenter = viewBinding.horizontalGuideCenter
        horizontalGuideEnd = viewBinding.horizontalGuideEnd
        super.onViewCreated(view, savedInstanceState)
    }

    fun answerComment(comment:CommentEntity.Comment){
        answerLayout.show()
        answerLayout.activated(true)
        responseToUser
                .apply {
                    text = comment.commentOwner?.firstName
                            ?: getString(R.string.unknown_user)
                }
        textAnswer.text = comment.text.substringBefore(PostCustomView.PARSE_SYMBOL)
        var height = iconPanel.height + pushUpDown.height / 2+ heightAnswerPanel
        if(panelStyleText.isVisible() || panelGravityText.isVisible()){
            height += heightTextStylePanel
        }
        CommentsViewModel.publishSubject.onNext(Pair(ADD_HEIGHT_CONTAINER, height))
    }

    override fun attachFileNotActivated() {
        //createCommentCustomView.show()
        richEditor.show()
        panelAddFile.gone()
        btnAdd.gone()
        amountFiles.gone()
        if (answerLayout.isActivated){
            answerLayout.show()
        }
        mediaRecyclerView.gone()
    }

    override fun attachFileActivated() {
        richEditor.gone()
        answerLayout.gone()
        panelAddFile.show()
    }

    override fun gonePanelStyleText() {
        super.gonePanelStyleText()
        var height = calculateHeight()
        CommentsViewModel.publishSubject.onNext(Pair(ADD_HEIGHT_CONTAINER,height))
    }

    override fun showPanelStyleText() {
        super.showPanelStyleText()
        var height = calculateHeight()
        height += heightTextStylePanel
        CommentsViewModel.publishSubject.onNext(Pair(ADD_HEIGHT_CONTAINER,height))
    }

    private fun calculateHeight(): Int {
        var height = iconPanel.height + pushUpDown.height / 2 + heightEditText
        if (answerLayout.isVisible())
            height += heightAnswerPanel
        return height
    }

    override fun gonePanelGravityText() {
        super.gonePanelGravityText()
        var height = calculateHeight()
        CommentsViewModel.publishSubject.onNext(Pair(ADD_HEIGHT_CONTAINER,height))
    }

    override fun showPanelGravityText() {
        super.showPanelGravityText()
        var height = calculateHeight()
        height += heightTextStylePanel
        CommentsViewModel.publishSubject.onNext(Pair(ADD_HEIGHT_CONTAINER,height))
    }

    override fun setupBoldText() {
        richEditor.clearAndFocusEditor()
        richEditor.setBold()
    }

    override fun setupItalicText() {
        richEditor.clearAndFocusEditor()
        richEditor.setItalic()
    }

    override fun setupStrikeText() {
        richEditor.clearAndFocusEditor()
        richEditor.setStrikeThrough()
    }

    override fun setupUnderlineText() {
        richEditor.clearAndFocusEditor()
        richEditor.setUnderline()
    }

    override fun startChooseColorText() {

    }

    override fun endChooseColorText() {

    }

    override fun changeTextColor(color: Int) {
        richEditor.setTextColor(color)
        richEditor.showKeyBoard()
    }

    override fun setupLeftGravity() {
        richEditor.setAlignLeft()
    }

    override fun setupCenterGravity() {
        richEditor.setAlignCenter()
    }

    override fun setupRightGravity() {
        richEditor.setAlignRight()
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

    override fun changeStateToHalfExpanded() {
        CommentsViewModel.publishSubject
                .onNext(Pair(CHANGE_STATE_BOTTOM_SHEET_DATA, BottomSheetBehavior.STATE_HALF_EXPANDED))
    }

    override fun changeStateViewAfterAddMedia() {
        mediaRecyclerView.gone()
        icAttachFile.activated(false)
        panelAddFile.gone()
        amountFiles.gone()
        btnAdd.gone()
        if (answerLayout.isActivated) answerLayout.show()
        //createCommentCustomView.show()
        richEditor.show()
    }

    override fun closeKeyboard() {
        /*try {
            createCommentCustomView.listEditText.forEach {editText->
                editText.dismissKeyboard()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
    }

    override fun stateSettling() {
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
        changeBottomConstraintForRecyclerView(horizontalGuideEnd.id)
    }

    override fun stateExpanded() {
        if (answerLayout.isActivated) answerLayout.show()
    }

    override fun stateHalfExpanded() {
        if (answerLayout.isActivated) answerLayout.show()
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
        //val height = restoreHeightEditText()
        restoreAllViewForCollapsedState()
        //CommentsViewModel.publishSubject.onNext(Pair(ADD_HEIGHT_CONTAINER, height))
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
            || chooseMedia.url.contains(".wav") || chooseMedia.url.contains(".flac")){
            val audioEntity = AudioEntity(0,chooseMedia.url,false,"",
                chooseMedia.name,chooseMedia.authorMusic,"",0,0,
                chooseMedia.duration)
            loadingViews[chooseMedia.url] = createAudioPlayerView(audioEntity)
            richEditor.insertAudio(chooseMedia.url)
           // createCommentCustomView.addMusic(audioEntity,loadingViews[chooseMedia.url] as DownloadAudioPlayerView)
        }
        else if (chooseMedia.url.contains(".jpeg") || chooseMedia.url.contains(".jpg")
                || chooseMedia.url.contains(".png")){
            val fileEntity = FileEntity(0,chooseMedia.url,false,"",
                chooseMedia.url.substringAfterLast("/"),0,0)
            loadingViews[chooseMedia.url] =
                    createImageView(fileEntity)
            richEditor.insertImage(chooseMedia.url, "alt")
            //loadingViews[chooseMedia.url]?.let { createCommentCustomView.addImage(fileEntity, it) }
        }
        else {
            val fileEntity = FileEntity(0,chooseMedia.url,false,"",
                chooseMedia.url.substringAfterLast("/"),0,0,
                chooseMedia.urlPreview, chooseMedia.duration)
            loadingViews[chooseMedia.url] = createVideoPlayerView(fileEntity)
            richEditor.insertVideo(chooseMedia.url)
            //createCommentCustomView.addVideo(fileEntity,
               // loadingViews[chooseMedia.url] as DownloadVideoPlayerView)
        }
        //createCommentCustomView.textPost.setCompoundDrawablesWithIntrinsicBounds(null, null,
          //     null, null)
        prepareListeners(loadingViews[chooseMedia.url], chooseMedia)
        imageUploadingStarted(loadingViews[chooseMedia.url])

        /*if (loadingViews.size == chooseMedias.size && createCommentCustomView.currentContainerIsLast()){
            createCommentCustomView.createAllMainView()
            controlCommentEditTextChanges()
        }*/
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
            imageLoadingDelegate.loadImageFromFile(fileEntity.preview, previewForVideo)
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
            /*createCommentCustomView.textPost.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_send), null)
            setUpRightDrawableListener()*/
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
        /*loadingViews[path].let {view->
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
                else ->{
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
        }*/
    }

    private fun restoreAllViewForCollapsedState() {
        if (answerLayout.isActivated) answerLayout.show()
        panelAddFile.gone()
        richEditor.show()
        pushUpDown.background = context?.
                    let { ContextCompat.getDrawable(it, R.drawable.btn_push_down) }
    }

    private fun setupEditComment(comment: CommentEntity.Comment) {
        answerLayout.activated(false)
        answerLayout.gone()
        parsingTextInComment(comment)
        presenter.addMediaUrl(comment)
    }

    private fun parsingTextInComment(comment: CommentEntity.Comment) {
       /* createCommentCustomView.removeAllViewsAndContainer()
        val textAfterParse = mutableListOf<Pair<String,String>>()
        val splitList = comment.text.split(PostCustomView.PARSE_SYMBOL)
        splitList.forEachIndexed { index, s:String ->
            if (index %2 == 1){
                textAfterParse.add(Pair(splitList[index-1],s))
            }
            if (splitList.size-1 < index +1){
                textAfterParse.add(Pair(s,""))
            }
        }
        textAfterParse.filter { pair-> pair.second.isNotEmpty() || pair.first.trim().isNotEmpty() }
            .forEach { text:Pair<String,String>->
                val container: LinearLayout = LayoutInflater.from(context)
                    .inflate(R.layout.layout_create_post_view, createCommentCustomView, false)
                        as LinearLayout
                val imageContainer = container.findViewById<CreateImageGalleryView>(R.id.createImageContainer)
                val audioContainer = container.findViewById<CreateAudioGalleryView>(R.id.createAudioContainer)
                val videoContainer = container.findViewById<CreateVideoGalleryView>(R.id.createVideoContainer)
                setupMediaViews(text.second, imageContainer,audioContainer,videoContainer, comment)
                val textView = container.findViewById<AppCompatEditText>(R.id.postText)
                textView.setText(text.first)
                createCommentCustomView.addViewInEditPost(textView,imageContainer, audioContainer, videoContainer)
                createCommentCustomView.addView(container)
            }
        createCommentCustomView.createAllMainView()
        controlCommentEditTextChanges()
        setUpRightDrawableListener()*/
    }

    private fun setupMediaViews(text: String,imageContainer: CreateImageGalleryView,
                    audioContainer: CreateAudioGalleryView, videoContainer:CreateVideoGalleryView
                                ,comment: CommentEntity.Comment) {
        if (text.length>3) {
            val newText = text.substring(1, text.length - 2).split(",")
            newText.forEach { nameMedia ->
                fillingView(comment, nameMedia, imageContainer, audioContainer, videoContainer)
            }
        }
    }

    private fun fillingView(comment: CommentEntity.Comment, name:String,
                            imageContainer:CreateImageGalleryView, audioContainer:CreateAudioGalleryView,
                            videoContainer: CreateVideoGalleryView){
        comment.audios.forEach {audioEntity->
            if (audioEntity.song == name) {
                val url = "/groups/0/comments/${audioEntity.file.substringAfterLast("/")}"
                loadingViews[url] = createAudioPlayerViewForEditComment(audioEntity)
                audioContainer.addAudio(audioEntity, loadingViews[url] as DownloadAudioPlayerView)
                //createCommentCustomView.namesAudio.add(Pair(name,loadingViews[url]))
                return@fillingView
            }
        }
        comment.images.forEach{imageEntity ->
            if (imageEntity.title == name) {
                val url = "/groups/0/comments/${imageEntity.file.substringAfterLast("/")}"
                loadingViews[url] = createImageViewForEditComment(imageEntity)
                loadingViews[url]?.let { imageContainer.addImage(it) }
                //createCommentCustomView.namesImage.add(Pair(name, loadingViews[url]))
                return@fillingView
            }
        }
        comment.videos.forEach { videoEntity ->
            if (videoEntity.title == name) {
                val url = "/groups/0/comments/${videoEntity.file.substringAfterLast("/")}"
                loadingViews[url] = createVideoPlayerViewForEditComment(videoEntity)
                videoContainer.addVideo(videoEntity, loadingViews[url] as DownloadVideoPlayerView)
              //  createCommentCustomView.namesVideo.add(Pair(name, loadingViews[url]))
                return@fillingView
            }
        }
    }

    private fun createAudioPlayerViewForEditComment(audioEntity: AudioEntity):DownloadAudioPlayerView{
        val url = "/groups/0/comments/${audioEntity.file.substringAfterLast("/")}"
        return DownloadAudioPlayerView(requireContext()).apply {
            trackName = audioEntity.song
            trackOwner = "Загрузил (ID:${audioEntity.owner})"
            durationTrack.text = if (audioEntity.duration != "") audioEntity.duration else "00:00"
           findViewById<ImageView>(R.id.detachImage).apply {
               show()
               setOnClickListener {
                   presenter.removeContent(url)
                   detachMedia(url)
               }
           }
        }
    }

    private fun createImageViewForEditComment(imageEntity: FileEntity):View{
        val url = "/groups/0/comments/${imageEntity.file.substringAfterLast("/")}"
        val image = LayoutInflater.from(context).inflate(R.layout.layout_create_pic, null)
        val pic = image.findViewById<SimpleDraweeView>(R.id.imagePreview)
        imageLoadingDelegate.loadImageFromUrl(imageEntity.file, pic)
        image.run {
            findViewById<ImageView>(R.id.detachImage).apply {
                show()
                setOnClickListener {
                    presenter.removeContent(url)
                    detachMedia(url)
                }
            }
        }
        return image
    }

    private fun createVideoPlayerViewForEditComment(videoEntity:FileEntity):DownloadVideoPlayerView{
        val url = "/groups/0/comments/${videoEntity.file.substringAfterLast("/")}"
        return DownloadVideoPlayerView(requireContext()).apply {
            imageLoadingDelegate.loadImageFromUrl(videoEntity.preview, previewForVideo)
            durationVideo.text = if (videoEntity.duration != "") videoEntity.duration else "00:00"
            nameVideo.text = videoEntity.title
            findViewById<ImageView>(R.id.detachImage).apply {
                show()
                setOnClickListener {
                    presenter.removeContent(url)
                    detachMedia(url)
                }
            }
        }
    }

}