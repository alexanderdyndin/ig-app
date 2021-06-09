package com.intergroupapplication.presentation.feature.editpost.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.os.bundleOf
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.customview.PostCustomView
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostFragment
import com.intergroupapplication.presentation.feature.editpost.presenter.EditPostPresenter
import com.intergroupapplication.presentation.feature.editpostbottomsheet.view.EditPostBottomSheetFragment
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
        parsingTextInPost()
    }

    override fun onResume() {
        super.onResume()
        fillListsWithUrls(groupPost)
    }

    override fun createPost(post:String) {
        Timber.tag("tut_text").d(post)
        groupPost?.id?.let {
            editPostPresenter.editPost(
               post, it,
                bottomFragment.getPhotosUrl(), bottomFragment.getVideosUrl(),
                bottomFragment.getAudiosUrl())
        }
    }

    private fun parsingTextInPost(){
        val textAfterParse = mutableListOf<Pair<String,String>>()
        val splitList = groupPost?.postText?.split(PostCustomView.PARSE_SYMBOL)?: emptyList()
        splitList.forEachIndexed { index, s:String ->
            if (index %2 == 1){
                    textAfterParse.add(Pair(splitList[index-1],s))
            }
            if (splitList.size-1 < index +1){
                textAfterParse.add(Pair(s,""))
            }
        }
        textAfterParse.forEach { text:Pair<String,String>->
            if (text.first.isNotEmpty() || text.second.isNotEmpty()) {
                val container: LinearLayout = LayoutInflater.from(context)
                        .inflate(R.layout.layout_create_post_view, createPostCustomView, false)
                        as LinearLayout
                val imageContainer = container.findViewById<GridLayout>(R.id.imageAndVideoContainer)
                val audioContainer = container.findViewById<LinearLayout>(R.id.audioContainer)
                setupMediaViews(text.second, imageContainer,audioContainer, groupPost)
                val textView = container.findViewById<AppCompatEditText>(R.id.postText)
                textView.setText(text.first)
                createPostCustomView.addViewInEditPost(textView,imageContainer, audioContainer)
                createPostCustomView.addView(container)
            }
        }
    }

    override fun firstCreateView() {

    }

    private fun setupMediaViews(text: String,imageContainer: GridLayout,audioContainer: LinearLayout
                                ,postEntity: GroupPostEntity.PostEntity?) {
        if (text.length>3) {
            val newText = text.substring(1, text.length - 2).split(",")
            newText.forEach { nameMedia ->
                fillingView(postEntity, nameMedia, audioContainer, imageContainer)
            }
        }
    }

    private fun fillingView(postEntity: GroupPostEntity.PostEntity?,name:String,audioContainer:LinearLayout,
                            imageContainer:GridLayout){
        postEntity?.audios?.forEach {audioEntity->
            if (audioEntity.song == name) {
                val url = "/groups/0/comments/${audioEntity.file.substringAfterLast("/")}"
                loadingViews[url] = layoutInflater.inflate(R.layout.layout_audio_in_create_post,
                        audioContainer, false)
                loadingViews[url]?.let {
                    val trackName = it.findViewById<TextView>(R.id.trackName)
                    val detachImage = it.findViewById<ImageView>(R.id.detachImage)
                    trackName?.text = audioEntity.song
                    detachImage.run {
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
                createPostCustomView.namesAudio.add(Pair(audioEntity.song, loadingViews[url]))
                return@fillingView
            }
        }
        postEntity?.images?.forEach{fileEntity ->
            if (fileEntity.title == name) {
                val url = "/groups/0/comments/${fileEntity.file.substringAfterLast("/")}"
                loadingViews[url] = layoutInflater.inflate(R.layout.layout_attach_image,
                        imageContainer, false)
                loadingViews[url]?.let {
                    val imagePreview = it.findViewById<SimpleDraweeView>(R.id.imagePreview)
                    imageLoadingDelegate.loadImageFromUrl(fileEntity.file, imagePreview)
                    val detachImage = it.findViewById<ImageView>(R.id.detachImage)
                    detachImage.run {
                        show()
                        setOnClickListener {
                            childFragmentManager.setFragmentResult(MEDIA_INTERACTION_REQUEST_CODE,
                                    bundleOf(METHOD_KEY to REMOVE_CONTENT_METHOD_CODE,
                                            CHOOSE_MEDIA_KEY to ChooseMedia(url)))
                            detachImage(url)
                        }
                    }
                }
                imageContainer.addView(loadingViews[url])
                createPostCustomView.namesVideoOrImage.add(Pair(name,loadingViews[url]))
                return@fillingView
            }
        }
        postEntity?.videos?.forEach { fileEntity ->
            if (fileEntity.title == name) {
                val url = "/groups/0/comments/${fileEntity.file.substringAfterLast("/")}"
                loadingViews[url] = layoutInflater.inflate(R.layout.layout_attach_image,
                      imageContainer, false)
                loadingViews[url]?.let {
                    val imagePreview = it.findViewById<SimpleDraweeView>(R.id.imagePreview)
                    imageLoadingDelegate.loadImageFromUrl(fileEntity.preview, imagePreview)
                    val detachImage = it.findViewById<ImageView>(R.id.detachImage)
                    detachImage.run {
                        show()
                        setOnClickListener {
                            childFragmentManager.setFragmentResult(MEDIA_INTERACTION_REQUEST_CODE,
                                    bundleOf(METHOD_KEY to REMOVE_CONTENT_METHOD_CODE,
                                            CHOOSE_MEDIA_KEY to ChooseMedia(url)))
                            detachImage(url)
                        }
                    }
                }
                imageContainer.addView(loadingViews[url])
                createPostCustomView.namesVideoOrImage.add(Pair(name, loadingViews[url]))
                return@fillingView
            }
        }
    }

    private fun fillListsWithUrls(postEntity: GroupPostEntity.PostEntity?){
        postEntity?.audios?.let { bottomFragment.addAudioInAudiosUrl(it) }
        postEntity?.videos?.let { bottomFragment.addVideoInVideosUrl(it) }
        postEntity?.images?.let { bottomFragment.addImagesInPhotosUrl(it) }
    }
}