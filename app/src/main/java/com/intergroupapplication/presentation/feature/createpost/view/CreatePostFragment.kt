package com.intergroupapplication.presentation.feature.createpost.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.intergroupapplication.domain.KeyboardVisibilityEvent
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.TEXT
import com.intergroupapplication.domain.gateway.ColorDrawableGateway
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
        const val ACTIVATED_KEY = "activated_key"
        const val RETRY_LOADING_METHOD_CODE = 0
        const val CANCEL_LOADING_METHOD_CODE = 1
        const val REMOVE_CONTENT_METHOD_CODE = 2
        const val IC_EDIT_TEXT_METHOD_CODE = 3
        const val IC_EDIT_ALIGN_METHOD_CODE = 4
        const val IC_ATTACH_FILE_METHOD_CODE = 5
        const val IC_EDIT_COLOR_METHOD_CODE = 6
        const val CHANGE_COLOR = 11
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: CreatePostPresenter

    @ProvidePresenter
    fun providePresenter(): CreatePostPresenter = presenter

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var colorDrawableGateway: ColorDrawableGateway

    private lateinit var bottomSheetBehaviour: AutoCloseBottomSheetBehavior<FrameLayout>

    protected val bottomFragment by lazy { PostBottomSheetFragment() }

    private val createPostBinding by viewBinding(FragmentCreatePostBinding::bind)

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_create_post

    protected val loadingViews: MutableMap<String, View?> = mutableMapOf()
    protected val namesMap = mutableMapOf<String,String>()
    private val finalNamesMedia = mutableListOf<String>()

    override fun getSnackBarCoordinator(): CoordinatorLayout = createPostBinding.createPostCoordinator

    protected lateinit var richEditor:RichEditor
    protected lateinit var publishBtn:TextView

    private lateinit var groupId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity?.let{
            KeyboardVisibilityEvent.setEventListener(it,viewLifecycleOwner){ isVisible ->
                Timber.tag("tut_keyboard").d(isVisible.toString())
                if (isVisible){
                    changeBottomConstraintMediaHolder(createPostBinding.
                        horizontalGuideEndWithKeyboard.id)
                    createPostBinding.mediaHolder.show()
                }
                else{
                    changeBottomConstraintMediaHolder(createPostBinding.horizontalGuideEnd.id)
                    createPostBinding.mediaHolder.hide()
                }
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

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
                    createPostBinding.selectBoldText.changeActivated(false,
                        createPostBinding.selectItalicText,createPostBinding.selectStrikeText,
                        createPostBinding.selectStrikeText)
                    types.forEach { type ->
                        when {
                            type.name.contains("FONT_COLOR") -> {
                                setFragmentResult(bundleOf(METHOD_KEY to CHANGE_COLOR,
                                    COLOR_KEY to type.color))
                                createPostBinding.icEditColor.setImageDrawable(colorDrawableGateway.
                                    getDrawableByColor(type.color))
                            }
                            else -> {
                                when(type){
                                    TextType.BOLD -> {
                                        createPostBinding.selectBoldText.activated(true)
                                    }
                                    TextType.ITALIC ->{
                                        createPostBinding.selectItalicText.activated(true)
                                    }
                                    TextType.UNDERLINE ->{
                                        createPostBinding.selectUnderlineText.activated(true)
                                    }
                                    TextType.STRIKETHROUGH ->{
                                       createPostBinding.selectStrikeText.activated(true)
                                    }
                                    TextType.JUSTIFYLEFT->{
                                        createPostBinding.leftGravityButton.isChecked = true
                                    }
                                    TextType.JUSTIFYCENTER->{
                                        createPostBinding.centerGravityButton.isChecked = true
                                    }
                                    TextType.JUSTIFYRIGHT->{
                                        createPostBinding.rightGravityButton.isChecked = true
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
                peekHeight = requireContext().dpToPx(37)
                createPostBinding.mediaHolder.minimumHeight = peekHeight
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
        setupIconPanel()
        setupPanelStyleText()
        setupPanelGravityText()
    }

    private fun setupIconPanel(){
        setupIcEditText()
        setupIcEditAlign()
        setupIcAttachFile()
        setupIcEditColor()
    }

    private fun setupIcEditText() {
        createPostBinding.icEditText.setOnClickListener {
            if (it.isActivated) {
                it.activated(false)
                gonePanelStyleText()
                setFragmentResult(
                    bundleOf(
                        METHOD_KEY to IC_EDIT_TEXT_METHOD_CODE,
                        ACTIVATED_KEY to false
                    )
                )
            } else {
                it.activated(true)
                showPanelStyleText()
                setFragmentResult(
                    bundleOf(
                        METHOD_KEY to IC_EDIT_TEXT_METHOD_CODE,
                        ACTIVATED_KEY to true
                    )
                )
                setFragmentResult(
                    bundleOf(
                        METHOD_KEY to IC_EDIT_ALIGN_METHOD_CODE,
                        ACTIVATED_KEY to false
                    )
                )
            }
        }
    }

    private fun setupIcEditAlign() {
        createPostBinding.icEditAlign.setOnClickListener {
            if (it.isActivated) {
                it.activated(false)
                gonePanelGravity()
                setFragmentResult(
                    bundleOf(
                        METHOD_KEY to IC_EDIT_ALIGN_METHOD_CODE,
                        ACTIVATED_KEY to false
                    )
                )
            } else {
                it.activated(true)
                showPanelGravity()
                setFragmentResult(
                    bundleOf(
                        METHOD_KEY to IC_EDIT_ALIGN_METHOD_CODE,
                        ACTIVATED_KEY to true
                    )
                )
                setFragmentResult(
                    bundleOf(
                        METHOD_KEY to IC_EDIT_TEXT_METHOD_CODE,
                        ACTIVATED_KEY to false
                    )
                )
            }
        }
    }

    private fun setupIcEditColor(){
        createPostBinding.icEditColor.setOnClickListener {
            setFragmentResult(bundleOf(METHOD_KEY to IC_EDIT_COLOR_METHOD_CODE))
        }
        createPostBinding.mediaHolder.hide()
    }

    private fun setupIcAttachFile(){
        createPostBinding.icAttachFile.setOnClickListener {
            setFragmentResult(bundleOf(METHOD_KEY to IC_ATTACH_FILE_METHOD_CODE))
        }
        createPostBinding.mediaHolder.hide()
    }
    private fun changeBottomConstraintMediaHolder(id: Int) {
        val paramsRichEditor = createPostBinding.mediaHolder.layoutParams
                as ConstraintLayout.LayoutParams
        paramsRichEditor.bottomToTop = id
        createPostBinding.mediaHolder.layoutParams = paramsRichEditor
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

    override fun showKeyboard() {
        try {
            richEditor.showKeyBoard()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun changeTextColor(color: Int) {
        richEditor.setTextColor(color)
    }

    override fun showPanelStyleText() {
        createPostBinding.panelStyleText.show()
        createPostBinding.panelGravityText.gone()
        createPostBinding.icEditText.changeActivated(true,createPostBinding.icEditAlign,
            createPostBinding.icAttachFile)
    }

    override fun gonePanelStyleText() {
        createPostBinding.panelStyleText.gone()
        createPostBinding.icEditText.activated(false)
    }

    override fun showPanelGravity() {
        createPostBinding.panelGravityText.show()
        createPostBinding.panelStyleText.gone()
        createPostBinding.icEditAlign.changeActivated(true,createPostBinding.icEditText,
            createPostBinding.icAttachFile)
    }

    override fun gonePanelGravity() {
        createPostBinding.panelGravityText.gone()
        createPostBinding.icEditAlign.activated(false)
    }


    private fun setupPanelStyleText() {
        setupBoldTextView()
        setupItalicTextView()
        setupStrikeTextView()
        setupUnderlineTextView()
    }

    private fun setupBoldTextView() {
        createPostBinding.selectBoldText.setOnClickListener {
            it.activated(!it.isActivated)
            richEditor.setBold()
        }
    }

    private fun setupItalicTextView() {
        createPostBinding.selectItalicText.setOnClickListener {
            it.activated(!it.isActivated)
            richEditor.setItalic()
        }
    }

    private fun setupStrikeTextView() {
        createPostBinding.selectStrikeText.setOnClickListener {
            it.activated(!it.isActivated)
            richEditor.setStrikeThrough()
        }
    }

    private fun setupUnderlineTextView() {
        createPostBinding.selectUnderlineText.setOnClickListener {
            it.activated(!it.isActivated)
            richEditor.setUnderline()
        }
    }

    private fun setupPanelGravityText(){
        setupLeftGravityView()
        setupCenterGravityView()
        setupRightGravityView()
    }

    private fun setupLeftGravityView(){
        createPostBinding.leftGravityButton.setOnClickListener {
            richEditor.setAlignLeft()
        }
    }

    private fun setupCenterGravityView(){
        createPostBinding.centerGravityButton.setOnClickListener {
            richEditor.setAlignCenter()
        }
    }

    private fun setupRightGravityView(){
        createPostBinding.rightGravityButton.setOnClickListener {
            richEditor.setAlignRight()
        }
    }

    override fun onPause() {
        super.onPause()
        chooseMedias.clear()
    }
}
