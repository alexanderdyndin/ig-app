package com.intergroupapplication.presentation.feature.mediaPlayer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.exoplayer2.ui.PlayerView
import com.intergroupapplication.R
import com.intergroupapplication.presentation.customview.CustomTimeBar

class VideoPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    val exoPlayer: PlayerView by lazy(LazyThreadSafetyMode.NONE) {
        _exoPlayer.findViewById(R.id.videoExoPlayerView)
    }
    val previewForVideo: SimpleDraweeView by lazy(LazyThreadSafetyMode.NONE) {
        _exoPlayer.findViewById(R.id.previewForVideo)
    }
    val nameVideo: TextView by lazy(LazyThreadSafetyMode.NONE) {
        _exoPlayer.findViewById(R.id.nameVideo)
    }
    val durationVideo: TextView by lazy(LazyThreadSafetyMode.NONE) {
        _exoPlayer.findViewById(R.id.exoDuration)
    }
    val exoProgress: CustomTimeBar by lazy(LazyThreadSafetyMode.NONE) {
        _exoPlayer.findViewById(R.id.exo_progress)
    }
    private val _exoPlayer = LayoutInflater.from(context).inflate(R.layout.view_video_player, this)

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }
}
