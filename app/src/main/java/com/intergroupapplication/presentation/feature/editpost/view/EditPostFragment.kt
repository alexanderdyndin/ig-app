package com.intergroupapplication.presentation.feature.editpost.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.os.bundleOf
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.customview.CreateAudioGalleryView
import com.intergroupapplication.presentation.customview.CreateImageGalleryView
import com.intergroupapplication.presentation.customview.CreateVideoGalleryView
import com.intergroupapplication.presentation.customview.PostCustomView
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostFragment
import com.intergroupapplication.presentation.feature.editpost.presenter.EditPostPresenter
import com.intergroupapplication.presentation.feature.postbottomsheet.view.PostBottomSheetFragment
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadAudioPlayerView
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadVideoPlayerView
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

class EditPostFragment:CreatePostFragment(),PostBottomSheetFragment.Callback {
    companion object{
        const val GROUP_POST_ENTITY_KEY = "group_post_entity_key"
    }

    @Inject
    @InjectPresenter
    lateinit var editPostPresenter:EditPostPresenter

    @ProvidePresenter
    fun provideEditPostPresenter():EditPostPresenter = editPostPresenter

    private var groupPost:GroupPostEntity.PostEntity? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        groupPost = arguments?.getParcelable(GROUP_POST_ENTITY_KEY)
        super.onViewCreated(view, savedInstanceState)
        publishBtn.text = view.context.getString(R.string.edit_post)
        parsingTextInPost()
    }

    override fun onResume() {
        super.onResume()
        fillListsWithUrls(groupPost)
    }

    override fun createPost(post:String) {
        groupPost?.id?.let {
            editPostPresenter.editPost(
               post, it,
                bottomFragment.getPhotosUrl(), bottomFragment.getVideosUrl(),
                bottomFragment.getAudiosUrl())
        }
    }

    private fun parsingTextInPost(){
        /*val textAfterParse = mutableListOf<Pair<String,String>>()
        val splitList = groupPost?.postText?.split(PostCustomView.PARSE_SYMBOL)?: emptyList()
        splitList.forEachIndexed { index, s:String ->
            if (index %2 == 1){
                    textAfterParse.add(Pair(splitList[index-1],s))
            }
            if (splitList.size-1 < index +1){
                textAfterParse.add(Pair(s,""))
            }
        }
        textAfterParse.filter { pair-> pair.second.isNotEmpty() || pair.first.trim().isNotEmpty() }
            .forEachIndexed { index, text:Pair<String,String>->
                val container: LinearLayout = LayoutInflater.from(context)
                    .inflate(R.layout.layout_create_post_view, createPostCustomView, false)
                    as LinearLayout
                val imageContainer = container.findViewById<CreateImageGalleryView>(R.id.createImageContainer)
                val audioContainer = container.findViewById<CreateAudioGalleryView>(R.id.createAudioContainer)
                val videoContainer = container.findViewById<CreateVideoGalleryView>(R.id.createVideoContainer)
                setupMediaViews(text.second, imageContainer,audioContainer,videoContainer, groupPost)
                val textView = container.findViewById<AppCompatEditText>(R.id.postText)
                textView.setText(text.first)
              //  createPostCustomView.addViewInEditPost(textView,imageContainer, audioContainer, videoContainer)
                //createPostCustomView.addView(container)
            }
        //createPostCustomView.createAllMainView()*/
    }

    override fun firstCreateView() {

    }

    private fun setupMediaViews(text: String,imageContainer: CreateImageGalleryView,
                audioContainer: CreateAudioGalleryView, videoContainer:CreateVideoGalleryView
                                ,postEntity: GroupPostEntity.PostEntity?) {
        if (text.length>3) {
            val newText = text.substring(1, text.length - 2).split(",")
            newText.forEach { nameMedia ->
                fillingView(postEntity, nameMedia, imageContainer, audioContainer, videoContainer)
            }
        }
    }

    private fun fillingView(postEntity: GroupPostEntity.PostEntity?,name:String,
                imageContainer:CreateImageGalleryView,audioContainer:CreateAudioGalleryView,
                            videoContainer: CreateVideoGalleryView){
        postEntity?.audios?.forEach {audioEntity->
            if (audioEntity.song == name) {
                val url = "/groups/0/comments/${audioEntity.file.substringAfterLast("/")}"
                loadingViews[url] = createAudioPlayerView(audioEntity)
                audioContainer.addAudio(audioEntity, loadingViews[url] as DownloadAudioPlayerView)
                //createPostCustomView.namesAudio.add(Pair(name,loadingViews[url]))
                return@fillingView
            }
        }
        postEntity?.images?.forEach{fileEntity ->
            if (fileEntity.title == name) {
                val url = "/groups/0/comments/${fileEntity.file.substringAfterLast("/")}"
                loadingViews[url] = createImageView(fileEntity)
                loadingViews[url]?.let { imageContainer.addImage(it) }
                //createPostCustomView.namesImage.add(Pair(name, loadingViews[url]))
                return@fillingView
            }
        }
        postEntity?.videos?.forEach { fileEntity ->
            if (fileEntity.title == name) {
                val url = "/groups/0/comments/${fileEntity.file.substringAfterLast("/")}"
                loadingViews[url] = createVideoPlayerView(fileEntity)
                videoContainer.addVideo(fileEntity, loadingViews[url] as DownloadVideoPlayerView)
               // createPostCustomView.namesVideo.add(Pair(name, loadingViews[url]))
                return@fillingView
            }
        }
    }

    override fun createAudioPlayerView(audioEntity: AudioEntity): DownloadAudioPlayerView {
        val url = "/groups/0/comments/${audioEntity.file.substringAfterLast("/")}"
        return DownloadAudioPlayerView(requireContext()).apply {
            trackName = audioEntity.song
            trackOwner = "Загрузил (ID:${audioEntity.owner})"
            durationTrack.text = if (audioEntity.duration != "") audioEntity.duration else "00:00"
            val detachImage = findViewById<ImageView>(R.id.detachImage)
            detachImage.run {
                show()
                setOnClickListener {
                    childFragmentManager.setFragmentResult(MEDIA_INTERACTION_REQUEST_CODE,
                        bundleOf(METHOD_KEY to REMOVE_CONTENT_METHOD_CODE,
                            CHOOSE_MEDIA_KEY to ChooseMedia(url)))
                    detachMedia(url)
                }
            }
        }
    }

    override fun createImageView(fileEntity: FileEntity): View {
        val url = "/groups/0/comments/${fileEntity.file.substringAfterLast("/")}"
        val image = LayoutInflater.from(context).inflate(R.layout.layout_create_pic, null)
        val pic = image.findViewById<SimpleDraweeView>(R.id.imagePreview)
        if (fileEntity.file.contains("/groups/0/comments/")){
            imageLoadingDelegate.loadImageFromUrl(fileEntity.file, pic)
        }
        else{
            imageLoadingDelegate.loadImageFromFile(fileEntity.file,pic)
        }
        image.run {
            val detachImage = findViewById<ImageView>(R.id.detachImage)
            detachImage.run {
                show()
                setOnClickListener {
                    childFragmentManager.setFragmentResult(MEDIA_INTERACTION_REQUEST_CODE,
                        bundleOf(METHOD_KEY to REMOVE_CONTENT_METHOD_CODE,
                            CHOOSE_MEDIA_KEY to ChooseMedia(url)))
                    detachMedia(url)
                }
            }
        }
        return image
    }

    override fun createVideoPlayerView(fileEntity: FileEntity): DownloadVideoPlayerView {
        val url = "/groups/0/comments/${fileEntity.file.substringAfterLast("/")}"
        return DownloadVideoPlayerView(requireContext()).apply {
            if (fileEntity.preview.contains("previewImage")){
                imageLoadingDelegate.loadImageFromFile(fileEntity.preview, previewForVideo)
            }else {
                imageLoadingDelegate.loadImageFromUrl(fileEntity.preview, previewForVideo)
            }
            durationVideo.text = if (fileEntity.duration != "") fileEntity.duration else "00:00"
            nameVideo.text = fileEntity.title
            val detachImage = findViewById<ImageView>(R.id.detachImage)
            detachImage.run {
                show()
                setOnClickListener {
                    childFragmentManager.setFragmentResult(MEDIA_INTERACTION_REQUEST_CODE,
                        bundleOf(METHOD_KEY to REMOVE_CONTENT_METHOD_CODE,
                            CHOOSE_MEDIA_KEY to ChooseMedia(url)))
                    detachMedia(url)
                }
            }
        }
    }

    private fun fillListsWithUrls(postEntity: GroupPostEntity.PostEntity?){
        postEntity?.audios?.let { bottomFragment.addAudioInAudiosUrl(it) }
        postEntity?.videos?.let { bottomFragment.addVideoInVideosUrl(it) }
        postEntity?.images?.let { bottomFragment.addImagesInPhotosUrl(it) }
    }
}