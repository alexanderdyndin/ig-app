package com.intergroupapplication.presentation.feature.mediaPlayer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.intergroupapplication.R
import com.intergroupapplication.data.model.AudioInAddFileModel

class AudioForAddFilesBottomSheetPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    val exoPlayer: PlayerView by lazy(LazyThreadSafetyMode.NONE) {
        _exoPlayer.findViewById(R.id.musicExoPlayerView)
    }
    val addAudioButton: Button by lazy(LazyThreadSafetyMode.NONE) {
        _exoPlayer.findViewById(R.id.addAudioButton)
    }
    private val _exoPlayer =
        LayoutInflater.from(context).inflate(R.layout.item_audio_for_add_files_bottom_sheet, this)
    private val nameTrack = _exoPlayer.findViewById<TextView>(R.id.trackName)
    private val durationTrack = _exoPlayer.findViewById<TextView>(R.id.audioDuration)
    private var trackName: String = ""
        set(value) {
            field = value
            nameTrack?.text = value
        }

    private var duration: String = ""
        set(value) {
            field = value
            durationTrack?.text = value
        }

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }


    fun setupDownloadAudioPlayerView(
        audioModel: AudioInAddFileModel
    ) {
        val player = makeAudioPlayer(audioModel)
        trackName = audioModel.name
        duration = audioModel.duration
        exoPlayer.controllerHideOnTouch = false
        exoPlayer.player = player
        if (player.playWhenReady) {
            player.playWhenReady = false
            player.playWhenReady = true
        }
    }

    private fun makeAudioPlayer(audioModel: AudioInAddFileModel)
            : SimpleExoPlayer {
        val musicPlayer = SimpleExoPlayer.Builder(context).build()
        val musicMediaItem: MediaItem = MediaItem.fromUri(audioModel.url)
        musicPlayer.setMediaItem(musicMediaItem)
        return musicPlayer
    }
}