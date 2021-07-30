package com.intergroupapplication.presentation.customview

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.danikula.videocache.HttpProxyCacheServer
import com.intergroupapplication.R
import com.intergroupapplication.data.model.MarkupModel
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.ParseConstants.END_AUDIO
import com.intergroupapplication.domain.entity.ParseConstants.END_CONTAINER
import com.intergroupapplication.domain.entity.ParseConstants.END_IMAGE
import com.intergroupapplication.domain.entity.ParseConstants.END_MEDIA_PREFIX
import com.intergroupapplication.domain.entity.ParseConstants.END_VIDEO
import com.intergroupapplication.domain.entity.ParseConstants.MEDIA_PREFIX
import com.intergroupapplication.domain.entity.ParseConstants.PARSE_SYMBOL
import com.intergroupapplication.domain.entity.ParseConstants.START_AUDIO
import com.intergroupapplication.domain.entity.ParseConstants.START_CONTAINER
import com.intergroupapplication.domain.entity.ParseConstants.START_IMAGE
import com.intergroupapplication.domain.entity.ParseConstants.START_MEDIA_PREFIX
import com.intergroupapplication.domain.entity.ParseConstants.START_VIDEO
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate

class PostCustomView @JvmOverloads constructor(context:Context, attrs: AttributeSet? = null,
       defStyleAttr: Int = 0) : LinearLayout(context,attrs,defStyleAttr) {

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
    }

    private lateinit var markupModel: MarkupModel
    var imageClickListener: (List<FileEntity>, Int) -> Unit = { _, _ -> }
    var proxy: HttpProxyCacheServer? = null
    var imageLoadingDelegate:ImageLoadingDelegate? = null

    fun setUpPost(markupModel: MarkupModel){
        this.removeAllViews()
        this.markupModel = markupModel
        parsingTextInPost()
    }

    private fun createFinalText(text:String): String {
        var finalTextComment = ""
        val listWithTextAndMediaName = listWithTextAfterSplittingHtml(text)
        listWithTextAndMediaName
            .forEachIndexed { index,string->
            if (string.contains(MEDIA_PREFIX)){
                if (index ==0) finalTextComment += START_MEDIA_PREFIX
                finalTextComment +=string.substringBefore(MEDIA_PREFIX).plus(",")
                if (index == listWithTextAndMediaName.size -1) finalTextComment += END_MEDIA_PREFIX
            }
            else{
                if (index !=0) finalTextComment+= END_MEDIA_PREFIX
                finalTextComment += string
                if (index != listWithTextAndMediaName.size -1) {
                    finalTextComment += START_MEDIA_PREFIX
                }
            }
        }
        return finalTextComment
    }

    private fun listWithTextAfterSplittingHtml(text: String): MutableList<String> {
        val firstList = mutableListOf<String>()
        firstList.addAll(
            text.replace("<br>", "")
            .split(START_IMAGE)
        )
        val secondList = mutableListOf<String>()
        splitHtml(firstList, secondList, END_IMAGE)
        addAllText(secondList, firstList, START_AUDIO)
        splitHtml(firstList, secondList, END_AUDIO)
        addAllText(secondList, firstList, START_VIDEO)
        secondList.clear()
        splitHtml(firstList, secondList, END_VIDEO)
        return secondList.filter { it.isNotEmpty() &&
                it != END_CONTAINER && it != START_CONTAINER }.toMutableList()
    }

    private fun splitHtml(
        firstList: MutableList<String>,
        secondList: MutableList<String>,
        prefix: String
    ) {
        firstList.forEach {
            if (it.contains(prefix)) {
                secondList.addAll(it.split(prefix))
            } else {
                secondList.add(it)
            }
        }
        firstList.clear()
    }

    private fun addAllText(
        secondList: MutableList<String>,
        firstList: MutableList<String>,
        prefix: String
    ) {
        secondList.filter { it.isNotEmpty() &&
                it != END_CONTAINER && it != START_CONTAINER}.forEach {
            firstList.addAll(it.split(prefix))
        }
        secondList.clear()
    }

    private fun parsingTextInPost(){
        val textAfterParse = createFinalText(markupModel.text).split(PARSE_SYMBOL).filter {
            it.isNotEmpty()
        }
        textAfterParse.forEach { text->
            val view = LayoutInflater.from(context)
                    .inflate(R.layout.layout_post_custom_view,this,false)
                        as LinearLayout
            val textView = view.findViewById<TextView>(R.id.postText)
            if (text.contains("^\\{".toRegex()) && text.contains("\\}\$".toRegex())){
                view.removeView(textView)
                if (text.length > 3){
                    setupMediaViews(text,view)
                }
            }
            else{
                textView.text = Html.fromHtml(text.trim().replace("text-align: right",
                    "text-align: end"),Html.FROM_HTML_MODE_COMPACT)
                view.run {
                    removeView(view.findViewById(R.id.imageBody))
                    removeView(view.findViewById(R.id.audioBody))
                    removeView(view.findViewById(R.id.videoBody))
                }
            }
            addView(view)
        }
    }

    private fun setupMediaViews(text: String,view: LinearLayout) {
        val newText = text.substring(1, text.length - 2).split(",")
        val imageUrl = mutableListOf<FileEntity>()
        val videoUrl = mutableListOf<FileEntity>()
        val audioUrl = mutableListOf<AudioEntity>()
        newText.forEach { nameMedia ->
            addFileByName(imageUrl, videoUrl, audioUrl, nameMedia)
        }
        if (imageUrl.isNotEmpty()) {
            createImageGalleryView(imageUrl,view.findViewById(R.id.imageBody))
        }
        else{
            view.removeView(view.findViewById(R.id.imageBody))
        }
        if (audioUrl.isNotEmpty()) {
            createAudioGalleryView(audioUrl,view.findViewById(R.id.audioBody))
        }
        else{
            view.removeView(view.findViewById(R.id.audioBody))
        }
        if (videoUrl.isNotEmpty()) {
            createVideoGalleryView(videoUrl,view.findViewById(R.id.videoBody))
        }
        else{
            view.removeView(view.findViewById(R.id.videoBody))
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

    private fun createImageGalleryView(imageUrl: MutableList<FileEntity>,
                                       imageGalleryView: ImageGalleryView) {
        imageGalleryView.setImages(imageUrl, markupModel.imagesExpanded, markupModel.images)
        imageGalleryView.imageClick = imageClickListener
        imageGalleryView.expand = { markupModel.imagesExpanded = it }
    }

    private fun createAudioGalleryView(audioUrl: MutableList<AudioEntity>,
                                       audioGalleryView: AudioGalleryView) {
        audioGalleryView.proxy = proxy
        audioGalleryView.setAudios(audioUrl)
        audioGalleryView.expand = { markupModel.audiosExpanded = it }
    }

    private fun createVideoGalleryView(videoUrl: MutableList<FileEntity>,
                                       videoGalleryView: VideoGalleryView) {
        videoGalleryView.proxy = proxy
        videoGalleryView.imageLoadingDelegate = imageLoadingDelegate
        videoGalleryView.setVideos(videoUrl)
        videoGalleryView.expand = { markupModel.videosExpanded = it }
    }

}