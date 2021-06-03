package com.intergroupapplication.presentation.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import com.intergroupapplication.R
import com.intergroupapplication.presentation.exstension.activated
import timber.log.Timber

class CreatePostCustomView @JvmOverloads constructor(context: Context,
                                                     private val attrs: AttributeSet? = null, private val defStyleAttr: Int = 0)
    : LinearLayout(context,attrs,defStyleAttr) {

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
    }

    lateinit var imageContainer:GridLayout
    lateinit var audioContainer:LinearLayout
    lateinit var textPost:AppCompatEditText
    val namesVideoOrImage = mutableListOf<Pair<String,View?>>()
    val namesAudio = mutableListOf<Pair<String,View?>>()
    val listImageContainers = mutableListOf<GridLayout>()
    val listAudioContainers = mutableListOf<LinearLayout>()
    val listEditText = mutableListOf<AppCompatEditText>()

    fun createAllMainView(){
        changeActivatedAllEditText()
        val view = LayoutInflater.from(context).inflate(R.layout.layout_create_post_view,this,false)
        addView(view)
        textPost = view.findViewById(R.id.postText)
        imageContainer = view.findViewById(R.id.imageAndVideoContainer)
        audioContainer = view.findViewById(R.id.audioContainer)
        setupEditText()
        listImageContainers.add(imageContainer)
        listEditText.add(textPost)
        listAudioContainers.add(audioContainer)
    }

    fun addViewInEditPost(editText: AppCompatEditText,imageContainer: GridLayout,audioContainer: LinearLayout){
        addMediaContainer(imageContainer, audioContainer)
        addEditText(editText)
    }

    private fun addEditText(editText:AppCompatEditText){
        textPost = editText
        setupEditText()
        listEditText.add(editText)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupEditText() {
        textPost.setOnTouchListener { v, event ->
            changeActivatedAllEditText()
            v.activated(true)
            changeCurrentContainers()
            return@setOnTouchListener super.onTouchEvent(event)
        }
        textPost.activated(true)
    }

    private fun addMediaContainer(imageContainer:GridLayout,audioContainer:LinearLayout){
        this.imageContainer = imageContainer
        listImageContainers.add(imageContainer)
        this.audioContainer = audioContainer
        listAudioContainers.add(audioContainer)
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

    fun addMusic(name:String,view:View?){
        audioContainer.addView(view)
        namesAudio.add(Pair(name,view))
    }

    private fun changeCurrentContainers(){
        listEditText.forEachIndexed { index,editText->
            if (editText.isActivated){
                imageContainer = listImageContainers[index]
                audioContainer = listAudioContainers[index]
            }
        }
    }

    fun addImageOrVideo(name:String,view:View?){
        imageContainer.addView(view)
        namesVideoOrImage.add(Pair(name,view))
    }

    fun detachName(view:View?){
        namesVideoOrImage.forEach { pair->
            if (pair.second == view){
                namesVideoOrImage.remove(pair)
                return@detachName
            }
        }

        namesAudio.forEach { pair->
            if (pair.second == view){
                namesAudio.remove(pair)
                return@detachName
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
                    textBeforeSend += "${namesVideoOrImage[numberImage].first},"
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