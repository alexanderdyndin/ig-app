package com.intergroupapplication.presentation.feature.editpost.view

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostFragment
import com.intergroupapplication.presentation.feature.editpost.presenter.EditPostPresenter
import com.intergroupapplication.presentation.feature.editpostbottomsheet.view.EditPostBottomSheetFragment
import kotlinx.android.synthetic.main.creategroup_toolbar_layout.*
import kotlinx.android.synthetic.main.fragment_create_post.*
import kotlinx.android.synthetic.main.layout_attach_image.view.*
import kotlinx.android.synthetic.main.layout_attach_image.view.detachImage
import kotlinx.android.synthetic.main.layout_audio_in_create_post.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import javax.inject.Inject

class EditPostFragment:CreatePostFragment(),EditPostBottomSheetFragment.Callback {
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
        postText.setText(groupPost?.postText)
        fillingView(groupPost)
    }

    override fun onResume() {
        super.onResume()
        fillListsWithUrls(groupPost)
    }

    override fun createPost() {
        groupPost?.id?.let {
            editPostPresenter.editPost(postText.text.toString().trim(), it,
                bottomFragment.getPhotosUrl(), bottomFragment.getVideosUrl(),
                bottomFragment.getAudiosUrl())
        }
    }

    private fun fillingView(postEntity: GroupPostEntity.PostEntity?){
        postEntity?.audios?.forEach {audioEntity->
            val url = "/groups/0/comments/${audioEntity.file.substringAfterLast("/")}"
            loadingViews[url] = layoutInflater.inflate(R.layout.layout_audio_in_create_post,audioContainer, false)
            loadingViews[url]?.let {
                it.trackName?.text = audioEntity.song
                it.detachImage.run {
                    show()
                    setOnClickListener {
                        childFragmentManager.setFragmentResult(MEDIA_INTERACTION_REQUEST_CODE,
                                bundleOf(METHOD_KEY to REMOVE_CONTENT_METHOD_CODE,
                                        CHOOSE_MEDIA_KEY to ChooseMedia(url)))
                        detachImage(url)
                    }
                }
            }
            audioContainer.addView(loadingViews[url])
        }
        postEntity?.images?.forEach{fileEntity ->
            val url = "/groups/0/comments/${fileEntity.file.substringAfterLast("/")}"
            loadingViews[url] = layoutInflater.inflate(R.layout.layout_attach_image, postContainer, false)
            loadingViews[url]?.let {
                it.imagePreview?.let { draweeView ->
                    imageLoadingDelegate.loadImageFromUrl(fileEntity.file, draweeView)
                }
                it.detachImage.run {
                    show()
                    setOnClickListener {
                        childFragmentManager.setFragmentResult(MEDIA_INTERACTION_REQUEST_CODE,
                                bundleOf(METHOD_KEY to REMOVE_CONTENT_METHOD_CODE,
                                        CHOOSE_MEDIA_KEY to ChooseMedia(url)))
                        detachImage(url)
                    }
                }
            }
            postContainer.addView(loadingViews[url])
        }
        postEntity?.videos?.forEach{fileEntity ->
            val url = "/groups/0/comments/${fileEntity.file.substringAfterLast("/")}"
            loadingViews[url] = layoutInflater.inflate(R.layout.layout_attach_image, postContainer, false)
            loadingViews[url]?.let {
                it.imagePreview?.let { draweeView ->
                    imageLoadingDelegate.loadImageFromUrl(fileEntity.preview, draweeView)
                }
                it.detachImage.run {
                    show()
                    setOnClickListener {
                        childFragmentManager.setFragmentResult(MEDIA_INTERACTION_REQUEST_CODE,
                                bundleOf(METHOD_KEY to REMOVE_CONTENT_METHOD_CODE,
                                        CHOOSE_MEDIA_KEY to ChooseMedia(url)))
                        detachImage(url)
                    }
                }
            }
            postContainer.addView(loadingViews[url])
        }
    }

    private fun fillListsWithUrls(postEntity: GroupPostEntity.PostEntity?){
        postEntity?.audios?.let { bottomFragment.addAudioInAudiosUrl(it) }
        postEntity?.videos?.let { bottomFragment.addVideoInVideosUrl(it) }
        postEntity?.images?.let { bottomFragment.addImagesInPhotosUrl(it) }
    }
}