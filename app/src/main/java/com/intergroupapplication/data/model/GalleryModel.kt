package com.intergroupapplication.data.model

data class GalleryModel(
    val url: String,
    val date: Long,
    override var isChoose: Boolean
) : ChooseClass(isChoose)
