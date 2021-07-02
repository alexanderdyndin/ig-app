package com.intergroupapplication.presentation.feature.createpost.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.budiyev.android.circularprogressbar.CircularProgressBar
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.model.TextType
import com.intergroupapplication.databinding.FragmentCreatePostBinding
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.TEXT
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.customview.AutoCloseBottomSheetBehavior
import com.intergroupapplication.presentation.customview.RichEditor
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.chooseMedias
import com.intergroupapplication.presentation.feature.createpost.presenter.CreatePostPresenter
import com.intergroupapplication.presentation.feature.postbottomsheet.view.PostBottomSheetFragment
import com.intergroupapplication.presentation.feature.group.view.GroupFragment
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadAudioPlayerView
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadVideoPlayerView
import io.reactivex.exceptions.CompositeException
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import javax.inject.Inject

open class CreatePostFragment : BaseFragment(), CreatePostView,PostBottomSheetFragment.Callback {

    companion object{
        const val MEDIA_INTERACTION_REQUEST_CODE = "media_interaction_request_code"
        const val METHOD_KEY = "method_key"
        const val CHOOSE_MEDIA_KEY = "choose_media_key"
        const val COLOR_KEY = "color_key"
        const val RETRY_LOADING_METHOD_CODE = 0
        const val CANCEL_LOADING_METHOD_CODE = 1
        const val REMOVE_CONTENT_METHOD_CODE = 2
        const val ACTIVATED_BOLD = 3
        const val ACTIVATED_ITALIC = 4
        const val ACTIVATED_UNDERLINE = 5
        const val ACTIVATED_STRIKETHROUGH = 6
        const val NOT_ACTIVATED_ALL_BUTTONS = 7
        const val SET_JUSTIFY_LEFT = 8
        const val SET_JUSTIFY_CENTER = 9
        const val SET_JUSTIFY_RIGHT = 10
        const val CHANGE_COLOR = 11
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: CreatePostPresenter

    @ProvidePresenter
    fun providePresenter(): CreatePostPresenter = presenter

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    private lateinit var bottomSheetBehaviour: AutoCloseBottomSheetBehavior<FrameLayout>

    protected val bottomFragment by lazy { PostBottomSheetFragment() }

    private val createPostBinding by viewBinding(FragmentCreatePostBinding::bind)

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_create_post

    protected val loadingViews: MutableMap<String, View?> = mutableMapOf()
    private val namesMap = mutableMapOf<String,String>()
    private val finalNamesMedia = mutableListOf<String>()

    override fun getSnackBarCoordinator(): CoordinatorLayout = createPostBinding.createPostCoordinator

    protected lateinit var richEditor:RichEditor
    protected lateinit var publishBtn:TextView

    private lateinit var groupId: String

    override fun viewCreated() {
        richEditor = createPostBinding.richEditor.apply {
            setEditorFontSize(18)
            val padding = context.dpToPx(4)
            setEditorPadding(padding, padding, padding, padding)
            setEditorFontColor(ContextCompat.getColor(context, R.color.whiteTextColor))
            setPlaceholder(context.getString(R.string.add_photo_or_text))
            setBackgroundColor(Color.parseColor("#12161E"))
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            decorationStateListener = object :RichEditor.OnDecorationStateListener{

                override fun onStateChangeListener(text: String, types: List<TextType>) {
                    setFragmentResult(bundleOf(METHOD_KEY
                            to NOT_ACTIVATED_ALL_BUTTONS))
                    types.forEach { type ->
                        when {
                            type.name.contains("FONT_COLOR") -> {
                                setFragmentResult(bundleOf(METHOD_KEY to CHANGE_COLOR,
                                    COLOR_KEY to type.color))
                            }
                            else -> {
                                when(type){
                                    TextType.BOLD -> {
                                        setFragmentResult(bundleOf(METHOD_KEY
                                                to ACTIVATED_BOLD))
                                    }
                                    TextType.ITALIC ->{
                                        setFragmentResult(bundleOf(METHOD_KEY
                                                to ACTIVATED_ITALIC))
                                    }
                                    TextType.UNDERLINE ->{
                                        setFragmentResult(bundleOf(METHOD_KEY
                                                to ACTIVATED_UNDERLINE))
                                    }
                                    TextType.STRIKETHROUGH ->{
                                        setFragmentResult(bundleOf(METHOD_KEY
                                                to ACTIVATED_STRIKETHROUGH))
                                    }
                                    TextType.JUSTIFYLEFT->{
                                        setFragmentResult(bundleOf(METHOD_KEY
                                                to SET_JUSTIFY_LEFT))
                                    }
                                    TextType.JUSTIFYCENTER->{
                                        setFragmentResult(bundleOf(METHOD_KEY
                                                to SET_JUSTIFY_CENTER))
                                    }
                                    TextType.JUSTIFYRIGHT->{
                                        setFragmentResult(bundleOf(METHOD_KEY
                                                to SET_JUSTIFY_RIGHT))
                                    }
                                    else -> Timber.tag("else_type").d(type.name)
                                }
                            }
                        }
                    }
                }
            }
        }
        publishBtn = createPostBinding.navigationToolbar.publishBtn
        chooseMedias.clear()
        groupId = arguments?.getString(GROUP_ID)!!
        presenter.groupId = groupId
        publishBtn.show()
        publishBtn.setOnClickListener {
            val post = richEditor.createFinalText(namesMap, finalNamesMedia)
            if (post.isEmpty()){
                dialogDelegate.showErrorSnackBar(getString(R.string.post_should_contains_text))
            }
            else if (loadingViews.isNotEmpty()) {
                var isLoading = false
                loadingViews.forEach { (_, view) ->
                    val darkCard = view?.findViewById<TextView>(R.id.darkCard)
                    if (darkCard?.isVisible() != false) {
                        isLoading = true
                    }
                }
                if (isLoading)
                    dialogDelegate.showErrorSnackBar(getString(R.string.image_still_uploading))
                else {
                    createPost(post)
                }
            }
            else {
                createPost(post)
            }
        }

        try {
            childFragmentManager.beginTransaction().replace(R.id.containerBottomSheet,
                bottomFragment).commit()
            bottomFragment.callback = this
            bottomSheetBehaviour = BottomSheetBehavior.from(createPostBinding.containerBottomSheet)
                    as AutoCloseBottomSheetBehavior<FrameLayout>
            bottomSheetBehaviour.run {
                peekHeight = requireContext().dpToPx(35)
                halfExpandedRatio = 0.6f
                isFitToContents = false
                addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                            chooseMedias.clear()
                            chooseMedias.addAll(loadingViews.keys.map {
                                ChooseMedia(it)
                            })
                        }
                        bottomFragment.changeState(newState)
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {

                    }
                })
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        createPostBinding.navigationToolbar.
            toolbarBackAction.setOnClickListener { onResultCancel() }
        setErrorHandler()
    }

    private fun setFragmentResult(bundle: Bundle){
        childFragmentManager.setFragmentResult(MEDIA_INTERACTION_REQUEST_CODE,
           bundle)
    }

    protected open fun createPost(post:String) {
        presenter.createPostWithImage(
                post,
                groupId,
                bottomFragment.getPhotosUrl(), bottomFragment.getVideosUrl(),
                bottomFragment.getAudiosUrl())
    }

    override fun postCreateSuccessfully(postEntity: GroupPostEntity.PostEntity) {
        onResultOk(postEntity.id)
    }

    override fun showLoading(show: Boolean) {
        publishBtn.isEnabled = show
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
            richEditor.insertImage(chooseMedia.url,"alt")
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

    protected open fun createAudioPlayerView(audioEntity: AudioEntity): DownloadAudioPlayerView {
        return DownloadAudioPlayerView(requireContext()).apply {
            trackName = audioEntity.song
            trackOwner = "Загрузил (ID:${audioEntity.owner})"
            durationTrack.text = if (audioEntity.duration != "") audioEntity.duration else "00:00"
        }
    }

    protected open fun createImageView(fileEntity: FileEntity): View{
        val image = LayoutInflater.from(context).inflate(R.layout.layout_create_pic, null)
        val pic = image.findViewById<SimpleDraweeView>(R.id.imagePreview)
        imageLoadingDelegate.loadImageFromFile(fileEntity.file,pic)
        return image
    }

    protected open fun createVideoPlayerView(fileEntity: FileEntity): DownloadVideoPlayerView {
        return DownloadVideoPlayerView(requireContext()).apply {
            imageLoadingDelegate.loadImageFromFile(fileEntity.preview,previewForVideo)
            durationVideo.text = if (fileEntity.duration != "") fileEntity.duration else "00:00"
            nameVideo.text = fileEntity.title
        }
    }

    protected open fun firstCreateView() {
        //createPostCustomView.createAllMainView()
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
    }

    override fun showImageUploadingProgress(progress: Float, path: String) {
        loadingViews[path]?.apply {
            val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            imageUploadingProgressBar?.progress = progress
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

    private fun setErrorHandler() {
        errorHandler.on(CompositeException::class.java) { throwable, _ ->
            run {
                (throwable as? CompositeException)?.exceptions?.forEach { ex ->
                    (ex as? FieldException)?.let {
                        if (it.field == TEXT) {
                            showErrorMessage(it.message.orEmpty())
                        }
                    }
                }
            }
        }
    }

    protected fun detachMedia(path: String) {
        /*loadingViews[path].let {view->
            when(view){
                is DownloadVideoPlayerView->{
                    view.exoPlayer.player?.pause()
                    createPostCustomView.listVideoContainers.forEach { container->
                        container.removeVideoView(loadingViews[path])
                    }
                }
                is DownloadAudioPlayerView->{
                    view.exoPlayer.player?.pause()
                    createPostCustomView.listAudioContainers.forEach {container->
                        container.removeAudioView(loadingViews[path])
                    }
                }
                else ->{
                    createPostCustomView.listImageContainers.forEach {container->
                        container.removeImageView(loadingViews[path])
                    }
                }
            }
        }
        createPostCustomView.deleteName(loadingViews[path])
        loadingViews.remove(path)
        chooseMedias.removeChooseMedia(path)*/
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

    private fun prepareListeners(uploadingView: View?, chooseMedia: ChooseMedia) {
        uploadingView?.apply {
            val stopUploading = findViewById<ImageView>(R.id.stopUploading)
            val imageUploadingProgressBar = findViewById<CircularProgressBar>(R.id.imageUploadingProgressBar)
            val detachImage = findViewById<ImageView>(R.id.detachImage)
            val refreshContainer = findViewById<LinearLayout>(R.id.refreshContainer)
            refreshContainer.setOnClickListener {
                imageUploadingProgressBar?.progress = 0f
                setFragmentResult( bundleOf(METHOD_KEY to RETRY_LOADING_METHOD_CODE,
                    CHOOSE_MEDIA_KEY to chooseMedia))
               /* createPostCustomView.listImageContainers.forEach {container->
                    container.removeView(uploadingView)
                }
                createPostCustomView.listAudioContainers.forEach {container->
                    container.removeView(uploadingView)
                }*/
                imageUploadingStarted(uploadingView)
            }
            stopUploading?.setOnClickListener {
                setFragmentResult(bundleOf(METHOD_KEY to  CANCEL_LOADING_METHOD_CODE,
                    CHOOSE_MEDIA_KEY to chooseMedia))
                detachMedia(chooseMedia.url)
            }
            detachImage?.setOnClickListener {
                setFragmentResult(bundleOf(METHOD_KEY to REMOVE_CONTENT_METHOD_CODE,
                    CHOOSE_MEDIA_KEY to chooseMedia))
                detachMedia(chooseMedia.url)
            }
        }
    }

    private fun onResultOk(postId: String) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(GroupFragment.POST_ID, postId)
        findNavController().popBackStack()
    }

    private fun onResultCancel() {
        findNavController().popBackStack()
    }

    override fun getState() = bottomSheetBehaviour.state

    override fun changeStateBottomSheet(newState: Int) {
        bottomSheetBehaviour.state = newState
    }

    override fun getLoadingView() = loadingViews
    override fun closeKeyboard() {
        try {
            richEditor.hideKeyboard()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun setUpBoldText() {
        richEditor.setBold()
    }

    override fun setUpItalicText() {
        richEditor.setItalic()
    }

    override fun setupStrikeText() {
        richEditor.setStrikeThrough()
    }

    override fun setupUnderlineText() {
        richEditor.setUnderline()
    }

    override fun setAlignLeft() {
        richEditor.setAlignLeft()
    }

    override fun setAlignCenter() {
        richEditor.setAlignCenter()
    }

    override fun setAlignRight() {
        richEditor.setAlignRight()
    }

    override fun onPause() {
        super.onPause()
        chooseMedias.clear()
    }
}
