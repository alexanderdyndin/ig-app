package com.intergroupapplication.presentation.base

import java.lang.Exception

sealed class ImageUploadingState {
    class ImageUploadingStarted(var path: String = ""): ImageUploadingState()
    class ImageUploaded(var path: String = ""): ImageUploadingState()
    class ImageUploadingProgress(var progress: Float, var path: String = ""): ImageUploadingState() {
        override fun toString(): String {
            return "ImageUploadingProgress(progress=$progress, path='$path')"
        }
    }

    class ImageUploadingError(var path: String = "", var exception: Throwable? = null): ImageUploadingState()
}
