package com.intergroupapplication.data.model

import com.google.android.exoplayer2.SimpleExoPlayer
import com.intergroupapplication.domain.entity.AudioEntity

data class AudioWithPlayer(val audio:AudioEntity, val player:SimpleExoPlayer)
