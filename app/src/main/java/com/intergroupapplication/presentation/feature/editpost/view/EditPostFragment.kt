package com.intergroupapplication.presentation.feature.editpost.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.os.bundleOf
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.customview.PostCustomView
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostFragment
import com.intergroupapplication.presentation.feature.editpost.presenter.EditPostPresenter
import com.intergroupapplication.presentation.feature.postbottomsheet.view.PostBottomSheetFragment
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadAudioPlayerView
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadVideoPlayerView
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
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
        groupPost?.let { richEditor.html = changeNameOnUrl(it)}
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

    private fun changeNameOnUrl(groupPost: GroupPostEntity.PostEntity):String{
        var text = groupPost.postText
        groupPost.audios.forEach {
            if (text.contains(it.song)){
                namesMap[it.file] = it.song
                text = text.substringBefore(it.song)+it.file+
                        text.substringAfter(it.song+PostCustomView.MEDIA_PREFIX)
            }
        }
        groupPost.images.forEach {
            if (text.contains(it.title)){
                namesMap[it.file] = it.title
                text = text.substringBefore(it.title)+it.file+
                        text.substringAfter(it.title+PostCustomView.MEDIA_PREFIX)
            }
        }
        groupPost.videos.forEach {
            if (text.contains(it.title)){
                namesMap[it.file] = it.title
                text = text.substringBefore(it.title)+it.file+
                        text.substringAfter(it.title+PostCustomView.MEDIA_PREFIX)
            }
        }
        return text
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