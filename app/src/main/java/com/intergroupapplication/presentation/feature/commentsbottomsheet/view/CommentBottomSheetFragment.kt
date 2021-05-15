package com.intergroupapplication.presentation.feature.commentsbottomsheet.view

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.presentation.base.BaseBottomSheetFragment
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.presenter.BottomSheetPresenter
import com.intergroupapplication.presentation.listeners.RightDrawableListener
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import kotlinx.android.synthetic.main.fragment_comment_bottom_sheet.*
import kotlinx.android.synthetic.main.layout_attach_image.*
import kotlinx.android.synthetic.main.layout_attach_image.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import javax.inject.Inject

class CommentBottomSheetFragment: BaseBottomSheetFragment(),BottomSheetView, Validator.ValidationListener {

    lateinit var callback: Callback

    @Inject
    @InjectPresenter
    lateinit var presenter: BottomSheetPresenter

    @Inject
    lateinit var validator: Validator

    @Inject
    lateinit var rightDrawableListener: RightDrawableListener

    private val loadingViews: MutableMap<String, View?> = mutableMapOf()

    private val heightView by lazy { requireContext().dpToPx(100) }
    private val heightEditTextWithFiveLine by lazy { requireContext().dpToPx(156) }
    private val heightEditText by lazy { requireContext().dpToPx(53) }
    private val heightLineInEditText by lazy { requireContext().dpToPx(16) }
    private val heightAnswerPanel by lazy { requireContext().dpToPx(35) }

    @ProvidePresenter
    fun providePresenter(): BottomSheetPresenter = presenter

    override fun layoutRes() = R.layout.fragment_comment_bottom_sheet

    override fun getSnackBarCoordinator() = bottom_sheet_coordinator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        commentEditText = view.findViewById(R.id.commentEditText)
        prepareEditText()
        controlCommentEditTextChanges()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun prepareEditText() {
        commentEditText.isVerticalScrollBarEnabled = true
        commentEditText.maxLines = 5
        commentEditText.setScroller(Scroller(requireContext()))
        commentEditText.movementMethod = ScrollingMovementMethod()
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
                        if (answer_layout.isVisible())
                            height += heightAnswerPanel
                        callback.addHeightContainer(height)
                    }
                }
                .let(compositeDisposable::add)
        setUpRightDrawableListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpRightDrawableListener() {
        commentEditText.setOnTouchListener(rightDrawableListener)
        rightDrawableListener.clickListener = {
            validator.validate()
            loadingViews.clear()
            chooseMedias.clear()
            postContainer.removeAllViews()
            commentEditText.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    null, null)
        }
    }

    fun answerComment(comment:CommentEntity.Comment){
        answer_layout.show()
        answer_layout.activated(true)
        responseToUser
                .apply {
                    text = comment.commentOwner?.firstName
                            ?: getString(R.string.unknown_user)
                }
        val height = commentEditText.height + iconPanel.height + pushUpDown.height / 2+ heightAnswerPanel
        callback.addHeightContainer(height)
        //commentEditText.showKeyboard()
    }

    override fun attachFileNotActivated() {
        commentEditText.show()
        panelAddFile.gone()
        btnAdd.gone()
        amountFiles.gone()
        postContainer.show()
        if (answer_layout.isActivated){
            answer_layout.show()
        }
        mediaRecyclerView.gone()
    }

    override fun attachFileActivated() {
        postContainer.gone()
        commentEditText.gone()
        answer_layout.gone()
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
        if (callback.getState() == BottomSheetBehavior.STATE_COLLAPSED){
            callback.changeStateBottomSheet(BottomSheetBehavior.STATE_HALF_EXPANDED)
        }
    }

    override fun changeStateViewAfterAddMedia() {
        mediaRecyclerView.gone()
        icAttachFile.activated(false)
        panelAddFile.gone()
        postContainer.show()
        amountFiles.gone()
        btnAdd.gone()
        commentEditText.show()
    }

    override fun closeKeyboard() {
        try {
            commentEditText.dismissKeyboard()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stateSettling() {
        Timber.tag("tut_state").d("SETTING")
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
        changeBottomConstraintForRecyclerView(horizontal_guide_end.id)
    }

    override fun stateExpanded() {
        val metrics = Resources.getSystem().displayMetrics
        var height = metrics.heightPixels-iconPanel.height - pushUpDown.height / 2
        if (loadingViews.isNotEmpty())
            height -=heightView
        if (answer_layout.isVisible())
            height -= heightAnswerPanel
        commentEditText.maxLines = height/heightLineInEditText - 1
        Timber.tag("tut_state").d("EXPANDED")
    }

    override fun stateHalfExpanded() {
        val metrics = Resources.getSystem().displayMetrics
        var height = metrics.heightPixels*0.6-iconPanel.height - pushUpDown.height / 2
        if (loadingViews.isNotEmpty())
            height -=heightView
        if (answer_layout.isVisible())
            height -= heightAnswerPanel
        commentEditText.maxLines = (height/heightLineInEditText).toInt() - 1
        changeBottomConstraintForRecyclerView(horizontal_guide_center.id)
        Timber.tag("tut_state").d("HALF_EXPANDED")
    }

    override fun stateDragging() {
        Timber.tag("tut_state").d("DRAGGING")
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
        changeBottomConstraintForRecyclerView(horizontal_guide_end.id)
    }

    override fun stateHidden() {
        Timber.tag("tut_state").d("HIDDEN")
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
    }

    override fun stateCollapsed() {
        val height = restoreHeightEditText()
        restoreAllViewForCollapsedState()
        callback.addHeightContainer(height)
        chooseMedias.clear()
        chooseMedias.addAll(loadingViews.keys.map {
            ChooseMedia(it)
        })
        super.stateCollapsed()
    }

    override fun commentCreated(commentEntity: CommentEntity) {
        callback.commentCreated(commentEntity)
        callback.addHeightContainer(heightEditText + iconPanel.height + pushUpDown.height / 2)
        callback.changeStateBottomSheet(BottomSheetBehavior.STATE_COLLAPSED)
    }

    override fun answerToCommentCreated(commentEntity: CommentEntity) {
        callback.answerToCommentCreated(commentEntity)
        callback.addHeightContainer(heightEditText + iconPanel.height + pushUpDown.height / 2)
        callback.changeStateBottomSheet(BottomSheetBehavior.STATE_COLLAPSED)
        answer_layout.gone()
        answer_layout.activated(false)
    }

    override fun showCommentUploading(show: Boolean) {
        callback.showCommentUploading(show)
    }

    override fun showImageUploadingStarted(chooseMedia: ChooseMedia) {
        loadingViews[chooseMedia.url] = layoutInflater.inflate(R.layout.layout_attach_image, postContainer, false)
        loadingViews[chooseMedia.url]?.let {
            it.imagePreview?.let { draweeView ->
                val type = MimeTypeMap.getFileExtensionFromUrl(chooseMedia.url)
                val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(type) ?: ""
                if (mime in listOf("audio/mpeg", "audio/aac", "audio/wav")) {
                    imageLoadingDelegate.loadImageFromResources(R.drawable.variant_10, draweeView)
                    it.nameView?.text = chooseMedia.trackName
                }
                else
                    imageLoadingDelegate.loadImageFromFile(chooseMedia.url, draweeView)
            }
        }
        commentEditText.setCompoundDrawablesWithIntrinsicBounds(null, null,
               null, null)
        postContainer.addView(loadingViews[chooseMedia.url])
        prepareListeners(loadingViews[chooseMedia.url], chooseMedia)
        imageUploadingStarted(loadingViews[chooseMedia.url])
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

    override fun showImageUploaded(path: String) {
        loadingViews[path]?.apply {
            darkCard?.hide()
            stopUploading?.hide()
            imageUploadingProgressBar?.hide()
            detachImage?.show()
        }
        var countUpload = false
        loadingViews.values.forEach{ _ ->
            if (imageUploadingProgressBar.progress<100){
                countUpload = true
                return@forEach
            }
        }
        if (!countUpload || loadingViews.size == 1){
            commentEditText.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_send), null)
            setUpRightDrawableListener()
        }
    }

    private fun prepareListeners(uploadingView: View?, chooseMedia: ChooseMedia) {
        uploadingView?.apply {
            refreshContainer.setOnClickListener {
                this.imageUploadingProgressBar?.progress = 0f
                presenter.retryLoading(chooseMedia)
                imageUploadingStarted(uploadingView)
            }
            stopUploading?.setOnClickListener {
                presenter.cancelUploading(chooseMedia.url)
                detachImage(chooseMedia.url)
            }
            detachImage?.setOnClickListener {
                presenter.removeContent(chooseMedia.url)
                detachImage(chooseMedia.url)
            }
        }
    }

    override fun hideSwipeLayout() {
        callback.hideSwipeLayout()
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

    private fun detachImage(path: String) {
        postContainer.removeView(loadingViews[path])
        loadingViews.remove(path)
        chooseMedias.removeChooseMedia(path)
        if(loadingViews.isEmpty()){
            commentEditText.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    null, null)
            val height = commentEditText.height + iconPanel.height + pushUpDown.height/2
            callback.addHeightContainer(height)
        }
    }

    private fun restoreHeightEditText(): Int {
        commentEditText.maxLines = 5
        var height = if (commentEditText.lineCount > 5) {
            heightEditTextWithFiveLine
        } else {
            commentEditText.height + iconPanel.height + pushUpDown.height / 2
        }
        if (loadingViews.isNotEmpty()) {
            height += heightView
        }
        if(answer_layout.isVisible()){
            height += heightAnswerPanel
        }
        return height
    }

    private fun restoreAllViewForCollapsedState() {
        panelAddFile.gone()
        commentEditText.show()
        postContainer.show()
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_down)
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
}