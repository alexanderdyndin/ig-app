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
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.model.CreateCommentDataModel
import com.intergroupapplication.data.model.ProgressMediaModel
import com.intergroupapplication.data.model.TextType
import com.intergroupapplication.databinding.FragmentCommentBottomSheetBinding
import com.intergroupapplication.domain.KeyboardVisibilityEvent
import com.intergroupapplication.domain.entity.*
import com.intergroupapplication.domain.entity.ParseConstants.END_CONTAINER
import com.intergroupapplication.domain.entity.ParseConstants.MEDIA_PREFIX
import com.intergroupapplication.domain.entity.ParseConstants.START_CONTAINER
import com.intergroupapplication.presentation.base.BaseBottomSheetFragment
import com.intergroupapplication.presentation.customview.*
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.presenter.CommentBottomSheetPresenter
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.CommentsAdapter
import com.intergroupapplication.presentation.widgets.progress.view.ProgressDialog
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class CommentBottomSheetFragment : BaseBottomSheetFragment(), BottomSheetView {

    companion object {
        const val CALL_METHOD_KEY = "call_method_key"
        const val METHOD_KEY = "method_key"
        const val DATA_KEY = "data_key"
        const val COMMENT_ID_KEY = "comment_id_key"
        const val CREATE_COMMENT_DATA = 0
        const val CHANGE_STATE_BOTTOM_SHEET_DATA = 1
        const val ADD_HEIGHT_CONTAINER = 2
        const val COMMENT_CREATED_DATA = 3
        const val ANSWER_COMMENT_CREATED_DATA = 4
        const val SHOW_COMMENT_UPLOADING_DATA = 5
        const val HIDE_SWIPE_DATA = 6
        const val EDIT_COMMENT_DATA = 7
        private const val MAIN_IC_EDIT_COLOR_DRAWABLE = 228
        private const val MAIN_COLOR = "#12161E"
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: CommentBottomSheetPresenter

    private var commentId: String? = null
    private val viewBinding by viewBinding(FragmentCommentBottomSheetBinding::bind)

    @ProvidePresenter
    fun providePresenter(): CommentBottomSheetPresenter = presenter

    override fun layoutRes() = R.layout.fragment_comment_bottom_sheet

    override fun getSnackBarCoordinator() = viewBinding.bottomSheetCoordinator

    private lateinit var richEditor: RichEditor
    private lateinit var sendButton: Button
    private lateinit var iconPanel: LinearLayout
    private lateinit var pushUpDown: Button
    private lateinit var answerLayout: LinearLayout
    private lateinit var responseToUser: TextView
    private lateinit var textAnswer: TextView
    private lateinit var horizontalGuideCollapsed: Guideline
    private lateinit var horizontalGuideCollapsedWithPanelStyle: Guideline
    private lateinit var horizontalGuideCollapsedWithTwoView: Guideline
    private lateinit var horizontalGuideEndWithKeyboard: Guideline
    private lateinit var containerRichEditor: LinearLayout
    private lateinit var panelStyleText: LinearLayout
    private lateinit var panelGravityText: RadioGroup
    private lateinit var boldText: ImageView
    private lateinit var italicText: ImageView
    private lateinit var strikeText: ImageView
    private lateinit var underlineText: ImageView
    private lateinit var leftGravityButton: RadioButton
    private lateinit var centerGravityButton: RadioButton
    private lateinit var rightGravityButton: RadioButton
    private val namesMap = mutableMapOf<String, String>()
    private val finalNamesMedia = mutableListOf<String>()
    private val loadingMedias = mutableMapOf<String, LoadMediaType>()
    private var allViewsIsUpload = true
    private var stateBeforeShowMediaRecycler = BottomSheetBehavior.STATE_COLLAPSED
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
                if (comment != null) {
                    setupEditComment(comment)
                }
            })
        activity?.let {
            KeyboardVisibilityEvent.setEventListener(
                it,
                viewLifecycleOwner,
                { isVisible ->
                    if (currentState == BottomSheetBehavior.STATE_EXPANDED) {
                        if (isVisible) {
                            changeBottomConstraintForView(horizontalGuideEndWithKeyboard.id)
                        } else {
                            changeBottomConstraintForView(horizontalGuideEnd.id)
                        }
                    }
                })
        }

        childFragmentManager.setFragmentResultListener(
            ProgressDialog.CALLBACK_METHOD_KEY,
            viewLifecycleOwner
        ) { _, result: Bundle ->
            when (result.getInt(ProgressDialog.METHOD_KEY)) {
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
                ProgressDialog.REMOVE_ALL_CONTENT_CODE -> {
                    result.getParcelableArrayList<ProgressMediaModel>(ProgressDialog.DATA_KEY)
                        ?.let {
                            it.forEach { media ->
                                deleteMediaFromEditor(media.chooseMedia)
                            }
                        }
                    presenter.removeAllContents()
                }
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun deleteMediaFromEditor(chooseMedia: ChooseMedia) {
        val prefix = when (chooseMedia.type) {
            MediaType.AUDIO -> {
                "${ParseConstants.START_AUDIO}${chooseMedia.url}${ParseConstants.END_AUDIO}"
            }
            MediaType.IMAGE -> {
                "${ParseConstants.START_IMAGE}${chooseMedia.url}${ParseConstants.END_IMAGE}"
            }
            MediaType.VIDEO -> {
                "${ParseConstants.START_VIDEO}${chooseMedia.url}${ParseConstants.END_VIDEO}"
            }
        }
        loadingMedias.remove(chooseMedia.url)
        richEditor.html = richEditor.html?.replace(prefix, "")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        richEditor = viewBinding.richEditor.apply {
            setEditorFontSize(18)
            val padding = context.dpToPx(4)
            setEditorPadding(padding, padding, padding, padding)
            setEditorFontColor(ContextCompat.getColor(view.context, R.color.whiteTextColor))
            setPlaceholder(context.getString(R.string.write_your_comment))
            setBackgroundColor(Color.parseColor(MAIN_COLOR))
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            decorationStateListener = object : RichEditor.OnDecorationStateListener {

                override fun onStateChangeListener(text: String, types: List<TextType>) {
                    boldText.changeActivated(false, italicText, underlineText, strikeText)
                    types.forEach { type ->
                        when {
                            type.name.contains("FONT_COLOR") -> {
                                icEditColor.setImageDrawable(
                                    colorDrawableGateway.getDrawableByColor(type.color)
                                )
                            }
                            else -> {
                                when (type) {
                                    TextType.BOLD -> {
                                        boldText.activated(true)
                                    }
                                    TextType.ITALIC -> {
                                        italicText.activated(true)
                                    }
                                    TextType.UNDERLINE -> {
                                        underlineText.activated(true)
                                    }
                                    TextType.STRIKETHROUGH -> {
                                        strikeText.activated(true)
                                    }
                                    TextType.JUSTIFYLEFT -> {
                                        leftGravityButton.isChecked = true
                                    }
                                    TextType.JUSTIFYCENTER -> {
                                        centerGravityButton.isChecked = true
                                    }
                                    TextType.JUSTIFYRIGHT -> {
                                        rightGravityButton.isChecked = true
                                    }
                                    else -> Timber.tag("else_type").d(type.name)
                                }
                            }
                        }
                    }
                }
            }
            textChangeListener = object : RichEditor.OnTextChangeListener {
                override fun onTextChange(text: String?) {
                    val newText = text?.substringBefore("re-state://")
                        ?.replace(
                            Regex("$START_CONTAINER<br>$END_CONTAINER|&nbsp;"),
                            ""
                        )?.trim()
                        ?.replace(
                            Regex("$START_CONTAINER *$END_CONTAINER"),
                            ""
                        ) ?: ""
                    if (newText.isNotEmpty() && allViewsIsUpload) {
                        sendButton.show()
                    } else {
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
        horizontalGuideCollapsedWithTwoView =
            viewBinding.horizontalGuideCollapsedWithPanelStyleAndAnswerLayout
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
        commentId?.let {
            parentFragmentManager.setResult(
                CALL_METHOD_KEY,
                METHOD_KEY to EDIT_COMMENT_DATA, DATA_KEY to CreateCommentDataModel(
                    richEditor.createFinalText(namesMap, finalNamesMedia),
                    presenter, finalNamesMedia
                ), COMMENT_ID_KEY to it
            )
            commentId = null
        } ?: parentFragmentManager.setResult(
            CALL_METHOD_KEY,
            METHOD_KEY to CREATE_COMMENT_DATA, DATA_KEY to
                    CreateCommentDataModel(
                        richEditor.createFinalText(namesMap, finalNamesMedia),
                        presenter, finalNamesMedia
                    )
        )
        richEditor.html = null
        panelStyleText.gone()
        panelGravityText.gone()
        icEditColor.activated(false)
        icEditAlign.activated(false)
        icEditText.activated(false)
        icEditColor.setImageDrawable(
            colorDrawableGateway.getDrawableByColor(
                MAIN_IC_EDIT_COLOR_DRAWABLE
            )
        )
        sendButton.hide()
        changeBottomConstraintRichEditor(horizontalGuideCollapsed.id)
        iconPanel.changeMargin(8)
    }

    fun answerComment(comment: CommentEntity.Comment) {
        answerLayout.show()
        answerLayout.activated(true)
        responseToUser
            .apply {
                text = comment.commentOwner?.firstName
                    ?: getString(R.string.unknown_user)
            }
        textAnswer.text = comment.text.substringBefore("<")
        var height = heightEditText + heightIconPanel + pushUpDown.height / 2 + heightAnswerPanel
        if (panelStyleText.isVisible() || panelGravityText.isVisible()) {
            height += heightTextStylePanel
            if (currentState == BottomSheetBehavior.STATE_COLLAPSED)
                changeBottomConstraintRichEditor(horizontalGuideCollapsedWithTwoView.id)
        } else {
            if (currentState == BottomSheetBehavior.STATE_COLLAPSED) {
                changeBottomConstraintRichEditor(horizontalGuideCollapsedWithPanelStyle.id)
            }
        }
        parentFragmentManager.setResult(
            CALL_METHOD_KEY,
            METHOD_KEY to ADD_HEIGHT_CONTAINER, DATA_KEY to height
        )
    }

    override fun attachFileNotActivated() {
        richEditor.run {
            show()
            if (html?.replace("<br>", "")?.isNotEmpty() == true && allViewsIsUpload) {
                sendButton.show()
            }
        }
        panelAddFile.gone()
        btnAdd.gone()
        amountFiles.gone()
        if (answerLayout.isActivated) {
            answerLayout.show()
        }
        mediaRecyclerView.gone()
        returnStateToOriginal()
    }

    override fun attachFileActivated() {
        super.attachFileActivated()
        iconPanel.changeMargin(8)
        stateBeforeShowMediaRecycler = currentState
        sendButton.gone()
        richEditor.gone()
        answerLayout.gone()
        panelAddFile.show()
        panelStyleText.gone()
        panelGravityText.gone()
        answerLayout.gone()
    }

    override fun gonePanelStyleText() {
        panelStyleText.gone()
        val height = calculateHeight()
        parentFragmentManager.setResult(
            CALL_METHOD_KEY,
            METHOD_KEY to ADD_HEIGHT_CONTAINER, DATA_KEY to height
        )
        if (currentState == BottomSheetBehavior.STATE_COLLAPSED) {
            if (answerLayout.isVisible()) {
                changeBottomConstraintRichEditor(horizontalGuideCollapsedWithPanelStyle.id)
            } else {
                changeBottomConstraintRichEditor(horizontalGuideCollapsed.id)
            }
        }
        iconPanel.changeMargin(8)
    }

    override fun showPanelStyleText() {
        super.showPanelStyleText()
        if (answerLayout.isActivated) answerLayout.show()
        panelStyleText.show()
        panelGravityText.gone()
        richEditor.run {
            show()
            if (html?.replace("<br>", "")?.isNotEmpty() == true && allViewsIsUpload)
                sendButton.show()
        }
        val height = calculateHeight() + heightTextStylePanel
        parentFragmentManager.setResult(
            CALL_METHOD_KEY,
            METHOD_KEY to ADD_HEIGHT_CONTAINER, DATA_KEY to height
        )
        if (currentState == BottomSheetBehavior.STATE_COLLAPSED) {
            if (!answerLayout.isVisible()) {
                changeBottomConstraintRichEditor(horizontalGuideCollapsedWithPanelStyle.id)
            } else {
                changeBottomConstraintRichEditor(horizontalGuideCollapsedWithTwoView.id)
            }
        }
        iconPanel.changeMargin(1)
    }

    override fun gonePanelGravityText() {
        panelGravityText.gone()
        val height = calculateHeight()
        parentFragmentManager.setResult(
            CALL_METHOD_KEY,
            METHOD_KEY to ADD_HEIGHT_CONTAINER, DATA_KEY to height
        )
        if (currentState == BottomSheetBehavior.STATE_COLLAPSED) {
            if (answerLayout.isVisible()) {
                changeBottomConstraintRichEditor(horizontalGuideCollapsedWithPanelStyle.id)
            } else {
                changeBottomConstraintRichEditor(horizontalGuideCollapsed.id)
            }
        }
        iconPanel.changeMargin(8)
    }

    override fun showPanelGravityText() {
        super.showPanelGravityText()
        if (answerLayout.isActivated) answerLayout.show()
        panelGravityText.show()
        panelStyleText.gone()
        richEditor.run {
            show()
            if (html?.replace("<br>", "")?.isNotEmpty() == true && allViewsIsUpload)
                sendButton.show()
        }
        val height = calculateHeight() + heightTextStylePanel
        parentFragmentManager.setResult(
            CALL_METHOD_KEY,
            METHOD_KEY to ADD_HEIGHT_CONTAINER, DATA_KEY to height
        )
        if (currentState == BottomSheetBehavior.STATE_COLLAPSED) {
            if (!answerLayout.isVisible()) {
                changeBottomConstraintRichEditor(horizontalGuideCollapsedWithPanelStyle.id)
            } else {
                changeBottomConstraintRichEditor(horizontalGuideCollapsedWithTwoView.id)
            }
        }
        iconPanel.changeMargin(1)
    }

    override fun calculateHeight(): Int {
        var height = heightIconPanel + pushUpDown.height / 2 + heightEditText
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
        iconPanel.changeMargin(8)
        stateBeforeShowMediaRecycler = currentState
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
            if (html?.replace("<br>", "")?.isNotEmpty() == true && allViewsIsUpload)
                sendButton.show()
        }
        returnStateToOriginal()
    }

    private fun returnStateToOriginal() {
        if (stateBeforeShowMediaRecycler == BottomSheetBehavior.STATE_COLLAPSED ||
            stateBeforeShowMediaRecycler == BottomSheetBehavior.STATE_SETTLING
        ) {
            parentFragmentManager.setResult(
                CALL_METHOD_KEY,
                METHOD_KEY to CHANGE_STATE_BOTTOM_SHEET_DATA,
                DATA_KEY to BottomSheetBehavior.STATE_COLLAPSED
            )
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

    private fun setupPanelGravityText() {
        setupLeftGravityView()
        setupCenterGravityView()
        setupRightGravityView()
    }

    private fun setupLeftGravityView() {
        leftGravityButton.setOnClickListener {
            richEditor.setAlignLeft()
        }
    }

    private fun setupCenterGravityView() {
        centerGravityButton.setOnClickListener {
            richEditor.setAlignCenter()
        }
    }

    private fun setupRightGravityView() {
        rightGravityButton.setOnClickListener {
            richEditor.setAlignRight()
        }
    }

    override fun attachGallery() {
        presenter.attachMedia(presenter::loadImage, galleryAdapter.getChooseMedias())
    }

    override fun attachVideo() {
        presenter.attachMedia(presenter::loadVideo, videoAdapter.getChooseMedias())
    }

    override fun attachAudio() {
        presenter.attachMedia(presenter::loadAudio, audioAdapter.getChooseMedias())
    }

    override fun attachFromCamera() {
        presenter.attachFromCamera()
    }

    override fun changeStateToHalfExpanded() {
        parentFragmentManager.setResult(
            CALL_METHOD_KEY,
            METHOD_KEY to CHANGE_STATE_BOTTOM_SHEET_DATA, DATA_KEY
                    to BottomSheetBehavior.STATE_HALF_EXPANDED
        )
    }

    override fun changeStateViewAfterAddMedia() {
        mediaRecyclerView.gone()
        icAttachFile.activated(false)
        panelAddFile.gone()
        amountFiles.gone()
        btnAdd.gone()
        if (answerLayout.isActivated) answerLayout.show()
        richEditor.show()
        returnStateToOriginal()
        dialogDelegate.showProgressDialog()
    }

    override fun stateSettling() {
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
        changeBottomConstraintForView(horizontalGuideEnd.id)
    }

    override fun stateExpanded() {
        if (answerLayout.isActivated) answerLayout.show()
    }

    override fun stateHalfExpanded() {
        if (answerLayout.isActivated && !icAttachFile.isActivated) answerLayout.show()
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
        if ((panelGravityText.isVisible || panelStyleText.isVisible) && answerLayout.isVisible) {
            changeBottomConstraintRichEditor(horizontalGuideCollapsedWithTwoView.id)
        } else if (panelGravityText.isVisible || panelStyleText.isVisible || answerLayout.isVisible) {
            changeBottomConstraintRichEditor(horizontalGuideCollapsedWithPanelStyle.id)
        } else {
            changeBottomConstraintRichEditor(horizontalGuideCollapsed.id)
        }
        restoreAllViewForCollapsedState()
        super.stateCollapsed()
    }

    override fun commentCreated(commentEntity: CommentEntity) {
        parentFragmentManager.setResult(CALL_METHOD_KEY, METHOD_KEY to COMMENT_CREATED_DATA)
        parentFragmentManager.setResult(
            CALL_METHOD_KEY,
            METHOD_KEY to ADD_HEIGHT_CONTAINER, DATA_KEY to
                    heightEditText + heightIconPanel + pushUpDown.height / 2
        )
        parentFragmentManager.setResult(
            CALL_METHOD_KEY,
            METHOD_KEY to CHANGE_STATE_BOTTOM_SHEET_DATA, DATA_KEY to
                    BottomSheetBehavior.STATE_COLLAPSED
        )
    }

    override fun answerToCommentCreated(commentEntity: CommentEntity) {
        parentFragmentManager.setResult(
            CALL_METHOD_KEY,
            METHOD_KEY to ANSWER_COMMENT_CREATED_DATA
        )
        parentFragmentManager.setResult(
            CALL_METHOD_KEY,
            METHOD_KEY to ADD_HEIGHT_CONTAINER, DATA_KEY to
                    heightEditText + heightIconPanel + pushUpDown.height / 2
        )
        parentFragmentManager.setResult(
            CALL_METHOD_KEY,
            METHOD_KEY to CHANGE_STATE_BOTTOM_SHEET_DATA, DATA_KEY to
                    BottomSheetBehavior.STATE_COLLAPSED
        )
        answerLayout.gone()
        answerLayout.activated(false)
    }

    override fun showCommentUploading(show: Boolean) {
        parentFragmentManager.setResult(
            CALL_METHOD_KEY,
            METHOD_KEY to SHOW_COMMENT_UPLOADING_DATA, DATA_KEY to
                    show
        )
    }

    override fun showImageUploadingStarted(chooseMedia: ChooseMedia) {
        when (chooseMedia.type) {
            MediaType.AUDIO -> {
                namesMap[chooseMedia.url] = chooseMedia.name
                richEditor.insertAudio(chooseMedia.url)
            }
            MediaType.IMAGE -> {
                val fileEntity = FileEntity(
                    0, chooseMedia.url, false, "",
                    chooseMedia.name, 0, 0
                )
                namesMap[chooseMedia.url] = fileEntity.title
                richEditor.insertImage(chooseMedia.url, "alt")

            }
            MediaType.VIDEO -> {
                val fileEntity = FileEntity(
                    0, chooseMedia.url, false, "",
                    chooseMedia.name, 0, 0,
                    chooseMedia.urlPreview, chooseMedia.duration
                )
                namesMap[chooseMedia.url] = fileEntity.title
                richEditor.insertVideo(chooseMedia.url)
            }
        }
        loadingMedias[chooseMedia.url] = LoadMediaType.START
        sendButton.hide()
        childFragmentManager.setResult(
            PROGRESS_KEY,
            PROGRESS_MODEL_KEY to ProgressMediaModel(chooseMedia, LoadMediaType.START)
        )
    }

    override fun showImageUploadingProgress(progress: Float, chooseMedia: ChooseMedia) {
        val type = LoadMediaType.PROGRESS.apply {
            this.progress = progress
        }
        loadingMedias[chooseMedia.url] = type
        childFragmentManager.setResult(
            PROGRESS_KEY,
            PROGRESS_MODEL_KEY to ProgressMediaModel(chooseMedia, type)
        )
    }

    override fun showImageUploadingError(chooseMedia: ChooseMedia) {
        loadingMedias[chooseMedia.url] = LoadMediaType.ERROR
        childFragmentManager.setResult(
            PROGRESS_KEY,
            PROGRESS_MODEL_KEY to ProgressMediaModel(chooseMedia, LoadMediaType.ERROR)
        )
    }

    override fun showImageUploaded(chooseMedia: ChooseMedia) {
        childFragmentManager.setResult(
            PROGRESS_KEY,
            PROGRESS_MODEL_KEY to ProgressMediaModel(chooseMedia, LoadMediaType.UPLOAD)
        )
        allViewsIsUpload = true
        loadingMedias[chooseMedia.url] = LoadMediaType.UPLOAD
        loadingMedias.values.forEach { type ->
            if (type != LoadMediaType.UPLOAD) {
                allViewsIsUpload = false
                return@forEach
            }
        }
        if (allViewsIsUpload) {
            sendButton.show()
        } else {
            sendButton.hide()
        }
    }

    override fun hideSwipeLayout() {
        parentFragmentManager.setResult(
            CALL_METHOD_KEY,
            METHOD_KEY to HIDE_SWIPE_DATA
        )
    }

    private fun restoreAllViewForCollapsedState() {
        if (answerLayout.isActivated) answerLayout.show()
        panelAddFile.gone()
        richEditor.run {
            show()
            if (html?.replace("<br>", "")?.isNotEmpty() == true &&
                allViewsIsUpload
            ) {
                sendButton.show()
            }
        }
        pushUpDown.background =
            context?.let { ContextCompat.getDrawable(it, R.drawable.btn_push_down) }
    }

    private fun setupEditComment(comment: CommentEntity.Comment) {
        answerLayout.activated(false)
        answerLayout.gone()
        richEditor.html = changeNameOnUrl(comment)
        presenter.addMediaUrl(comment)
        commentId = comment.id
    }

    private fun changeNameOnUrl(comment: CommentEntity.Comment): String {
        namesMap.clear()
        var text = comment.text
        comment.audios.forEach {
            if (text.contains(it.nameSong)) {
                namesMap[it.urlFile] = it.nameSong
                text = text.substringBefore(it.nameSong) + it.urlFile +
                        text.substringAfter(it.nameSong + MEDIA_PREFIX)
            }
        }
        comment.images.forEach {
            if (text.contains(it.title)) {
                namesMap[it.file] = it.title
                text = text.substringBefore(it.title) + it.file +
                        text.substringAfter(it.title + MEDIA_PREFIX)
            }
        }
        comment.videos.forEach {
            if (text.contains(it.title)) {
                namesMap[it.file] = it.title
                text = text.substringBefore(it.title) + it.file +
                        text.substringAfter(it.title + MEDIA_PREFIX)
            }
        }
        return text
    }


    private fun LinearLayout.changeMargin(dp: Int) {
        (this.layoutParams as ConstraintLayout.LayoutParams).topMargin = context.dpToPx(dp)
    }
}
