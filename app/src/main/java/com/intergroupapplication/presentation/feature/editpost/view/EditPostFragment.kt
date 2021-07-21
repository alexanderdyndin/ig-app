package com.intergroupapplication.presentation.feature.editpost.view

import android.os.Bundle
import android.view.View
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.ParseConstants.MEDIA_PREFIX
import com.intergroupapplication.presentation.customview.PostCustomView
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostFragment
import com.intergroupapplication.presentation.feature.editpost.presenter.EditPostPresenter
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

class EditPostFragment : CreatePostFragment() {
    companion object {
        const val GROUP_POST_ENTITY_KEY = "group_post_entity_key"
    }

    @Inject
    @InjectPresenter
    lateinit var editPostPresenter: EditPostPresenter

    @ProvidePresenter
    fun provideEditPostPresenter(): EditPostPresenter = editPostPresenter

    private var groupPost: GroupPostEntity.PostEntity? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        groupPost = arguments?.getParcelable(GROUP_POST_ENTITY_KEY)
        super.onViewCreated(view, savedInstanceState)
        publishBtn.text = view.context.getString(R.string.edit_post)
        groupPost?.let { richEditor.html = changeNameOnUrl(it) }
    }

    override fun onResume() {
        super.onResume()
        fillListsWithUrls(groupPost)
    }

    override fun createPost(post: String, finalNamesMedia: List<String>) {
        groupPost?.id?.let {
            editPostPresenter.editPost(
                post, it,
                bottomFragment.getPhotosUrl(), bottomFragment.getVideosUrl(),
                bottomFragment.getAudiosUrl(), finalNamesMedia
            )
        }
    }

    private fun changeNameOnUrl(groupPost: GroupPostEntity.PostEntity): String {
        var text = groupPost.postText
        groupPost.audios.forEach {
            if (text.contains(it.song)) {
                namesMap[it.file] = it.song
                text = text.substringBefore(it.song) + it.file +
                        text.substringAfter(it.song + MEDIA_PREFIX)
            }
        }
        groupPost.images.forEach {
            if (text.contains(it.title)) {
                namesMap[it.file] = it.title
                text = text.substringBefore(it.title) + it.file +
                        text.substringAfter(it.title + MEDIA_PREFIX)
            }
        }
        groupPost.videos.forEach {
            if (text.contains(it.title)) {
                namesMap[it.file] = it.title
                text = text.substringBefore(it.title) + it.file +
                        text.substringAfter(it.title + MEDIA_PREFIX)
            }
        }
        return text
    }

    private fun fillListsWithUrls(postEntity: GroupPostEntity.PostEntity?) {
        postEntity?.audios?.let { bottomFragment.addAudioInAudiosUrl(it) }
        postEntity?.videos?.let { bottomFragment.addVideoInVideosUrl(it) }
        postEntity?.images?.let { bottomFragment.addImagesInPhotosUrl(it) }
    }
}