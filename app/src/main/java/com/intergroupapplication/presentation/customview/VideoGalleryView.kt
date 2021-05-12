package com.intergroupapplication.presentation.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.danikula.videocache.CacheListener
import com.danikula.videocache.HttpProxyCacheServer
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.dpToPx
import com.intergroupapplication.presentation.exstension.getActivity
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.exstension.pxToDp
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.intergroupapplication.presentation.feature.mediaPlayer.IGMediaService
import com.intergroupapplication.presentation.feature.mediaPlayer.VideoPlayerView
import kotlinx.android.synthetic.main.layout_expand.view.*
import kotlinx.android.synthetic.main.layout_hide.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class VideoGalleryView @JvmOverloads constructor(context: Context,
                                                 private val attrs: AttributeSet? = null, private val defStyleAttr: Int = 0):
        LinearLayout(context, attrs, defStyleAttr) {

    var expand: (isExpanded: Boolean) -> Unit = {}

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
    }

    private var container: LinearLayout = LinearLayout(context, attrs, defStyleAttr)

    private var uris: List<FileEntity> = emptyList()

    private var isExpanded: Boolean = false

    var proxy: HttpProxyCacheServer? = null
    var imageLoadingDelegate: ImageLoadingDelegate? = null

    fun setVideos(uris: List<FileEntity>, isExpanded: Boolean = false) {
        this.uris = uris
        this.isExpanded = isExpanded
        parseUrl(uris, isExpanded)
    }

    private fun parseUrl(urls: List<FileEntity>, isExpanded: Boolean) {
        container.removeAllViews()
        container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.removeAllViews()
        this.addView(container)
        if (isExpanded && urls.size > 1) {
            createVideos(urls)
            container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, context.dpToPx(300 * urls.size))
            val hider = LayoutInflater.from(context).inflate(R.layout.layout_hide, this, false)
            hider.btnHide.setOnClickListener {
                this.isExpanded = false
                parseUrl(uris, this.isExpanded)
                expand.invoke(this.isExpanded)
            }
            this.addView(hider)
        } else if (!isExpanded && urls.size > 1) {
            createVideos(urls.subList(0, 1))
            container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, context.dpToPx(300))
            val expander = LayoutInflater.from(context).inflate(R.layout.layout_expand, this, false)
            expander.btnExpand.setOnClickListener {
                this.isExpanded = true
                parseUrl(uris, this.isExpanded)
                expand.invoke(this.isExpanded)
            }
            this.addView(expander)
        } else if (urls.isNotEmpty()) {
            createVideos(urls)
            container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, context.dpToPx(300 * urls.size))
        }
    }

    private fun createVideos(urls: List<FileEntity>) {
        val activity = this.getActivity()
        if (activity is MainActivity) {
            CoroutineScope(Dispatchers.Main).launch {
                val bindedService = activity.bindMediaService()
                bindedService?.let {
                    urls.forEach {
                        val playerView = VideoPlayerView(context)
                        val player = makeVideoPlayer(it, bindedService,playerView.previewForVideo)
                        playerView.exoPlayer.player = player
                        imageLoadingDelegate?.loadImageFromUrl(it.preview,playerView.previewForVideo)
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

    private fun makeVideoPlayer(video: FileEntity, service: IGMediaService.ServiceBinder,preview:SimpleDraweeView): SimpleExoPlayer {
        val videoPlayer = if (service.getMediaFile() == IGMediaService.MediaFile(false, video.id)) {
            val bindedPlayer = service.getExoPlayerInstance()
            if (bindedPlayer != null) return bindedPlayer
            else SimpleExoPlayer.Builder(context).build()
        }
        else SimpleExoPlayer.Builder(context).build()

        val listener = object : Player.EventListener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (playWhenReady) {
                    preview.gone()
                    service.setPlayer(videoPlayer, IGMediaService.MediaFile(false, video.id), video.title, video.description)
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                if (state == Player.STATE_ENDED){
                    preview.show()
                }
                else{
                    preview.gone()
                }
            }
        }
        videoPlayer.addListener(listener)
        proxy?.let {
            it.registerCacheListener(CacheListener { cacheFile, url, percentsAvailable ->
                Timber.d(String.format("onCacheAvailable. percents: %d, file: %s, url: %s", percentsAvailable, cacheFile, url));
            }, video.file)
            val proxyUrl = it.getProxyUrl(video.file)
            val videoMediaItem: MediaItem = MediaItem.fromUri(proxyUrl)
            // Set the media item to be played.
            videoPlayer.setMediaItem(videoMediaItem)
        } ?: let {
            // Build the media item.
            val videoMediaItem: MediaItem = MediaItem.fromUri(video.file)
            // Set the media item to be played.
            videoPlayer.setMediaItem(videoMediaItem)
            // Prepare the player.
//                    musicPlayer.prepare()
        }
        return videoPlayer
    }

}