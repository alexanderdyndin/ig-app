package com.intergroupapplication.data.model

data class AudioInAddFileModel(
    val url: String,
    val name: String,
    val duration: String,
    val author: String,
    override var isChoose: Boolean
) : ChooseClass(isChoose)
