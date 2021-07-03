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
import com.intergroupapplication.presentation.feature.commentsbottomsheet.view.CommentBottomSheetFragment
import timber.log.Timber

class PostCustomView @JvmOverloads constructor(context:Context,
                                               private val attrs: AttributeSet? = null, private val defStyleAttr: Int = 0)
    : LinearLayout(context,attrs,defStyleAttr) {

    companion object{
        const val PARSE_SYMBOL = "~~"
        const val MEDIA_PREFIX = "(MEDIA)"
        private const val START_AUDIO = "<audio src=\""
        private const val START_VIDEO = "<video src=\""
        private const val START_IMAGE = "<img src=\""
        private const val END_AUDIO = "\" controls=\"\"></audio>"
        private const val END_VIDEO = "controls=\"\"></video>"
        private const val END_IMAGE = "\" alt=\"alt\">"
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

    private fun createFinalText(text:String): String {
        var finalTextComment = ""
        val listWithTextAndMediaName = listWithTextAfterSplittingHtml(text)
        listWithTextAndMediaName.filter{it.isNotEmpty() && it != "</div>" && it != "<div>"}
            .forEachIndexed { index,string->
            if (string.contains(MEDIA_PREFIX)){
                finalTextComment +=string.substringBefore(MEDIA_PREFIX).plus(",")
                if (index == listWithTextAndMediaName.size -1) finalTextComment += "}~~"
            }
            else{
                if (index !=0) finalTextComment+="}~~"
                finalTextComment += string
                if (index != listWithTextAndMediaName.size -1) finalTextComment += "~~{"
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
        return secondList
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
        secondList.filter { it.isNotEmpty() && it != "</div>" && it != "<div>"}.forEach {
            firstList.addAll(it.split(prefix))
        }
        secondList.clear()
    }

    private fun parsingTextInPost(){
        //val textAfterParse = markupModel.text.split(PARSE_SYMBOL)
        val textAfterParse = createFinalText(markupModel.text).split(PARSE_SYMBOL)
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
            createImageGalleryView(imageUrl,view.findViewById(R.id.imageBody))
        }
        if (audioUrl.isNotEmpty()) {
            createAudioGalleryView(audioUrl,view.findViewById(R.id.audioBody))
        }
        if (videoUrl.isNotEmpty()) {
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