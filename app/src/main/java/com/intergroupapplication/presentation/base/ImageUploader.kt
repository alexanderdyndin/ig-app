package com.intergroupapplication.presentation.base

import com.workable.errorhandler.ErrorHandler
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable

interface ImageUploader {
    fun uploadFromCamera(
        view: ImageUploadingView,
        errorHandler: ErrorHandler?,
        groupId: String? = null,
        upload: (String?) -> Observable<Float>
    ): Disposable

    fun uploadFromGallery(
        view: ImageUploadingView,
        errorHandler: ErrorHandler?,
        groupId: String? = null,
        upload: (String?) -> Observable<Float>
    ): Disposable

    fun getLastPhotoUploadedUrl(): Single<String>
}
