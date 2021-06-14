package com.intergroupapplication.presentation.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadAudioPlayerView

class CreateAudioGalleryView  @JvmOverloads constructor(context: Context,
                                                        private val attrs: AttributeSet? = null, private val defStyleAttr: Int = 0):
    LinearLayout(context, attrs, defStyleAttr) {

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
    }

    private var container: LinearLayout = LinearLayout(context, attrs, defStyleAttr)
    val downloadAudioPlayerViewList = mutableListOf<Pair<AudioEntity, DownloadAudioPlayerView>>()

    private var isExpanded: Boolean = false

    fun addAudio(audioEntity: AudioEntity, view: DownloadAudioPlayerView){
        container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        downloadAudioPlayerViewList.add(Pair(audioEntity, view))
        createContainer(downloadAudioPlayerViewList,isExpanded)
    }

    private fun createContainer(list:List<Pair<AudioEntity,DownloadAudioPlayerView>>, isExpanded: Boolean)
    {
        container.removeAllViews()
        this.removeAllViews()
        this.addView(container)
        if (isExpanded && list.size > 2){
            list.forEach { pair->
                setupDownloadAudioPlayerView(pair.first, pair.second)
            }
            val hider = LayoutInflater.from(context).inflate(
                R.layout.layout_hide, this,
                false)
            val btnHide = hider.findViewById<FrameLayout>(R.id.btnHide)
            btnHide.setOnClickListener {
                this.isExpanded = false
                createContainer(downloadAudioPlayerViewList, this.isExpanded)
            }
            this.addView(hider)
        }
        else if(!isExpanded && list.size > 2){
            list.subList(0, 2).forEach { pair->
                setupDownloadAudioPlayerView(pair.first, pair.second)
            }
            val expander = LayoutInflater.from(context).inflate(
                R.layout.layout_expand, this,
                false)
            val btnExpand = expander.findViewById<FrameLayout>(R.id.btnExpand)
            btnExpand.setOnClickListener {
                this.isExpanded = true
                createContainer(downloadAudioPlayerViewList, this.isExpanded)
            }
            this.addView(expander)
        }
        else {
            list.forEach { pair ->
                setupDownloadAudioPlayerView(pair.first, pair.second)
            }
        }
    }

    private fun setupDownloadAudioPlayerView(
        audioEntity: AudioEntity, view: DownloadAudioPlayerView) {
        val player = makeAudioPlayer(audioEntity)
        view.exoPlayer.controllerHideOnTouch = false
        view.exoPlayer.player = player
        container.addView(view)
        if (player.playWhenReady) {
            player.playWhenReady = false
            player.playWhenReady = true
        }
    }

    private fun makeAudioPlayer(audio: AudioEntity)
            : SimpleExoPlayer {
        val musicPlayer = SimpleExoPlayer.Builder(context).build()
        val musicMediaItem: MediaItem = MediaItem.fromUri(audio.file)
        musicPlayer.setMediaItem(musicMediaItem)
        return musicPlayer
    }

    fun removeAudioView(view: View?){
        container.removeView(view)
        downloadAudioPlayerViewList.removeView(view)
        createContainer(downloadAudioPlayerViewList,isExpanded)
    }

    private fun MutableList<Pair<AudioEntity, DownloadAudioPlayerView>>.removeView(view:View?){
        this.forEach {pair->
            if (pair.second == view){
                this.remove(pair)
                return
            }
        }
    }

}