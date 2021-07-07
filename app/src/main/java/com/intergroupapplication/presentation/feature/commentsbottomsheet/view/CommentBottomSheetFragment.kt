package com.intergroupapplication.presentation.feature.commentsbottomsheet.view

import android.graphics.Color
import android.graphics.Typeface.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.budiyev.android.circularprogressbar.CircularProgressBar
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.model.TextType
import com.intergroupapplication.databinding.FragmentCommentBottomSheetBinding
import com.intergroupapplication.domain.KeyboardVisibilityEvent
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
import timber.log.Timber
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
        private const val MAIN_IC_EDIT_COLOR_DRAWABLE = 228
        private const val MAIN_COLOR = "#12161E"
    }

    private val viewBinding by viewBinding(FragmentCommentBottomSheetBinding::bind)

    @Inject
    @InjectPresenter
    lateinit var presenter: CommentBottomSheetPresenter

    @Inject
    lateinit var rightDrawableListener: RightDrawableListener

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    private val loadingViews: MutableMap<String, View?> = mutableMapOf()

    private val heightEditText by lazy { requireContext().dpToPx(70) }
    private val heightAnswerPanel by lazy { requireContext().dpToPx(35) }

    @ProvidePresenter
    fun providePresenter(): CommentBottomSheetPresenter = presenter

    override fun layoutRes() = R.layout.fragment_comment_bottom_sheet

    override fun getSnackBarCoordinator() = viewBinding.bottomSheetCoordinator

    private lateinit var richEditor: RichEditor
    private lateinit var sendButton: Button
    private lateinit var iconPanel:LinearLayout
    private lateinit var pushUpDown:Button
    private lateinit var answerLayout:LinearLayout
    private lateinit var responseToUser:TextView
    private lateinit var textAnswer:TextView
    private lateinit var horizontalGuideCollapsed: Guideline
    private lateinit var horizontalGuideCollapsedWithPanelStyle: Guideline
    private lateinit var horizontalGuideEndWithKeyboard:Guideline
    private lateinit var containerRichEditor:LinearLayout
    private lateinit var panelStyleText:LinearLayout
    private lateinit var panelGravityText:RadioGroup
    private lateinit var boldText:ImageView
    private lateinit var italicText:ImageView
    private lateinit var strikeText:ImageView
    private lateinit var underlineText:ImageView
    private lateinit var leftGravityButton:RadioButton
    private lateinit var centerGravityButton:RadioButton
    private lateinit var rightGravityButton:RadioButton
    private val namesMap = mutableMapOf<String,String>()
    private val finalNamesMedia = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        parentFragmentManager.setFragmentResultListener(CommentsAdapter.EDIT_COMMENT_REQUEST,
            viewLifecycleOwner,
            { _, result ->
                val comment: CommentEntity.Comment? = result
                    .getParcelable(CommentsAdapter.COMMENT_KEY)
                if (comment != null){
                    setupEditComment(comment)
                }
            })
        activity?.let {
            KeyboardVisibilityEvent.setEventListener(
                it,
                viewLifecycleOwner,
                { isVisible->
                    if (currentState == BottomSheetBehavior.STATE_EXPANDED){
                        if (isVisible){
                            changeBottomConstraintForView(horizontalGuideEndWithKeyboard.id)
                        }
                        else{
                            changeBottomConstraintForView(horizontalGuideEnd.id)
                        }
                    }
                })
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        richEditor = viewBinding.richEditor.apply {
            setEditorFontSize(18)
            val padding = context.dpToPx(4)
            setEditorPadding(padding, padding, padding, padding)
            setEditorFontColor(ContextCompat.getColor(view.context, R.color.whiteTextColor))
            setPlaceholder(context.getString(R.string.add_photo_or_text))
            setBackgroundColor(Color.parseColor(MAIN_COLOR))
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            decorationStateListener = object :RichEditor.OnDecorationStateListener{

                override fun onStateChangeListener(text: String, types: List<TextType>) {
                    val buttons = mutableListOf(boldText,italicText,underlineText,strikeText)
                    types.forEach { type ->
                        when {
                            type.name.contains("FONT_COLOR") -> {
                                icEditColor.setImageDrawable(colorDrawableGateway.
                                getDrawableByColor(type.color))
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
                                    else -> Timber.tag("else_type").d(type.name)
                                }
                            }
                        }
                        buttons.forEach { button ->
                            button.activated(false)
                        }
                    }
                }
            }
            textChangeListener = object : RichEditor.OnTextChangeListener{
                override fun onTextChange(text: String?) {
                    Timber.tag("tut_text").d(text?.substringBefore("re-state://"))
                    if(text?.replace("<br>","")?.isNotEmpty() == true
                        && allViewsIsUpload){
                        sendButton.show()
                    }
                    else{
                        sendButton.hide()
                    }
                }
            }
        }
        sendButton = viewBinding.sendCommentButton.apply {
            setOnClickListener {
                sendComment()
            }
        }
        iconPanel = viewBinding.iconPanel
        pushUpDown = viewBinding.pushUpDown
        answerLayout = viewBinding.answerLayout
        responseToUser = viewBinding.responseToUser
        textAnswer = viewBinding.textAnswer
        horizontalGuideCollapsed = viewBinding.horizontalGuideCollapsed
        horizontalGuideCollapsedWithPanelStyle = viewBinding.horizontalGuideCollapsedWithPanelStyle
        containerRichEditor = viewBinding.containerForRichEditorAndSendButton
        horizontalGuideEndWithKeyboard = viewBinding.horizontalGuideEndWithKeyboard
        panelStyleText = viewBinding.panelStyleText
        panelGravityText = viewBinding.panelGravityText
        boldText = viewBinding.selectBoldText
        italicText = viewBinding.selectItalicText
        underlineText = viewBinding.selectUnderlineText
        strikeText = viewBinding.selectStrikeText
        leftGravityButton = viewBinding.leftGravityButton
        centerGravityButton = viewBinding.centerGravityButton
        rightGravityButton = viewBinding.rightGravityButton
        setupPanelStyleText()
        setupPanelGravityText()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun sendComment() {
        CommentsViewModel.publishSubject
            .onNext(
                Pair(
                    CREATE_COMMENT_DATA, Triple(
                        richEditor.createFinalText(namesMap,finalNamesMedia),
                        presenter, finalNamesMedia
                    )
                )
            )
        loadingViews.clear()
        chooseMedias.clear()
        richEditor.html = null
        panelStyleText.gone()
        panelGravityText.gone()
        icEditColor.activated(false)
        icEditAlign.activated(false)
        icEditText.activated(false)
        icEditColor.setImageDrawable(colorDrawableGateway.getDrawableByColor(
            MAIN_IC_EDIT_COLOR_DRAWABLE))
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
        richEditor.run  {
            show()
            if (html?.replace("<br>","")?.isNotEmpty() == true){
                sendButton.show()
            }
        }
        panelAddFile.gone()
        btnAdd.gone()
        amountFiles.gone()
        if (answerLayout.isActivated){
            answerLayout.show()
        }
        mediaRecyclerView.gone()
    }

    override fun attachFileActivated() {
        sendButton.gone()
        richEditor.gone()
        answerLayout.gone()
        panelAddFile.show()
        panelStyleText.gone()
        icEditText.activated(false)
        panelGravityText.gone()
        icEditAlign.activated(false)
    }

    override fun gonePanelStyleText() {
        panelStyleText.gone()
        val height = calculateHeight()
        CommentsViewModel.publishSubject.onNext(Pair(ADD_HEIGHT_CONTAINER,height))
        if (currentState == BottomSheetBehavior.STATE_COLLAPSED)
            changeBottomConstraintRichEditor(horizontalGuideCollapsed.id)
    }

    override fun showPanelStyleText() {
        super.showPanelStyleText()
        panelStyleText.show()
        panelGravityText.gone()
        richEditor.run {
            show()
            if (html?.replace("<br>","")?.isNotEmpty() == true) sendButton.show()
        }
        val height = calculateHeight() + heightTextStylePanel
        CommentsViewModel.publishSubject.onNext(Pair(ADD_HEIGHT_CONTAINER,height))
        if (currentState == BottomSheetBehavior.STATE_COLLAPSED)
            changeBottomConstraintRichEditor(horizontalGuideCollapsedWithPanelStyle.id)
    }

    override fun gonePanelGravityText() {
        panelGravityText.gone()
        val height = calculateHeight()
        CommentsViewModel.publishSubject.onNext(Pair(ADD_HEIGHT_CONTAINER,height))
        if (currentState == BottomSheetBehavior.STATE_COLLAPSED)
            changeBottomConstraintRichEditor(horizontalGuideCollapsed.id)
    }

    override fun showPanelGravityText() {
        super.showPanelGravityText()
        panelGravityText.show()
        panelStyleText.gone()
        richEditor.run {
            show()
            if (html?.replace("<br>","")?.isNotEmpty() == true) sendButton.show()
        }
        val height = calculateHeight() + heightTextStylePanel
        CommentsViewModel.publishSubject.onNext(Pair(ADD_HEIGHT_CONTAINER,height))
        if (currentState == BottomSheetBehavior.STATE_COLLAPSED)
            changeBottomConstraintRichEditor(horizontalGuideCollapsedWithPanelStyle.id)
    }

    override fun calculateHeight(): Int {
        var height = iconPanel.height + pushUpDown.height / 2 + heightEditText
        if (answerLayout.isVisible())
            height += heightAnswerPanel
        return height
    }

    private fun setupPanelStyleText() {
        setupBoldTextView()
        setupItalicTextView()
        setupStrikeTextView()
        setupUnderlineTextView()
    }

    private fun setupBoldTextView() {
        boldText.setOnClickListener {
            it.activated(!it.isActivated)
            richEditor.setBold()
        }
    }

    private fun setupItalicTextView() {
        italicText.setOnClickListener {
            it.activated(!it.isActivated)
            richEditor.setItalic()
        }
    }

    private fun setupStrikeTextView() {
        strikeText.setOnClickListener {
            it.activated(!it.isActivated)
            richEditor.setStrikeThrough()
        }
    }

    private fun setupUnderlineTextView() {
        underlineText.setOnClickListener {
            it.activated(!it.isActivated)
            richEditor.setUnderline()
        }
    }

    override fun startChooseColorText() {
        stateBeforeChooseColor = currentState
        super.startChooseColorText()
        panelGravityText.gone()
        panelStyleText.gone()
        richEditor.gone()
        sendButton.gone()
        answerLayout.gone()
    }

    override fun endChooseColorText() {
        super.endChooseColorText()
        if (answerLayout.isActivated) answerLayout.show()
        richEditor.run {
            show()
            if (html?.replace("<br>","")?.isNotEmpty() == true) sendButton.show()
        }
        if (stateBeforeChooseColor == BottomSheetBehavior.STATE_COLLAPSED ||
            stateBeforeChooseColor == BottomSheetBehavior.STATE_SETTLING){
            CommentsViewModel.publishSubject.onNext(CHANGE_STATE_BOTTOM_SHEET_DATA to
                    BottomSheetBehavior.STATE_COLLAPSED)
        }
    }

    override fun changeTextColor(color: Int) {
        endChooseColorText()
        super.changeTextColor(color)
        richEditor.run {
            setTextColor(color)
            showKeyBoard()
        }
    }

    private fun setupPanelGravityText(){
        setupLeftGravityView()
        setupCenterGravityView()
        setupRightGravityView()
    }

    private fun setupLeftGravityView(){
        leftGravityButton.setOnClickListener {
            richEditor.setAlignLeft()
        }
    }

    private fun setupCenterGravityView(){
        centerGravityButton.setOnClickListener {
            richEditor.setAlignCenter()
        }
    }

    private fun setupRightGravityView(){
        rightGravityButton.setOnClickListener {
            richEditor.setAlignRight()
        }
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
        richEditor.run  {
            show()
            if (html?.replace("<br>","")?.isNotEmpty() == true
                && allViewsIsUpload){
                sendButton.show()
            }
        }
    }

    override fun closeKeyboard() {
        richEditor.hideKeyboard()
    }

    override fun stateSettling() {
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
        changeBottomConstraintForView(horizontalGuideEnd.id)
    }

    override fun stateExpanded() {
        if (answerLayout.isActivated) answerLayout.show()
    }

    override fun stateHalfExpanded() {
        if (answerLayout.isActivated) answerLayout.show()
        changeBottomConstraintForView(horizontalGuideCenter.id)
    }

    override fun stateDragging() {
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
        changeBottomConstraintForView(horizontalGuideEnd.id)
    }

    override fun changeBottomConstraintForView(id: Int) {
        super.changeBottomConstraintForView(id)
        changeBottomConstraintRichEditor(id)
    }

    private fun changeBottomConstraintRichEditor(id: Int) {
        val paramsRichEditor = containerRichEditor.layoutParams as ConstraintLayout.LayoutParams
        paramsRichEditor.bottomToTop = id
        containerRichEditor.layoutParams = paramsRichEditor
    }

    override fun stateHidden() {
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
    }

    override fun stateCollapsed() {
        if (panelGravityText.isVisible || panelStyleText.isVisible){
            changeBottomConstraintRichEditor(horizontalGuideCollapsedWithPanelStyle.id)
        }
        else {
            changeBottomConstraintRichEditor(horizontalGuideCollapsed.id)
        }
        restoreAllViewForCollapsedState()
        chooseMedias.clear()
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
            namesMap[chooseMedia.url] = chooseMedia.name
            richEditor.insertAudio(chooseMedia.url)
        }
        else if (chooseMedia.url.contains(".jpeg") || chooseMedia.url.contains(".jpg")
                || chooseMedia.url.contains(".png")){
            val fileEntity = FileEntity(0,chooseMedia.url,false,"",
                chooseMedia.url.substringAfterLast("/"),0,0)
            loadingViews[chooseMedia.url] =
                    createImageView(fileEntity)
            namesMap[chooseMedia.url] = fileEntity.title
            richEditor.insertImage(chooseMedia.url, "alt")
        }
        else {
            val fileEntity = FileEntity(0,chooseMedia.url,false,"",
                chooseMedia.url.substringAfterLast("/"),0,0,
                chooseMedia.urlPreview, chooseMedia.duration)
            loadingViews[chooseMedia.url] = createVideoPlayerView(fileEntity)
            namesMap[chooseMedia.url] = fileEntity.title
            richEditor.insertVideo(chooseMedia.url)
        }
        prepareListeners(loadingViews[chooseMedia.url], chooseMedia)
        imageUploadingStarted(loadingViews[chooseMedia.url])
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

    private var allViewsIsUpload = true
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
        loadingViews.values.forEach{ view ->
            val imageUploadingProgressBar = view
                ?.findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            if (imageUploadingProgressBar?.progress?:0.0f<100){
                allViewsIsUpload = false
                return@forEach
            }
        }
        if (allViewsIsUpload || loadingViews.isNotEmpty()){
            sendButton.show()
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

    }

    private fun restoreAllViewForCollapsedState() {
        if (answerLayout.isActivated) answerLayout.show()
        panelAddFile.gone()
        richEditor.run  {
            show()
            if (html?.replace("<br>","")?.isNotEmpty() == true){
                sendButton.show()
            }
        }
        pushUpDown.background = context?.
                    let { ContextCompat.getDrawable(it, R.drawable.btn_push_down) }
    }

    private fun setupEditComment(comment: CommentEntity.Comment) {
        answerLayout.activated(false)
        answerLayout.gone()
        richEditor.html = changeNameOnUrl(comment)
        presenter.addMediaUrl(comment)
    }

    private fun changeNameOnUrl(comment:CommentEntity.Comment):String{
        namesMap.clear()
        var text = comment.text
        comment.audios.forEach {
            if (text.contains(it.song)){
                namesMap[it.file] = it.song
                text = text.substringBefore(it.song)+it.file+text.
                    substringAfter(it.song+PostCustomView.MEDIA_PREFIX)
            }
        }
        comment.images.forEach {
            if (text.contains(it.title)){
                namesMap[it.file] = it.title
                text = text.substringBefore(it.title)+it.file+text.
                    substringAfter(it.title+PostCustomView.MEDIA_PREFIX)
            }
        }
        comment.videos.forEach {
            if (text.contains(it.title)){
                namesMap[it.file] = it.title
                text = text.substringBefore(it.title)+it.file+text.
                    substringAfter(it.title+PostCustomView.MEDIA_PREFIX)
            }
        }
        return text
    }

}