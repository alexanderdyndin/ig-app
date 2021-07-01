package com.intergroupapplication.presentation.customview

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.danikula.videocache.HttpProxyCacheServer
import com.intergroupapplication.R
import com.intergroupapplication.data.model.MarkupModel
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import timber.log.Timber

class PostCustomView @JvmOverloads constructor(context:Context,
                                               private val attrs: AttributeSet? = null, private val defStyleAttr: Int = 0)
    : LinearLayout(context,attrs,defStyleAttr) {

    companion object{
        const val PARSE_SYMBOL = "~~"
    }

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
    }

    private lateinit var markupModel: MarkupModel
    var imageClickListener: (List<FileEntity>, Int) -> Unit = { _, _ -> }
    var proxy: HttpProxyCacheServer? = null
    lateinit var imageLoadingDelegate:ImageLoadingDelegate

    fun setUpPost(markupModel: MarkupModel){
        this.removeAllViews()
        this.markupModel = markupModel
        parsingTextInPost()
    }

    private fun parsingTextInPost(){
        val textAfterParse = markupModel.text.split(PARSE_SYMBOL)
        textAfterParse.forEach { text->
            val view = LayoutInflater.from(context)
                    .inflate(R.layout.layout_post_custom_view,this,false)
            if (text.contains("^\\{".toRegex()) && text.contains("\\}\$".toRegex())){
                if (text.length > 3){
                    setupMediaViews(text,view)
                }
            }
            else{
                val textView = view.findViewById<TextView>(R.id.postText)
                textView.text = Html.fromHtml(text.replace("text-align: right",
                    "text-align: end"),Html.FROM_HTML_MODE_COMPACT)
            }
            addView(view)
        }
    }

    private fun setupMediaViews(text: String,view: View) {
        val newText = text.substring(1, text.length - 2).split(",")
        val imageUrl = mutableListOf<FileEntity>()
        val videoUrl = mutableListOf<FileEntity>()
        val audioUrl = mutableListOf<AudioEntity>()
        newText.forEach { nameMedia ->
            addFileByName(imageUrl, videoUrl, audioUrl, nameMedia)
        }
        if (imageUrl.isNotEmpty()) {
            Timber.tag("tut_image").d("tut")
            createImageGalleryView(imageUrl,view.findViewById(R.id.imageBody))
        }
        if (audioUrl.isNotEmpty()) {
            Timber.tag("tut_audio").d("tut")
            createAudioGalleryView(audioUrl,view.findViewById(R.id.audioBody))
        }
        if (videoUrl.isNotEmpty()) {
            Timber.tag("tut_video").d("tut")
            createVideoGalleryView(videoUrl,view.findViewById(R.id.videoBody))
        }
    }

    private fun addFileByName(l1:MutableList<FileEntity>,l2:MutableList<FileEntity>,
                              l3:MutableList<AudioEntity>, name:String){
        markupModel.images.forEach {
            if (it.title == name){
                l1.add(it)
                return@addFileByName
            }
        }
        markupModel.videos.forEach {
            if (it.title == name){
                l2.add(it)
                return@addFileByName
            }
        }
        markupModel.audios.forEach {
            if (it.song == name){
                l3.add(it)
                return@addFileByName
            }
        }
    }

    private fun createImageGalleryView(imageUrl: MutableList<FileEntity>,imageGalleryView: ImageGalleryView) {
        imageGalleryView.setImages(imageUrl, markupModel.imagesExpanded)
        imageGalleryView.imageClick = imageClickListener
        imageGalleryView.expand = { markupModel.imagesExpanded = it }
    }

    private fun createAudioGalleryView(audioUrl: MutableList<AudioEntity>,audioGalleryView: AudioGalleryView) {
        audioGalleryView.proxy = proxy
        audioGalleryView.setAudios(audioUrl)
        audioGalleryView.expand = { markupModel.audiosExpanded = it }
    }

    private fun createVideoGalleryView(videoUrl: MutableList<FileEntity>,videoGalleryView: VideoGalleryView) {
        videoGalleryView.proxy = proxy
        videoGalleryView.imageLoadingDelegate = imageLoadingDelegate
        videoGalleryView.setVideos(videoUrl)
        videoGalleryView.expand = { markupModel.videosExpanded = it }
    }

}