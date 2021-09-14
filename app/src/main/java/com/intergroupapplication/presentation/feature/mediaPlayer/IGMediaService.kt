package com.intergroupapplication.presentation.feature.mediaPlayer

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.browse.MediaBrowser
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.service.media.MediaBrowserService
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.intergroupapplication.R
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity


class IGMediaService : MediaBrowserService() {

    companion object {
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
    }


    private var exoPlayer: SimpleExoPlayer? = null
    private var audioPlayerView: AudioPlayerView? = null
    private var videoPlayerView: VideoPlayerView? = null
    private var mediaFile: MediaFile? = null
    private var notificationTitle: String? = null
    private var notificationSubtitle: String? = null
    private val audioManager by lazy {
        applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private val afChangeListener by lazy {
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    exoPlayer?.pause()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    exoPlayer?.pause()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    exoPlayer?.volume = 0.1f
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    exoPlayer?.let {
                        it.volume = 1f
                        it.play()
                    }
                }
            }
        }
    }

    /**
     * Will be called by our activity to get information about exo player.
     */

    private fun stop() {
        stopForeground(true)
    }

    override fun onCreate() {
        super.onCreate()
        val trackSelection = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(this, trackSelection)
        exoPlayer = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_FOREGROUND_SERVICE -> {
                stop()
            }
            ACTION_PLAY -> {
                exoPlayer?.let { exo -> exo.playWhenReady = true }
                displayNotification(true)
            }
            ACTION_PAUSE -> {
                exoPlayer?.let { exo -> exo.playWhenReady = false }
                displayNotification(false)
                stopForeground(false)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        exoPlayer?.playWhenReady = false
        return ServiceBinder()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return null
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {

    }

    override fun onUnbind(intent: Intent?): Boolean {
        exoPlayer?.stop()
        return super.onUnbind(intent)
    }

    /**
     * This class will be what is returned when an activity binds to this service.
     * The activity will also use this to know what it can get from our service to know
     * about the video playback.
     */
    inner class ServiceBinder : Binder() {

        /**
         * This method should be used only for setting the exoplayer instance.
         * If exoplayer's internal are altered or accessed we can not guarantee
         * things will work correctly.
         */
        fun getExoPlayerInstance() = exoPlayer
        fun getMediaFile() = mediaFile

        fun setAudioPlayer(
            player: SimpleExoPlayer,
            mediaFile: MediaFile,
            notificationTitle: String?,
            notificationSubtitle: String?,
            audioPlayer: AudioPlayerView
        ) {
            if (player != exoPlayer) {
                exoPlayer?.pause()
                audioPlayerView?.exoProgress?.setLocalCacheBufferedPosition(0)
                videoPlayerView?.exoProgress?.setLocalCacheBufferedPosition(0)
                audioPlayerView = audioPlayer
                exoPlayer?.seekTo(0)
                audioPlayer.exoProgress.setLocalCacheBufferedPosition(100)
                exoPlayer = player
            }
            this@IGMediaService.notificationTitle = notificationTitle
            this@IGMediaService.notificationSubtitle = notificationSubtitle
            this@IGMediaService.mediaFile = mediaFile
            displayNotification(true)
        }

        fun setVideoPlayer(
            player: SimpleExoPlayer,
            mediaFile: MediaFile,
            notificationTitle: String?,
            notificationSubtitle: String?,
            videoPlayer: VideoPlayerView
        ) {
            if (player != exoPlayer) {
                exoPlayer?.pause()
                audioPlayerView?.exoProgress?.setLocalCacheBufferedPosition(0)
                videoPlayerView?.exoProgress?.setLocalCacheBufferedPosition(0)
                videoPlayerView = videoPlayer
                exoPlayer?.seekTo(0)
                videoPlayer.exoProgress.setLocalCacheBufferedPosition(100)
                exoPlayer = player
            }
            this@IGMediaService.notificationTitle = notificationTitle
            this@IGMediaService.notificationSubtitle = notificationSubtitle
            this@IGMediaService.mediaFile = mediaFile
            displayNotification(true)
        }

        fun changeControlButton(start: Boolean) {
            displayNotification(start)
        }

    }

    data class MediaFile(
        val isAudio: Boolean,
        val fileId: Int
    )

    private fun displayNotification(startForeground: Boolean) {
        requestAudioFocus()
        val isPlaying = exoPlayer?.playWhenReady == true

        val audioTitle = notificationTitle.let { if (it.isNullOrBlank()) "Unknown song" else it }
        val audioAuthor =
            notificationSubtitle.let { if (it.isNullOrBlank()) "Unknown author" else it }

        val playPauseButtonId = if (isPlaying) R.drawable.ic_media_notification_pause
        else R.drawable.ic_media_notification_play

        val intent = Intent(this, IGMediaService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val pendingIntent = PendingIntent.getService(this, 0, intent, 0)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, MainActivity.MEDIA_CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.exo_notification_small_icon)
            .addAction(playPauseButtonId, if (isPlaying) "Pause" else "Play", pendingIntent) // #1
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0)
            )
            .setNotificationSilent()
            .setContentTitle(audioTitle)
            .setContentText(audioAuthor)

        if (Build.VERSION.SDK_INT > 26) {
            notificationBuilder.setChannelId(MainActivity.MEDIA_CHANNEL_ID)
        }
        val notification = notificationBuilder.build()
        if (startForeground) startForeground(NOTIFICATION_ID, notification)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                    setAudioAttributes(AudioAttributes.Builder().run {
                        setUsage(AudioAttributes.USAGE_GAME)
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        build()
                    })
                    setAcceptsDelayedFocusGain(true)
                    setOnAudioFocusChangeListener(afChangeListener)
                    build()
                })
        } else {
            audioManager.requestAudioFocus(
                afChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer = null
        audioPlayerView = null
        videoPlayerView = null
    }
}
