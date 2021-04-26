package com.intergroupapplication.presentation.feature.bottomsheet.view

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Scroller
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.VideoModel
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.bottomsheet.adapter.*
import com.intergroupapplication.presentation.feature.bottomsheet.presenter.BottomSheetPresenter
import com.intergroupapplication.presentation.feature.bottomsheet.viewmodel.BottomViewModel
import com.intergroupapplication.presentation.listeners.RightDrawableListener
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*
import kotlinx.android.synthetic.main.layout_attach_image.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class BottomSheetFragment(val callback: Callback): BaseFragment(), BottomSheetView, Validator.ValidationListener {


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

    @NotEmpty(messageResId = R.string.comment_should_contain_text)
    lateinit var commentEditText: AppCompatEditText

   // @Inject
    //lateinit var callback: Callback

    @Inject
    @InjectPresenter
    lateinit var presenter: BottomSheetPresenter

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: BottomViewModel

    @ProvidePresenter
    fun providePresenter(): BottomSheetPresenter = presenter

    private val attachImageView by lazy { layoutInflater.inflate(R.layout.layout_attach_image, postContainer, false) }

    private val loadingViews: MutableMap<String, View?> = mutableMapOf()

    override fun layoutRes() = R.layout.fragment_bottom_sheet

    override fun getSnackBarCoordinator() = bottom_sheet_coordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, modelFactory)[BottomViewModel::class.java]
        settingAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        commentEditText = view.findViewById(R.id.commentEditText)
        mediaRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            addItemDecoration(ItemOffsetDecoration(context,R.dimen.lb_page_indicator_arrow_shadow_offset))
        }
        prepareEditText()
        controlCommentEditTextChanges()
        setUpAddFilePanel()
    }

    private fun setUpAddFilePanel() {
        icAttachFile.apply {
            setOnClickListener {
                if (isActivated) {
                    isActivated = false
                    commentEditText.visibility = View.VISIBLE
                    panelAddFile.visibility = View.GONE
                    mediaRecyclerView.visibility = View.GONE
                } else {
                    this.isActivated = true
                    commentEditText.visibility = View.GONE
                    panelAddFile.visibility = View.VISIBLE
                    //callback.changeStateBottomSheet(BottomSheetBehavior.STATE_HALF_EXPANDED)
                }
            }
        }

        galleryButton.setOnClickListener {
            if (loadingViews.count() >= 10) {
                Toast.makeText(requireContext(), "Не больше 10 вложений", Toast.LENGTH_SHORT).show()
            } else {
                mediaRecyclerView.adapter = galleryAdapter
                setVisibilityForAddFiles()
                //dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                  //      mapOf(R.id.fromCamera to { presenter.attachFromCamera() }, R.id.fromGallery to { presenter.attachFromGallery() }))
            }
        }

        musicButton.setOnClickListener {
            if (loadingViews.count() >= 10) {
                Toast.makeText(requireContext(), "Не больше 10 вложений", Toast.LENGTH_SHORT).show()
            } else {
                mediaRecyclerView.adapter = audioAdapter
                setVisibilityForAddFiles()
              //  presenter.attachAudio()
            }
        }

        videoButton.setOnClickListener {
            if (loadingViews.count() >= 10) {
                Toast.makeText(requireContext(), "Не больше 10 вложений", Toast.LENGTH_SHORT).show()
            } else {
                mediaRecyclerView.adapter = videoAdapter
                setVisibilityForAddFiles()
            }
        }

        amountFiles.setOnClickListener {

        }

        btnAdd.setOnClickListener {

        }

        playlistButton.setOnClickListener {
            Toast.makeText(requireContext(), "Пока недоступно", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setVisibilityForAddFiles() {
        mediaRecyclerView.visibility = View.VISIBLE
        amountFiles.visibility = View.VISIBLE
        btnAdd.visibility = View.VISIBLE
    }

    private fun settingAdapter() {
        galleryAdapter.apply {
            photos.addAll(addGalleryUri(MediaStore.Images.ImageColumns.DATA,uriConstants =  MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
            notifyDataSetChanged()
        }
        audioAdapter.apply {
            audios.addAll(addGalleryUri(MediaStore.Audio.AudioColumns.DATA, uriConstants = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI))
            notifyDataSetChanged()
        }
        videoAdapter.apply {
            videos.addAll(addVideoUri(MediaStore.Video.VideoColumns.DATA,MediaStore.Video.VideoColumns.DURATION,uriConstants =  MediaStore.Video.Media.EXTERNAL_CONTENT_URI))
            notifyDataSetChanged()
        }
    }

    private fun addGalleryUri(mediaConstants: String, uriConstants: Uri):MutableList<String> {
        val listUrlImage = mutableListOf<String>()
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
                listUrlImage.add(path)
            }
        }
        listUrlImage.reverse()
        return listUrlImage
    }

    private fun addVideoUri(vararg mediaConstants: String, uriConstants: Uri):MutableList<VideoModel>{
        val listUrlImage = mutableListOf<VideoModel>()
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
                val path_second = cursor.getInt(cursor.getColumnIndexOrThrow(mediaConstants[1]))
                val calendar = GregorianCalendar()
                calendar.time = Date(path_second.toLong())
                val minute_calendar = calendar.get(GregorianCalendar.MINUTE)
                val second_calendar = calendar.get(GregorianCalendar.SECOND)
                val minute = if (minute_calendar<10) "0$minute_calendar" else "$minute_calendar"
                val second = if (second_calendar<10) "0$second_calendar" else "$second_calendar"
                listUrlImage.add(VideoModel(path,"$minute:$second"))
            }
        }
        listUrlImage.reverse()
        return listUrlImage
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
        // panelAddFile.visibility = View.VISIBLE
        // commentEditText.visibility = View.GONE
        postContainer.visibility = View.GONE
        Timber.tag("tut_state").d("SETTING")
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
    }

    private fun stateExpanded() {
        //commentEditText.visibility = View.GONE
        postContainer.visibility = View.GONE
        commentEditText.maxLines = 40
        Timber.tag("tut_state").d("EXPANDED")
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
    }

    private fun stateHalfExpanded() {
        //panelAddFile.visibility = View.VISIBLE
        //commentEditText.visibility = View.GONE
        postContainer.visibility = View.GONE
        commentEditText.maxLines = 20
        Timber.tag("tut_state").d("HALF_EXPANDED")
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
    }

    private fun stateDragging() {
        // panelAddFile.visibility = View.VISIBLE
        //commentEditText.visibility = View.GONE
        postContainer.visibility = View.GONE
        Timber.tag("tut_state").d("DRAGGING")
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
    }

    private fun stateHidden() {
        // panelAddFile.visibility = View.VISIBLE
        // commentEditText.visibility = View.GONE
        postContainer.visibility = View.GONE
        Timber.tag("tut_state").d("HIDDEN")
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_up)
    }

    private fun stateCollapsed() {
        commentEditText.maxLines = 5
        var height = if (commentEditText.lineCount > 5) {
            470
        } else {
            commentEditText.height + iconPanel.height + pushUpDown.height / 2
        }
        if (loadingViews.isNotEmpty()) {
            height += 300
        }
        panelAddFile.visibility = View.GONE
        commentEditText.visibility = View.VISIBLE
        postContainer.visibility = View.VISIBLE
        mediaRecyclerView.visibility = View.GONE
        callback.addHeightContainer(height)
        icAttachFile.isActivated = false
        Timber.tag("tut_state").d("_COLLAPSED")
        pushUpDown.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_push_down)
    }


    private fun controlCommentEditTextChanges() {
        RxTextView.textChanges(commentEditText).subscribe {
            if(commentEditText.isFocused) {
                var height = commentEditText.height + iconPanel.height + pushUpDown.height / 2
                if (loadingViews.isNotEmpty()) {
                    height += 300
                }
                callback.addHeightContainer(height)
            }
        }.let(compositeDisposable::add)
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
                }
                .let(compositeDisposable::add)
        setUpRightDrawableListener()
    }

    private fun setUpRightDrawableListener() {
        commentEditText.setOnTouchListener(rightDrawableListener)
        rightDrawableListener.clickListener = {
            validator.validate()
            loadingViews.clear()
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
        loadingViews[path] = attachImageView
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
        fun addHeightContainer(height: Int)
        fun commentCreated(commentEntity: CommentEntity)
        fun answerToCommentCreated(commentEntity: CommentEntity)
        fun showCommentUploading(show: Boolean)
        fun hideSwipeLayout()
    }

}