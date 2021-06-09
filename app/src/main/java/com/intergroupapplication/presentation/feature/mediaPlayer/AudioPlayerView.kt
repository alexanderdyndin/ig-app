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
    private val _exoPlayer = LayoutInflater.from(context).inflate(R.layout.view_music_player, this)
    val exoPlayer by lazy<PlayerView> { _exoPlayer.findViewById(R.id.musicExoPlayerView) }
    val exoProgress by lazy<CustomTimeBar> { _exoPlayer.findViewById(R.id.exo_progress) }
    private val nameTrack =  _exoPlayer.findViewById<TextView>(R.id.trackName)
    private val whomTrack =  _exoPlayer.findViewById<TextView>(R.id.fromWhom)
    val durationTrack = _exoPlayer.findViewById<TextView>(R.id.exoDuration)
    var trackName: String = ""
        set(value) {
            field = value
            nameTrack?.text = value
        }
    var trackOwner: String = ""
        set(value) {
            field = value
            whomTrack?.text = value
        }


    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }
}