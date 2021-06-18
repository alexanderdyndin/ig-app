package com.intergroupapplication.presentation.customview

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.presentation.exstension.getActivity
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.intergroupapplication.presentation.feature.mediaPlayer.AudioPlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioViewInList @JvmOverloads constructor(context: Context,
                        attrs: AttributeSet? = null, defStyleAttr: Int = 0):
    LinearLayout(context, attrs, defStyleAttr) {

    init {
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL
    }

    private val container = LinearLayout(context, attrs, defStyleAttr)

    fun setAudio(audio: AudioEntity) {
        container.run {
            orientation = VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        createAudio(audio)
    }


    private fun createAudio(audioEntity: AudioEntity) {
        container.removeAllViews()
        this.removeAllViews()
        val activity = this.getActivity()
        if (activity is MainActivity) {
            CoroutineScope(Dispatchers.Main).launch {
                val bindService = activity.bindMediaService()
                bindService?.let {
                        val playerView = AudioPlayerView(context)
                        val player = it.getPlayerByAudio(audioEntity, context, playerView)
                        val trackName = if (audioEntity.artist == "") audioEntity.song
                                        else "${audioEntity.artist} - ${audioEntity.song}"
                        playerView.trackName = trackName
                        playerView.trackOwner = "Загрузил (ID:${audioEntity.owner})"
                        playerView.durationTrack.text = if (audioEntity.duration != "")
                                                        audioEntity.duration else "00:00"
                        playerView.exoPlayer.player = player
                        container.addView(playerView)
                }
            }
        } else throw Exception("Activity is not MainActivity")
        this.addView(container)
    }

}