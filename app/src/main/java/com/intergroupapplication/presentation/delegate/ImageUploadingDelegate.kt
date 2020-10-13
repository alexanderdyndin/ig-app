package com.intergroupapplication.presentation.delegate

import com.intergroupapplication.domain.exception.CanNotUploadPhoto
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.base.ImageUploadingView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ImageUploadingDelegate @Inject constructor(private val photoGateway: PhotoGateway) : ImageUploader {

    companion object {
        private const val FULL_UPLOADED_PROGRESS = 100F
    }

    override fun uploadFromCamera(view: ImageUploadingView,
                                  errorHandler: ErrorHandler?): Disposable {
        var progress = 0f
        return photoGateway.loadFromCamera()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter { !it.isEmpty() }
                .doOnNext { view.showImageUploadingStarted(it) }
                .observeOn(Schedulers.io())
                .flatMap { photoGateway.uploadToAws() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    progress = it
                    view.showImageUploadingProgress(it)
                }, {
                    errorHandler?.handle(CanNotUploadPhoto())
                    view.showImageUploadingError()
                }, { if (progress == FULL_UPLOADED_PROGRESS) view.showImageUploaded() })
    }

    override fun uploadFromGallery(view: ImageUploadingView, errorHandler: ErrorHandler?): Disposable {
        var progress = 0f
        return photoGateway.loadFromGallery()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter { !it.isEmpty() }
                .doOnNext { view.showImageUploadingStarted(it) }
                .observeOn(Schedulers.io())
                .flatMap { photoGateway.uploadToAws() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    progress = it
                    view.showImageUploadingProgress(it)
                }, {
                    errorHandler?.handle(CanNotUploadPhoto())
                    view.showImageUploadingError()
                }, { if (progress >= FULL_UPLOADED_PROGRESS) view.showImageUploaded() })
    }

    override fun getLastPhotoUploadedUrl(): Single<String> =
            photoGateway.getLastPhotoUrl()

}
