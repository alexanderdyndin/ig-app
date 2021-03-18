package com.intergroupapplication.presentation.feature.mediaPlayer

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.browse.MediaBrowser
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.service.media.MediaBrowserService
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.intergroupapplication.R
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity


class IGMediaService : MediaBrowserService() {

    companion object {
        const val NOTIFICATION_ID = 1
        const val MEDIA_URL = "mediaUrl"

        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
    }


    private var exoPlayer: SimpleExoPlayer? = null
    private var mediaFile: MediaFile? = null
    private var notificationTitle: String? = null
    private var notificationSubtitle: String? = null

    /**
     * Will be called by our activity to get information about exo player.
     */
    override fun onBind(intent: Intent?): IBinder {
            exoPlayer?.playWhenReady = false  //Tell exoplayer to start as soon as it's content is loaded.
//            loadExampleMedia(intent.getStringExtra(MEDIA_URL))
//            displayNotification()
        return ServiceBinder()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return null
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowser.MediaItem>>) {

    }

    private fun stop() {
//        exoPlayer?.release()
        stopForeground(true)
    }

    override fun onCreate() {
        super.onCreate()
        val trackSelection = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(this, trackSelection)
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
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
        fun setPlayer(player: SimpleExoPlayer, mediaFile: MediaFile, notificationTitle: String?, notificationSubtitle: String?) {
            if (player != exoPlayer){
                exoPlayer?.pause()
                exoPlayer = player
            }
            this@IGMediaService.notificationTitle = notificationTitle
            this@IGMediaService.notificationSubtitle = notificationSubtitle
            this@IGMediaService.mediaFile = mediaFile
            displayNotification(true)
        }

        fun loadMedia(mediaUrl: String) {
            loadExampleMedia(mediaUrl)
            displayNotification(true)
        }
    }

    data class MediaFile(
            val isAudio: Boolean,
            val fileId: Int
    )

    /**
     * When called will load into exo player our sample playback video.
     */
    private fun loadExampleMedia(urlLink: String) {
        val mediaItem = MediaItem.fromUri(urlLink)
        val source = ExtractorMediaSource.Factory(DefaultDataSourceFactory(this))
                .setExtractorsFactory(DefaultExtractorsFactory()).createMediaSource(mediaItem)
        exoPlayer?.setMediaSource(source)
        exoPlayer?.playWhenReady = true
//        exoPlayer?.prepare()
    }

    private fun displayNotification(startForeground: Boolean) {
        val isPlaying = exoPlayer?.playWhenReady == true
        //Lets create our remote view.
//        val remoteView = RemoteViews(packageName, R.layout.notification_media_player)

        val audioTitle = notificationTitle.let { if (it.isNullOrBlank()) "Unknown song" else it}
        val audioAuthor = notificationSubtitle.let { if (it.isNullOrBlank()) "Unknown author" else it}

        val playPauseButtonId = if (isPlaying) R.drawable.ic_media_notification_pause else R.drawable.ic_media_notification_play

        val cancel: Intent = Intent(this, IGMediaService::class.java)
        cancel.action = ACTION_STOP_FOREGROUND_SERVICE
        val cancelUploadIntent = PendingIntent.getService(this, 0, cancel, PendingIntent.FLAG_CANCEL_CURRENT)

        val intent = Intent(this, IGMediaService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val pendingIntent = PendingIntent.getService(this, 0, intent, 0)
//        remoteView.setOnClickPendingIntent(R.id.playPause, intent)

        //Now for showing through the notification manager.
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val notificationBuilder = NotificationCompat.Builder(this, MainActivity.MEDIA_CHANNEL_ID)
//        notificationBuilder.setCustomBigContentView(remoteView)
//        notificationBuilder.setSmallIcon(android.R.drawable.sym_def_app_icon)
        val notificationBuilder = NotificationCompat.Builder(this, MainActivity.MEDIA_CHANNEL_ID)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.exo_notification_small_icon)
                // Add media control buttons that invoke intents in your media service
//                .addAction(R.drawable.ic_media_notification_previous_track, "Previous", prevPendingIntent) // #0
                .addAction(playPauseButtonId, if (isPlaying) "Pause" else "Play", pendingIntent) // #1
//                .addAction(R.drawable.ic_media_notification_next_track, "Next", nextPendingIntent) // #2
                // Apply the media style template
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0)
                )
                .setNotificationSilent()
                .setContentTitle(audioTitle)
                .setContentText(audioAuthor)

        //Check for version and create a channel if needed.
        if (Build.VERSION.SDK_INT > 26) {
            notificationBuilder.setChannelId(MainActivity.MEDIA_CHANNEL_ID)
        }
        val notification = notificationBuilder.build()
        if(startForeground) startForeground(NOTIFICATION_ID, notification)
        manager.notify(NOTIFICATION_ID, notification)
    }
}