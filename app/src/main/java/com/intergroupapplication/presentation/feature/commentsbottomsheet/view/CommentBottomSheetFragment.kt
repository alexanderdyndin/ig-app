package com.intergroupapplication.presentation.feature.commentsbottomsheet.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.presentation.base.BaseBottomSheetFragment
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.presenter.BottomSheetPresenter
import com.intergroupapplication.presentation.feature.commentsdetails.viewmodel.CommentsViewModel
import com.intergroupapplication.presentation.listeners.RightDrawableListener
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import kotlinx.android.synthetic.main.fragment_comment_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_create_post.*
import kotlinx.android.synthetic.main.item_comment.*
import kotlinx.android.synthetic.main.layout_attach_image.*
import kotlinx.android.synthetic.main.layout_attach_image.view.*
import kotlinx.android.synthetic.main.layout_attach_image.view.darkCard
import kotlinx.android.synthetic.main.layout_attach_image.view.detachImage
import kotlinx.android.synthetic.main.layout_attach_image.view.imageUploadingProgressBar
import kotlinx.android.synthetic.main.layout_attach_image.view.refreshContainer
import kotlinx.android.synthetic.main.layout_attach_image.view.stopUploading
import kotlinx.android.synthetic.main.layout_audio_in_create_post.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import javax.inject.Inject

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

    private val heightView by lazy { requireContext().dpToPx(100) }
    private val heightEditTextWithFiveLine by lazy { requireContext().dpToPx(180) }
    private val heightEditText by lazy { requireContext().dpToPx(53) }
    private val heightLineInEditText by lazy { requireContext().dpToPx(16) }
    private val heightAnswerPanel by lazy { requireContext().dpToPx(35) }

    @ProvidePresenter
    fun providePresenter(): BottomSheetPresenter = presenter

    override fun layoutRes() = R.layout.fragment_comment_bottom_sheet

    override fun getSnackBarCoordinator() = bottom_sheet_coordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, modelFactory)[CommentsViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
                        if (answer_layout.isVisible())
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
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpRightDrawableListener() {
        rightDrawableListener.clickListener = {
            CommentsViewModel.publishSubject
                    .onNext(Pair(CREATE_COMMENT_DATA,Pair(createCommentCustomView.createFinalText(), presenter)))
            loadingViews.clear()
            chooseMedias.clear()
            createCommentCustomView.removeAllViews()
            createCommentCustomView.createAllMainView()
            createCommentCustomView.textPost.run {
                hint = requireContext()
                        .getString(R.string.write_your_comment)
                setCompoundDrawablesWithIntrinsicBounds(null, null,
                        null, null)
            }
        }
        createCommentCustomView.textPost.setOnTouchListener(rightDrawableListener)
    }

    fun answerComment(comment:CommentEntity.Comment){
        answer_layout.show()
        answer_layout.activated(true)
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
        if (answer_layout.isActivated){
            answer_layout.show()
        }
        mediaRecyclerView.gone()
    }

    override fun attachFileActivated() {
        createCommentCustomView.gone()
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
        changeBottomConstraintForRecyclerView(horizontal_guide_end.id)
    }

    override fun stateExpanded() {

    }

    override fun stateHalfExpanded() {
        changeBottomConstraintForRecyclerView(horizontal_guide_center.id)
    }

    override fun stateDragging() {
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
        changeBottomConstraintForRecyclerView(horizontal_guide_end.id)
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
        answer_layout.gone()
        answer_layout.activated(false)
    }

    override fun showCommentUploading(show: Boolean) {
        CommentsViewModel.publishSubject.onNext(Pair(SHOW_COMMENT_UPLOADING_DATA,show))
    }

    override fun showImageUploadingStarted(chooseMedia: ChooseMedia) {
        if (chooseMedia.url.contains(".mp3") || chooseMedia.url.contains(".mpeg") || chooseMedia.url.contains(".wav")){
            loadingViews[chooseMedia.url] = layoutInflater.inflate(R.layout.layout_audio_in_create_post,
                    createCommentCustomView.audioContainer, false)
            loadingViews[chooseMedia.url]?.let {
                it.trackName?.text = chooseMedia.name
            }
            createCommentCustomView.addMusic(chooseMedia.name,loadingViews[chooseMedia.url])
        }
        else {
            loadingViews[chooseMedia.url] = layoutInflater.inflate(R.layout.layout_attach_image,
                   createCommentCustomView.imageContainer, false)
            loadingViews[chooseMedia.url]?.let {
                it.imagePreview?.let { draweeView ->
                    imageLoadingDelegate.loadImageFromFile(chooseMedia.url, draweeView)
                }
            }
            createCommentCustomView.addImageOrVideo(chooseMedia.url.substringAfterLast("/"),
                    loadingViews[chooseMedia.url])
        }
        createCommentCustomView.textPost.setCompoundDrawablesWithIntrinsicBounds(null, null,
                null, null)
        prepareListeners(loadingViews[chooseMedia.url], chooseMedia)
        imageUploadingStarted(loadingViews[chooseMedia.url])

        if (loadingViews.size == chooseMedias.size && createCommentCustomView.currentContainerIsLast()){
            createCommentCustomView.createAllMainView()
            createCommentCustomView.textPost.hint = requireContext()
                    .getString(R.string.write_your_comment)
            controlCommentEditTextChanges()
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
            createCommentCustomView.textPost.setCompoundDrawablesWithIntrinsicBounds(null, null,
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
        CommentsViewModel.publishSubject.onNext(Pair(HIDE_SWIPE_DATA, null))
    }

    private fun detachImage(path: String) {
        createCommentCustomView.listImageContainers.forEach {container->
            container.removeView(loadingViews[path])
        }
        createCommentCustomView.listAudioContainers.forEach {container->
            container.removeView(loadingViews[path])
        }
        createCommentCustomView.detachName(loadingViews[path])
        loadingViews.remove(path)
        chooseMedias.removeChooseMedia(path)
        if(loadingViews.isEmpty()){
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
        if(answer_layout.isVisible()){
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