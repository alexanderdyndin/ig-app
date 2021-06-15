package com.intergroupapplication.presentation.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.forEachIndexed
import androidx.core.view.isEmpty
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.exstension.activated
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadAudioPlayerView
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadVideoPlayerView
import timber.log.Timber

class CreatePostCustomView @JvmOverloads constructor(context: Context,
                                             attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context,attrs,defStyleAttr) {

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
    }

    private lateinit var imageContainer:CreateImageGalleryView
    private lateinit var videoContainer:CreateVideoGalleryView
    private lateinit var audioContainer:CreateAudioGalleryView
    lateinit var textPost:AppCompatEditText
    val namesImage = mutableListOf<Pair<String,View?>>()
    val namesVideo = mutableListOf<Pair<String,View?>>()
    val namesAudio = mutableListOf<Pair<String,View?>>()
    val listImageContainers = mutableListOf<CreateImageGalleryView>()
    val listAudioContainers = mutableListOf<CreateAudioGalleryView>()
    val listVideoContainers = mutableListOf<CreateVideoGalleryView>()
    val listEditText = mutableListOf<AppCompatEditText>()

    fun createAllMainView(){
        changeActivatedAllEditText()
        val view = LayoutInflater.from(context).inflate(R.layout.layout_create_post_view,this,
            false)
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

    fun addViewInEditPost(editText: AppCompatEditText,imageContainer: CreateImageGalleryView,
                  audioContainer: CreateAudioGalleryView,videoGalleryView: CreateVideoGalleryView){
        addMediaContainer(imageContainer, audioContainer,videoGalleryView)
        addEditText(editText)
    }

    fun removeAllBesidesFirstView(){
        removeAllViewsAndContainer()
        createAllMainView()
    }

    fun removeAllViewsAndContainer(){
        clearAllList()
        this.removeAllViews()
    }

    private fun clearAllList() {
        namesAudio.clear()
        namesImage.clear()
        namesVideo.clear()
        listEditText.clear()
        listImageContainers.clear()
        listVideoContainers.clear()
        listAudioContainers.clear()
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

    private fun addMediaContainer(imageContainer:CreateImageGalleryView,
                  audioContainer:CreateAudioGalleryView, videoContainer:CreateVideoGalleryView){
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
        removeContainer()
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

    private fun removeContainer(){
        this.forEachIndexed { index,view->
            if (listEditText[index].text.toString().trim().isEmpty() &&
                listAudioContainers[index].downloadAudioPlayerViewList.isEmpty() &&
                listVideoContainers[index].downloadVideoPlayerViewList.isEmpty() &&
                    listImageContainers[index].isEmpty() && index != this.childCount - 1){
                removeEmptyContainer(index, view)
                return@removeContainer
            }
        }
    }

    private fun removeEmptyContainer(index: Int, view: View) {
        listEditText.removeAt(index)
        listVideoContainers.removeAt(index)
        listAudioContainers.removeAt(index)
        listImageContainers.removeAt(index)
        this.removeView(view)
    }

    fun createFinalText():String{
        var textBeforeSend = ""
        var numberImage = 0
        var numberVideo = 0
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
                listImageContainers[index].downloadImageView.forEach { _ ->
                    textBeforeSend += "${namesImage[numberImage].first},"
                    numberImage++
                }
            }
            if (listVideoContainers[index].childCount!=0) {
                listVideoContainers[index].downloadVideoPlayerViewList.forEach { pair->
                    pair.second.exoPlayer.player?.pause()
                    pair.second.exoPlayer.player = null
                    textBeforeSend += "${namesVideo[numberImage].first},"
                    numberVideo++
                }
            }
            if (listAudioContainers[index].childCount!=0) {
                listAudioContainers[index].downloadAudioPlayerViewList.forEach { pair ->
                    pair.second.exoPlayer.player?.pause()
                    pair.second.exoPlayer.player = null
                    textBeforeSend += "${namesAudio[numberAudio].first},"
                    numberAudio++
                }
            }
            textBeforeSend+="}~~"
        }
        return textBeforeSend
    }
}