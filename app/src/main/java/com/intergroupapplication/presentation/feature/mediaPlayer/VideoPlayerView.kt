package com.intergroupapplication.presentation.feature.mediaPlayer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.intergroupapplication.R

class VideoPlayerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val _exoPlayer = LayoutInflater.from(context).inflate(R.layout.view_video_player, this)
    val exoPlayer by lazy<StyledPlayerView> { _exoPlayer.findViewById(R.id.videoExoPlayerView) }

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }
}