package com.intergroupapplication.presentation.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.danikula.videocache.HttpProxyCacheServer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.presentation.exstension.getActivity
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.intergroupapplication.presentation.feature.mediaPlayer.AudioPlayerView
import com.intergroupapplication.presentation.feature.mediaPlayer.IGMediaService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioGalleryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr) {
    var expand: (isExpanded: Boolean) -> Unit = {}

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
    }

    private var container: LinearLayout = LinearLayout(context, attrs, defStyleAttr)

    private var uris: List<AudioEntity> = emptyList()

    private var isExpanded: Boolean = false

    var proxy: HttpProxyCacheServer? = null

    fun setAudios(uris: List<AudioEntity>, isExpanded: Boolean = false) {
        this.uris = uris
        this.isExpanded = isExpanded
        parseUrl(uris, isExpanded)
    }

    private fun parseUrl(urls: List<AudioEntity>, isExpanded: Boolean) {
        this.removeAllViews()
        container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        container.removeAllViews()
        this.addView(container)
        if (isExpanded && urls.size > 2) {
            createAudios(urls)
            container.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            val hider = LayoutInflater.from(context).inflate(R.layout.layout_hide, this, false)
            val btnHide = hider.findViewById<FrameLayout>(R.id.btnHide)
            btnHide.setOnClickListener {
                this.isExpanded = false
                parseUrl(uris, this.isExpanded)
                expand(this.isExpanded)
            }
            this.addView(hider)
        } else if (!isExpanded && urls.size > 2) {
            createAudios(urls.subList(0, 2))
            container.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            val expander = LayoutInflater.from(context).inflate(R.layout.layout_expand, this, false)
            val btnExpand = expander.findViewById<FrameLayout>(R.id.btnExpand)
            btnExpand.setOnClickListener {
                this.isExpanded = true
                parseUrl(uris, this.isExpanded)
                expand(this.isExpanded)
            }
            this.addView(expander)
        } else if (urls.isNotEmpty()) {
            createAudios(urls)
            container.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
    }

    private fun createAudios(urls: List<AudioEntity>) {
        val activity = this.getActivity()
        if (activity is MainActivity) {
            CoroutineScope(Dispatchers.Main).launch {
                val bindService = activity.bindMediaService()
                bindService?.let {
                    urls.forEach {
                        val playerView = AudioPlayerView(context)
                        val player = makeAudioPlayer(it, bindService, playerView)
                        val trackName =
                            if (it.artist == "") it.nameSong else "${it.artist} - ${it.nameSong}"
                        playerView.trackName = trackName
                        playerView.trackOwner = "Загрузил (ID:${it.owner})"
                        playerView.durationTrack.text =
                            if (it.duration != "") it.duration else "00:00"
                        playerView.exoPlayer.player = player
                        container.addView(playerView)
                        if (player.playWhenReady) {
                            player.playWhenReady = false
                            player.playWhenReady = true
                        }
                    }

                }
            }
        } else throw Exception("Activity is not MainActivity")
    }

    private fun makeAudioPlayer(
        audio: AudioEntity, service: IGMediaService.ServiceBinder,
        playerView: AudioPlayerView
    ): SimpleExoPlayer {

        val musicPlayer =
            if (service.getMediaFile() == IGMediaService.MediaFile(true, audio.id)) {
                val bindPlayer = service.getExoPlayerInstance()
                if (bindPlayer != null) {

                    return bindPlayer
                } else SimpleExoPlayer.Builder(context).build()
            } else SimpleExoPlayer.Builder(context).build()

        val listener = object : Player.EventListener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (playWhenReady) {
                    service.setAudioPlayer(
                        musicPlayer,
                        IGMediaService.MediaFile(true, audio.id), audio.nameSong,
                        audio.description, playerView
                    )
                }
                service.changeControlButton(playWhenReady)
            }

            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                if (state == Player.STATE_ENDED) {
                    service.changeControlButton(false)
                }
            }
        }
        musicPlayer.addListener(listener)
        proxy?.let {
            val proxyUrl = it.getProxyUrl(audio.urlFile)
            it.registerCacheListener({ _, _, percentsAvailable ->
                playerView.exoProgress.setLocalCacheBufferedPosition(percentsAvailable)
            }, audio.urlFile)
            if (it.isCached(audio.urlFile)) {
                playerView.exoProgress.setLocalCacheBufferedPosition(100)
            }
            val musicMediaItem: MediaItem = MediaItem.fromUri(proxyUrl)
            musicPlayer.setMediaItem(musicMediaItem)
        } ?: let {
            val musicMediaItem: MediaItem = MediaItem.fromUri(audio.urlFile)
            musicPlayer.setMediaItem(musicMediaItem)
        }
        return musicPlayer
    }
}
