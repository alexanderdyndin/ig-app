package com.intergroupapplication.presentation.base

interface ImageUploadingView {
    fun showImageUploadingStarted(path: String)
    fun showImageUploaded()
    fun showImageUploadingProgress(progress: Float)
    fun showImageUploadingError()
}
