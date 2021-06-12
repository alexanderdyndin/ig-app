package com.intergroupapplication.presentation.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import by.kirich1409.viewbindingdelegate.viewBinding
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.R
import com.intergroupapplication.databinding.LayoutCreatePostViewBinding
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.exstension.activated
import com.intergroupapplication.presentation.feature.mediaPlayer.AudioPlayerView
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadAudioPlayerView
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadVideoPlayerView
import com.intergroupapplication.presentation.feature.mediaPlayer.VideoPlayerView
import kotlinx.android.synthetic.main.item_comment.view.*
import timber.log.Timber

class CreatePostCustomView @JvmOverloads constructor(context: Context,
                                                     private val attrs: AttributeSet? = null, private val defStyleAttr: Int = 0)
    : LinearLayout(context,attrs,defStyleAttr) {

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
    }

    //lateinit var imageContainer:GridLayout
    //lateinit var audioContainer:LinearLayout
    lateinit var imageContainer:ImageGalleryView
    lateinit var videoContainer:VideoGalleryView
    lateinit var audioContainer:AudioGalleryView
    lateinit var textPost:AppCompatEditText
   // val namesVideoOrImage = mutableListOf<Pair<String,View?>>()
    val namesImage = mutableListOf<Pair<String,View?>>()
    val namesVideo = mutableListOf<Pair<String,View?>>()
    val namesAudio = mutableListOf<Pair<String,View?>>()
    //val listImageContainers = mutableListOf<GridLayout>()
    //val listAudioContainers = mutableListOf<LinearLayout>()
    val listImageContainers = mutableListOf<ImageGalleryView>()
    val listAudioContainers = mutableListOf<AudioGalleryView>()
    val listVideoContainers = mutableListOf<VideoGalleryView>()
    val listEditText = mutableListOf<AppCompatEditText>()

    fun createAllMainView(){
        changeActivatedAllEditText()
        val view = LayoutInflater.from(context).inflate(R.layout.layout_create_post_view,this,false)
        addView(view)
        textPost = view.findViewById(R.id.postText)
        imageContainer = view.findViewById(R.id.createImageContainer)
        videoContainer = view.findViewById(R.id.createVideoContainer)
        audioContainer = view.findViewById(R.id.createAudioContainer)
        listEditText.add(textPost)
        setupEditText()
        listImageContainers.add(imageContainer)
        listAudioContainers.add(audioContainer)
        listVideoContainers.add(videoContainer)
    }

    fun addViewInEditPost(editText: AppCompatEditText,imageContainer: ImageGalleryView,
                          audioContainer: AudioGalleryView,videoGalleryView: VideoGalleryView){
        addMediaContainer(imageContainer, audioContainer,videoGalleryView)
        addEditText(editText)
    }

    fun removeAllBesidesFirstView(){
        with(this.getChildAt(0) as LinearLayout){
            textPost = (this.getChildAt(0) as AppCompatEditText)
            textPost.setText("")
            textPost.activated(true)
            imageContainer =(this.getChildAt(1) as ImageGalleryView)
            imageContainer.removeAllViews()
            videoContainer =  (this.getChildAt(2) as VideoGalleryView)
            videoContainer.removeAllViews()
            audioContainer =  (this.getChildAt(3) as AudioGalleryView)
            audioContainer.removeAllViews()
        }
        (1 until this.childCount).forEach {
            this.removeViewAt(it)
            listAudioContainers.removeAt(it)
            listEditText.removeAt(it)
            listImageContainers.removeAt(it)
        }
        namesAudio.clear()
        namesImage.clear()
        namesVideo.clear()
        //namesVideoOrImage.clear()
    }

    private fun addEditText(editText:AppCompatEditText){
        textPost = editText
        setupEditText()
        listEditText.add(editText)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupEditText() {
        if (childCount > 1){
            textPost.hint = ""
        }
        textPost.setOnTouchListener { v, event ->
            changeActivatedAllEditText()
            v.activated(true)
            changeCurrentContainers()
            return@setOnTouchListener super.onTouchEvent(event)
        }
        textPost.setOnClickListener {v->
            changeActivatedAllEditText()
            v.activated(true)
            changeCurrentContainers()
        }
        textPost.activated(true)
    }

    private fun addMediaContainer(imageContainer:ImageGalleryView,audioContainer:AudioGalleryView,
        videoContainer: VideoGalleryView){
        this.imageContainer = imageContainer
        listImageContainers.add(imageContainer)
        this.audioContainer = audioContainer
        listAudioContainers.add(audioContainer)
        this.videoContainer = videoContainer
        listVideoContainers.add(videoContainer)
    }

    fun currentContainerIsLast():Boolean{
        listEditText.forEachIndexed { index, editText ->
            if (editText.isActivated && index == this.childCount - 1)
                return true
        }
        return false
    }

    fun allTextIsEmpty():Boolean{
        listEditText.forEach { editText->
            if (editText.text.toString().isNotEmpty()){
                return@allTextIsEmpty false
            }
        }
        return true
    }

    @SuppressLint("ResourceAsColor")
    private fun changeActivatedAllEditText(){
        listEditText.forEach { editText->
            editText.activated(false)
        }
    }

    private fun changeCurrentContainers(){
        listEditText.forEachIndexed { index,editText->
            if (editText.isActivated){
                imageContainer = listImageContainers[index]
                audioContainer = listAudioContainers[index]
                videoContainer = listVideoContainers[index]
            }
        }
    }

    fun addMusic(audioEntity: AudioEntity,view:DownloadAudioPlayerView){
        audioContainer.addAudio(audioEntity,view)
        namesAudio.add(Pair(audioEntity.song,view))
    }
    fun addImage(fileEntity: FileEntity, view:View){
        imageContainer.addImage(view)
        namesImage.add(Pair(fileEntity.title,view))
    }

    fun addVideo(fileEntity: FileEntity,view:DownloadVideoPlayerView){
        videoContainer.addVideo(fileEntity,view)
        namesVideo.add(Pair(fileEntity.title,view))
    }

    fun deleteName(view:View?){
        namesImage.forEach { pair->
            if (pair.second == view){
                namesImage.remove(pair)
                return@deleteName
            }
        }

        namesVideo.forEach { pair->
            if (pair.second == view){
                namesVideo.remove(pair)
                return@deleteName
            }
        }

        namesAudio.forEach { pair->
            if (pair.second == view){
                namesAudio.remove(pair)
                return@deleteName
            }
        }
    }

    fun createFinalText():String{
        var textBeforeSend = ""
        var numberImage = 0
        var numberAudio = 0
        (0 until this.childCount).forEach { index->
            textBeforeSend += listEditText[index].text.toString().trim()
            if (index != this.childCount-1 ){
                textBeforeSend += "~~{"
            }
            else{
                textBeforeSend.removePrefix("~~{}~~")
                Timber.tag("tut_end").d(textBeforeSend)
                return@createFinalText textBeforeSend
            }
            if (listImageContainers[index].childCount!=0) {
                (0 until listImageContainers[index].childCount).forEach { _ ->
                    textBeforeSend += "${namesImage[numberImage].first},"
                    numberImage++
                }
            }
            if (listVideoContainers[index].childCount!=0) {
                (0 until listVideoContainers[index].childCount).forEach { _ ->
                    textBeforeSend += "${namesVideo[numberImage].first},"
                    numberImage++
                }
            }
            if (listAudioContainers[index].childCount!=0) {
                (0 until listAudioContainers[index].childCount).forEach { _ ->
                    textBeforeSend += "${namesAudio[numberAudio].first},"
                    numberAudio++
                }
            }
            textBeforeSend+="}~~"
        }
        return textBeforeSend
    }
}