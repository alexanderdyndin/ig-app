package com.intergroupapplication.presentation.feature.mediaPlayer

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
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


class IGMediaService : MediaBrowserServiceCompat() {

    companion object {
        const val NOTIFICATION_ID = 1
        const val MEDIA_URL = "mediaUrl"

        const val PLAY_PAUSE_ACTION = "actionPlayPause"
        const val ACTION_PLAY = 1
        const val ACTION_PAUSE = 0
    }


    private lateinit var exoPlayer: SimpleExoPlayer

    /**
     * Will be called by our activity to get information about exo player.
     */
    override fun onBind(intent: Intent?): IBinder {
        intent?.let {
            exoPlayer.playWhenReady = false  //Tell exoplayer to start as soon as it's content is loaded.
//            loadExampleMedia(intent.getStringExtra(MEDIA_URL))
//            displayNotification()
        }
        return ServiceBinder()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return null
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {

    }


    override fun onCreate() {
        super.onCreate()
        val trackSelection = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(this, trackSelection)
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val action = it.getIntExtra(PLAY_PAUSE_ACTION, -1)
            when (action) {
                ACTION_PAUSE -> exoPlayer.playWhenReady = exoPlayer.playWhenReady.let { return@let !it }
            }
            displayNotification()
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
    }

    /**
     * When called will load into exo player our sample playback video.
     */
    private fun loadExampleMedia(urlLink: String) {
        val mediaItem = MediaItem.fromUri(urlLink)
        val source = ExtractorMediaSource.Factory(DefaultDataSourceFactory(this))
                .setExtractorsFactory(DefaultExtractorsFactory()).createMediaSource(mediaItem)
        exoPlayer.setMediaSource(source)
        exoPlayer.prepare()
    }

    private fun displayNotification() {
        val isPlaying = exoPlayer.playWhenReady
        //Lets create our remote view.
//        val remoteView = RemoteViews(packageName, R.layout.notification_media_player)

        val playPauseButtonId = if (isPlaying) R.drawable.ic_media_notification_pause else R.drawable.ic_media_notification_play

        val intent = Intent(this, IGMediaService::class.java).apply {
            putExtra(PLAY_PAUSE_ACTION, if (isPlaying) ACTION_PAUSE else ACTION_PLAY)
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
                        .setShowActionsInCompactView(0))
                .setContentTitle("Wonderful music")
                .setContentText("My Awesome Band")

        //Check for version and create a channel if needed.
        if (Build.VERSION.SDK_INT > 26) {
            notificationBuilder.setChannelId(MainActivity.MEDIA_CHANNEL_ID)
        }
        val notification = notificationBuilder.build()
        startForeground(NOTIFICATION_ID, notification)
        manager.notify(NOTIFICATION_ID, notification)
    }
}