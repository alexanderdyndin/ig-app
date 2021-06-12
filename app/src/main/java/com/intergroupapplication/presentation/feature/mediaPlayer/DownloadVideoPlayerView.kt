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

class DownloadVideoPlayerView  @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val _exoPlayer = LayoutInflater.from(context).inflate(R.layout.view_download_video_player,
        this)
    val exoPlayer by lazy<PlayerView> { _exoPlayer.findViewById(R.id.videoExoPlayerView) }
    val previewForVideo by lazy<SimpleDraweeView> { _exoPlayer.findViewById(R.id.previewForVideo) }
    val nameVideo by lazy<TextView> { _exoPlayer.findViewById(R.id.nameVideo) }
    val durationVideo by lazy<TextView> {_exoPlayer.findViewById(R.id.exoDuration)  }
    val exoProgress by lazy<CustomTimeBar> { _exoPlayer.findViewById(R.id.exo_progress) }
    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }
}