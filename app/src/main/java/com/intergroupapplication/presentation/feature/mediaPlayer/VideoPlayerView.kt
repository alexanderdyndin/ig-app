package com.intergroupapplication.presentation.feature.mediaPlayer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.danikula.videocache.CacheListener
import com.danikula.videocache.HttpProxyCacheServer
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.exstension.getActivity
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class VideoPlayerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val _exoPlayer = LayoutInflater.from(context).inflate(R.layout.view_video_player, this)
    val exoPlayer by lazy<PlayerView> { _exoPlayer.findViewById(R.id.videoExoPlayerView) }
    val previewForVideo by lazy<SimpleDraweeView> { _exoPlayer.findViewById(R.id.previewForVideo) }
    val nameVideo by lazy<TextView> { _exoPlayer.findViewById(R.id.nameVideo) }
    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }
}