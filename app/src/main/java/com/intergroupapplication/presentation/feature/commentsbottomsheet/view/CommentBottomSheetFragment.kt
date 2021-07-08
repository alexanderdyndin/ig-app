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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.model.TextType
import com.intergroupapplication.databinding.FragmentCommentBottomSheetBinding
import com.intergroupapplication.domain.KeyboardVisibilityEvent
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.LoadMediaType
import com.intergroupapplication.presentation.base.BaseBottomSheetFragment
import com.intergroupapplication.presentation.customview.*
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.presenter.CommentBottomSheetPresenter
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentsAdapter
import com.intergroupapplication.presentation.feature.commentsdetails.viewmodel.CommentsViewModel
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
    lateinit var modelFactory: ViewModelProvider.Factory

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
    private val progressMedias = mutableMapOf<String,LoadMediaType>()
    private var allViewsIsUpload = true
    private val heightEditText by lazy { requireContext().dpToPx(70) }
    private val heightAnswerPanel by lazy { requireContext().dpToPx(35) }

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
                    boldText.changeActivated(false,italicText,underlineText,strikeText)
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
                                    }
                                    TextType.ITALIC ->{
                                        italicText.activated(true)
                                    }
                                    TextType.UNDERLINE ->{
                                        underlineText.activated(true)
                                    }
                                    TextType.STRIKETHROUGH ->{
                                        strikeText.activated(true)
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
            if (html?.replace("<br>","")?.isNotEmpty() == true && allViewsIsUpload){
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
            if (html?.replace("<br>","")?.isNotEmpty() == true && allViewsIsUpload)
                sendButton.show()
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
            if (html?.replace("<br>","")?.isNotEmpty() == true && allViewsIsUpload)
                sendButton.show()
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
            if (html?.replace("<br>","")?.isNotEmpty() == true && allViewsIsUpload)
                sendButton.show()
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
        richEditor.show()
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
            namesMap[chooseMedia.url] = chooseMedia.name
            richEditor.insertAudio(chooseMedia.url)
        }
        else if (chooseMedia.url.contains(".jpeg") || chooseMedia.url.contains(".jpg")
                || chooseMedia.url.contains(".png")){
            val fileEntity = FileEntity(0,chooseMedia.url,false,"",
                chooseMedia.url.substringAfterLast("/"),0,0)
            namesMap[chooseMedia.url] = fileEntity.title
            richEditor.insertImage(chooseMedia.url, "alt")
        }
        else {
            val fileEntity = FileEntity(0,chooseMedia.url,false,"",
                chooseMedia.url.substringAfterLast("/"),0,0,
                chooseMedia.urlPreview, chooseMedia.duration)
            namesMap[chooseMedia.url] = fileEntity.title
            richEditor.insertVideo(chooseMedia.url)
        }
        progressMedias[chooseMedia.url] = LoadMediaType.START
        sendButton.hide()
    }

    override fun showImageUploadingProgress(progress: Float, path: String) {
        progressMedias[path] = LoadMediaType.PROGRESS.apply {
            this.progress = progress
        }
    }

    override fun showImageUploadingError(path: String) {
        Timber.tag("tut_error").d(path)
        progressMedias[path] = LoadMediaType.ERROR
    }

    override fun showImageUploaded(path: String) {
        allViewsIsUpload = true
        progressMedias[path] = LoadMediaType.UPLOAD
        progressMedias.values.forEach { type->
            if (type != LoadMediaType.UPLOAD){
                allViewsIsUpload = false
                return@forEach
            }
        }
        if (allViewsIsUpload){
            sendButton.show()
        }
        else{
            sendButton.hide()
        }
    }

    override fun hideSwipeLayout() {
        CommentsViewModel.publishSubject.onNext(Pair(HIDE_SWIPE_DATA, null))
    }

    private fun restoreAllViewForCollapsedState() {
        if (answerLayout.isActivated) answerLayout.show()
        panelAddFile.gone()
        richEditor.run  {
            show()
            if (html?.replace("<br>","")?.isNotEmpty() == true && allViewsIsUpload){
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