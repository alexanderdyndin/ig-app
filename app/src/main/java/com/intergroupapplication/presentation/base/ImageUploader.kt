package com.intergroupapplication.presentation.base

import com.workable.errorhandler.ErrorHandler
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable

interface ImageUploader {
    fun uploadFromCamera(view: ImageUploadingView, errorHandler: ErrorHandler?, groupId: String? = null,upload:(String?)->Observable<Float>): Disposable
    //fun uploadFromCameraAvatarUser(view: ImageUploadingView, errorHandler: ErrorHandler?, groupId: String? = null): Disposable
    //fun uploadFromCameraAvatarGroup(view: ImageUploadingView, errorHandler: ErrorHandler?, groupId: String? = null): Disposable
    fun uploadFromGallery(view: ImageUploadingView, errorHandler: ErrorHandler?, groupId: String? = null,upload:(String?)->Observable<Float>): Disposable
   // fun uploadFromGalleryAvatarUser(view: ImageUploadingView, errorHandler: ErrorHandler?, groupId: String? = null): Disposable
    //fun uploadFromGalleryAvatarGroup(view: ImageUploadingView, errorHandler: ErrorHandler?, groupId: String? = null): Disposable
    fun getLastPhotoUploadedUrl(): Single<String>
}