package com.intergroupapplication.presentation.customview

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.setPadding
import com.danikula.videocache.CacheListener
import com.danikula.videocache.HttpProxyCacheServer
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.github.florent37.shapeofview.shapes.CutCornerView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.exstension.dpToPx
import com.intergroupapplication.presentation.exstension.getActivity
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.intergroupapplication.presentation.feature.mediaPlayer.AudioPlayerView
import com.intergroupapplication.presentation.feature.mediaPlayer.IGMediaService
import com.intergroupapplication.presentation.feature.mediaPlayer.VideoPlayerView
import kotlinx.android.synthetic.main.layout_2pic.view.*
import kotlinx.android.synthetic.main.layout_2pic.view.pic1
import kotlinx.android.synthetic.main.layout_2pic.view.pic2
import kotlinx.android.synthetic.main.layout_3pic.view.*
import kotlinx.android.synthetic.main.layout_expand.view.*
import kotlinx.android.synthetic.main.layout_hide.view.*
import kotlinx.android.synthetic.main.layout_pic.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.zip.Inflater
import kotlin.math.roundToInt

class AudioGalleryView @JvmOverloads constructor(context: Context,
                                                 private val attrs: AttributeSet? = null, private val defStyleAttr: Int = 0):
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
        container.removeAllViews()
        this.addView(container)
        if (isExpanded && urls.size > 2) {
            createAudios(urls)
            container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, context.dpToPx(60 * urls.size))
            val hider = LayoutInflater.from(context).inflate(R.layout.layout_hide, this, false)
            hider.btnHide.setOnClickListener {
                this.isExpanded = false
                parseUrl(uris, this.isExpanded)
                expand.invoke(this.isExpanded)
            }
            this.addView(hider)
        } else if (!isExpanded && urls.size > 2) {
            createAudios(urls.subList(0, 2))
            container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, context.dpToPx(60 * 2))
            val expander = LayoutInflater.from(context).inflate(R.layout.layout_expand, this, false)
            expander.btnExpand.setOnClickListener {
                this.isExpanded = true
                parseUrl(uris, this.isExpanded)
                expand.invoke(this.isExpanded)
            }
            this.addView(expander)
        } else if (urls.isNotEmpty()) {
            createAudios(urls)
            container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, context.dpToPx(60 * urls.size))
        }
    }

    private fun createAudios(urls: List<AudioEntity>) {
        val activity = this.getActivity()
        if (activity is MainActivity) {
            CoroutineScope(Dispatchers.Main).launch {
                val bindedService = activity.bindMediaService()
                bindedService?.let {
                    urls.forEach {
                        val player = makeAudioPlayer(it, bindedService)
                        val playerView = AudioPlayerView(context)
                        playerView.trackName = "${it.artist} - ${it.song}"
                        playerView.trackOwner = "Загрузил (ID:${it.owner})"
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

    private fun makeAudioPlayer(audio: AudioEntity, service: IGMediaService.ServiceBinder): SimpleExoPlayer {

        val musicPlayer = if (service.getMediaFile() == IGMediaService.MediaFile(true, audio.id)) {
            val bindedPlayer = service.getExoPlayerInstance()
            if (bindedPlayer != null) return bindedPlayer
            else SimpleExoPlayer.Builder(context).build()
        }
        else SimpleExoPlayer.Builder(context).build()

        val listener = object : Player.EventListener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (playWhenReady) {
                    service.setPlayer(musicPlayer, IGMediaService.MediaFile(true, audio.id), audio.song, audio.description)
                }
            }
        }
        musicPlayer.addListener(listener)

        proxy?.let {
            val proxyUrl = it.getProxyUrl(audio.file)
            val musicMediaItem: MediaItem = MediaItem.fromUri(proxyUrl)
            // Set the media item to be played.
            musicPlayer.setMediaItem(musicMediaItem)
        } ?: let {
            // Build the media item.
            val musicMediaItem: MediaItem = MediaItem.fromUri(audio.file)
            // Set the media item to be played.
            musicPlayer.setMediaItem(musicMediaItem)
            // Prepare the player.
            //musicPlayer.prepare()
        }


        return musicPlayer
    }

}