package com.intergroupapplication.presentation.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.mediaPlayer.DownloadVideoPlayerView

class CreateVideoGalleryView @JvmOverloads constructor(context: Context,
                       private val attrs: AttributeSet? = null, private val defStyleAttr: Int = 0):
    LinearLayout(context, attrs, defStyleAttr) {

    var expand: (isExpanded: Boolean) -> Unit = {}

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
    }

    private var container: LinearLayout = LinearLayout(context, attrs, defStyleAttr)
    val downloadVideoPlayerViewList = mutableListOf<Pair<FileEntity,DownloadVideoPlayerView>>()
    private var isExpanded: Boolean = false

    fun addVideo(fileEntity: FileEntity, view: DownloadVideoPlayerView){
        container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        downloadVideoPlayerViewList.add(Pair(fileEntity,view))
        createContainer(downloadVideoPlayerViewList, isExpanded)
    }

    private fun createContainer(list:List<Pair<FileEntity,DownloadVideoPlayerView>>, isExpanded: Boolean)
    {
        container.removeAllViews()
        this.removeAllViews()
        this.addView(container)
        if (isExpanded && list.size > 1){
            list.forEach { pair->
                setupDownloadVideoPlayerView(pair.first, pair.second)
            }
            val hider = LayoutInflater.from(context).inflate(
                R.layout.layout_hide, this,
                false)
            val btnHide = hider.findViewById<FrameLayout>(R.id.btnHide)
            btnHide.setOnClickListener {
                this.isExpanded = false
                createContainer(downloadVideoPlayerViewList, this.isExpanded)
            }
            this.addView(hider)
        }
        else if(!isExpanded && list.size > 1){
            list.subList(0,1).forEach { pair->
                setupDownloadVideoPlayerView(pair.first, pair.second)
            }
            val expander = LayoutInflater.from(context).inflate(
                R.layout.layout_expand, this,
                false)
            val btnExpand = expander.findViewById<FrameLayout>(R.id.btnExpand)
            btnExpand.setOnClickListener {
                this.isExpanded = true
                createContainer(downloadVideoPlayerViewList, this.isExpanded)
            }
            this.addView(expander)
        }
        else {
            list.forEach { pair ->
                setupDownloadVideoPlayerView(pair.first, pair.second)
            }
        }
    }

    private fun setupDownloadVideoPlayerView(
        videoEntity: FileEntity,
        view: DownloadVideoPlayerView
    ) {
        val player = makeVideoPlayer(videoEntity, view)
        view.exoPlayer.controllerHideOnTouch = false
        view.exoPlayer.player = player
        container.addView(view)
        if (player.playWhenReady) {
            player.playWhenReady = false
            player.playWhenReady = true
        }
    }

    private fun makeVideoPlayer(video: FileEntity,
                                playerView: DownloadVideoPlayerView
    ): SimpleExoPlayer {
        val videoPlayer =  SimpleExoPlayer.Builder(context).build()
        val listener = object: Player.EventListener{
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (playWhenReady) {
                    playerView.exoPlayer.controllerHideOnTouch = true
                    playerView.previewForVideo.gone()
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                when (state) {
                    Player.STATE_ENDED -> {
                        playerView.exoPlayer.controllerHideOnTouch = false
                        playerView.previewForVideo.show()
                    }
                    else -> {
                        playerView.exoPlayer.controllerHideOnTouch = true
                        playerView.previewForVideo.gone()
                    }
                }
            }
        }
        videoPlayer.addListener(listener)
        val videoMediaItem: MediaItem = MediaItem.fromUri(video.file)
        videoPlayer.setMediaItem(videoMediaItem)
        return videoPlayer
    }

    fun removeVideoView(view: View?){
        container.removeView(view)
        downloadVideoPlayerViewList.removeView(view)
        createContainer(downloadVideoPlayerViewList,isExpanded)
    }


    private fun MutableList<Pair<FileEntity,DownloadVideoPlayerView>>.removeView(view:View?){
        this.forEach {pair->
            if (pair.second == view){
                this.remove(pair)
                return
            }
        }
    }
}