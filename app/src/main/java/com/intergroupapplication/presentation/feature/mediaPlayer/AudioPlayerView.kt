package com.intergroupapplication.presentation.feature.mediaPlayer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.exoplayer2.ui.PlayerView
import com.intergroupapplication.R
import com.intergroupapplication.presentation.customview.CustomTimeBar

class AudioPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    val exoPlayer: PlayerView by lazy(LazyThreadSafetyMode.NONE) {
        _exoPlayer.findViewById(R.id.musicExoPlayerView)
    }
    val exoProgress: CustomTimeBar by lazy(LazyThreadSafetyMode.NONE) {
        _exoPlayer.findViewById(R.id.exo_progress)
    }
    val durationTrack: TextView by lazy(LazyThreadSafetyMode.NONE) {
        _exoPlayer.findViewById(R.id.exoDuration)
    }
    private val _exoPlayer = LayoutInflater.from(context).inflate(R.layout.view_music_player, this)
    private val nameTrack: TextView by lazy(LazyThreadSafetyMode.NONE) {
        _exoPlayer.findViewById(R.id.trackName)
    }
    private val whomTrack: TextView by lazy(LazyThreadSafetyMode.NONE) {
        _exoPlayer.findViewById(R.id.fromWhom)
    }
    var trackName: String = ""
        set(value) {
            field = value
            nameTrack.text = value
        }
    var trackOwner: String = ""
        set(value) {
            field = value
            whomTrack.text = value
        }


    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }
}