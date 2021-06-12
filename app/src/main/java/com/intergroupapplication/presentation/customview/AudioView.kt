package com.intergroupapplication.presentation.customview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.danikula.videocache.HttpProxyCacheServer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.presentation.exstension.getActivity
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.intergroupapplication.presentation.feature.mediaPlayer.AudioPlayerView
import com.intergroupapplication.presentation.feature.mediaPlayer.IGMediaService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class AudioView @JvmOverloads constructor(context: Context, private val attrs: AttributeSet? = null,
          private val defStyleAttr: Int = 0):LinearLayout(context,attrs,defStyleAttr) {

    companion object{
        var mediaPlayer:SimpleExoPlayer? = null
    }
    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
    }

    private var container: LinearLayout = LinearLayout(context, attrs, defStyleAttr).apply {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }



    fun setupAudio(audioEntity: AudioEntity){
        this.removeAllViews()
        this.addView(container)
       createAudio(audioEntity)
    }

    private fun createAudio(audioEntity: AudioEntity){
        val activity = this@AudioView.getActivity()
        if (activity is MainActivity){
            CoroutineScope(Dispatchers.Main).launch {
                val bindService = activity.bindMediaService()
                bindService?.let {
                    setupAudioPlayerView(audioEntity,it)
                }
            }
        }
    }

    private fun setupAudioPlayerView(
        audioEntity: AudioEntity,
        bindService: IGMediaService.ServiceBinder
    ) {
        val playerView = AudioPlayerView(context)
        if (mediaPlayer == null) {
            mediaPlayer = makeAudioPlayer(audioEntity, bindService, playerView)
        }
        val trackName =
            if (audioEntity.artist == "")
                audioEntity.song
            else "${audioEntity.artist} - ${audioEntity.song}"
        playerView.trackName = trackName
        playerView.trackOwner = "Загрузил (ID:${audioEntity.owner})"
        playerView.durationTrack.text =
            if (audioEntity.duration != "") audioEntity.duration else "00:00"
        playerView.exoPlayer.player = mediaPlayer
        mediaPlayer?.addMediaItem(MediaItem.fromUri(audioEntity.file))
        container.addView(playerView)
        if (mediaPlayer?.playWhenReady!!) {
            mediaPlayer?.playWhenReady = false
            mediaPlayer?.playWhenReady = true
        }
    }

    private fun makeAudioPlayer(audio: AudioEntity, service: IGMediaService.ServiceBinder,
                                playerView: AudioPlayerView): SimpleExoPlayer {

        val musicPlayer = if (service.getMediaFile() == IGMediaService.MediaFile(true, audio.id)) {
            val bindPlayer = service.getExoPlayerInstance()
            if (bindPlayer != null){
                return bindPlayer
            }
            else SimpleExoPlayer.Builder(context).build()
        }
        else SimpleExoPlayer.Builder(context).build()

        val listener = object : Player.EventListener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (playWhenReady) {
                    service.setAudioPlayer(musicPlayer, IGMediaService.MediaFile(true, audio.id),
                        audio.song, audio.description,playerView)
                }
                service.changeControlButton(playWhenReady)
            }

            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                Timber.tag("tut_playback").d(state.toString())
            }
        }
        musicPlayer.addListener(listener)
        return musicPlayer
    }

}