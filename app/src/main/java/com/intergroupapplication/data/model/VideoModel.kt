package com.intergroupapplication.data.model

data class VideoModel(val url:String, val duration:String, override var isChoose:Boolean)
    :ChooseClass(isChoose)
