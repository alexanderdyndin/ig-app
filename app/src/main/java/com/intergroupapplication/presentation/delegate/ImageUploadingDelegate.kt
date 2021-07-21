package com.intergroupapplication.presentation.delegate

import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.domain.entity.MediaType
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
import timber.log.Timber
import javax.inject.Inject

class ImageUploadingDelegate @Inject constructor(private val photoGateway: PhotoGateway) : ImageUploader {

    companion object {
        const val FULL_UPLOADED_PROGRESS = 100F
    }

    override fun uploadFromCamera(view: ImageUploadingView,
                                  errorHandler: ErrorHandler?, groupId: String?,
                                  upload:(String?)->Observable<Float>): Disposable {
        var progress = 0f
        var path = ""
        return photoGateway.loadFromCamera()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter { !it.isEmpty() }
                .doOnNext {
                    view.showImageUploadingStarted(ChooseMedia(it,
                    name = it.substringAfterLast("/"),
                    type = MediaType.IMAGE))
                    path = it
                }
                .observeOn(Schedulers.io())
                .flatMap { upload(groupId) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    progress = it
                    view.showImageUploadingProgress(it, ChooseMedia(path,
                        name = path.substringAfterLast("/"),
                        type = MediaType.IMAGE))
                }, {
                    errorHandler?.handle(CanNotUploadPhoto())
                    view.showImageUploadingError(ChooseMedia(path,
                        name = path.substringAfterLast("/"),
                        type = MediaType.IMAGE))
                }, { if (progress == FULL_UPLOADED_PROGRESS)
                    view.showImageUploaded(ChooseMedia(path,
                        name = path.substringAfterLast("/"),
                        type = MediaType.IMAGE)) })
    }

    override fun uploadFromGallery(view: ImageUploadingView, errorHandler: ErrorHandler?, groupId: String?,upload:(String?)->Observable<Float>): Disposable {
        var progress = 0f
        var path = ""
        return photoGateway.loadFromGallery()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter { !it.isEmpty() }
                .doOnNext {
                    view.showImageUploadingStarted(ChooseMedia(it,
                        name = it.substringAfterLast("/"),
                        type = MediaType.IMAGE))
                    path = it
                }
                .observeOn(Schedulers.io())
                .flatMap { upload(groupId) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    progress = it
                    view.showImageUploadingProgress(it, ChooseMedia(path,
                        name = path.substringAfterLast("/"),
                        type = MediaType.IMAGE))
                }, {
                    errorHandler?.handle(CanNotUploadPhoto())
                    view.showImageUploadingError(ChooseMedia(path,
                        name = path.substringAfterLast("/"),
                        type = MediaType.IMAGE))
                }, { if (progress >= FULL_UPLOADED_PROGRESS) view.showImageUploaded(ChooseMedia(path,
                    name = path.substringAfterLast("/"),
                        type = MediaType.IMAGE)) })
    }

    override fun getLastPhotoUploadedUrl(): Single<String> =
            photoGateway.getLastPhotoUrl()

}
