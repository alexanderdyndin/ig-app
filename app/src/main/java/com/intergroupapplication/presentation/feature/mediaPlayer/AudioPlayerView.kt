package com.intergroupapplication.presentation.feature.mediaPlayer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.intergroupapplication.R

class IGAudioPlayerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val _exoPlayer = LayoutInflater.from(context).inflate(R.layout.view_music_player, this)
    val exoPlayer by lazy { _exoPlayer.findViewById<PlayerView>(R.id.musicExoPlayerView) }

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }
}