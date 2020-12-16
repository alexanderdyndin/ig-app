package com.intergroupapplication.presentation.feature.createpost.presenter

import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostView

import moxy.InjectViewState
import com.intergroupapplication.domain.entity.CreateGroupPostEntity
import com.intergroupapplication.domain.gateway.GroupPostGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.exstension.handleLoading
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


import javax.inject.Inject

@InjectViewState
class CreatePostPresenter @Inject constructor(private val groupPostGateway: GroupPostGateway,
                                              private val photoGateway: PhotoGateway,
                                              private val errorHandler: ErrorHandler,
                                              private val imageUploadingDelegate: ImageUploader)
    : BasePresenter<CreatePostView>() {

    private var uploadingDisposable: Disposable? = null

    fun createPost(createGroupPostEntity: CreateGroupPostEntity,
                   groupId: String) {
        compositeDisposable.add(groupPostGateway.createPost(createGroupPostEntity, groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    viewState.postCreateSuccessfully(it)
                }, { errorHandler.handle(it) }))
    }

    fun createPostWithImage(postText: String, groupId: String) {
        compositeDisposable.add(photoGateway.getLastPhotoUrl()
                .subscribeOn(Schedulers.io())
                .flatMap { groupPostGateway.createPost(CreateGroupPostEntity(postText, it), groupId) }
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({ viewState.postCreateSuccessfully(it) }, { errorHandler.handle(it) }))
    }

    fun attachFromGallery() {
        stopImageUploading()
        uploadingDisposable = imageUploadingDelegate.uploadFromGallery(viewState, errorHandler)
    }

    fun attachFromCamera() {
        stopImageUploading()
        uploadingDisposable = imageUploadingDelegate.uploadFromCamera(viewState, errorHandler)
    }

    fun retryLoadImage() {
        stopImageUploading()
        uploadingDisposable = photoGateway.uploadToAws()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ viewState.showImageUploadingProgress(it) }, {
                    errorHandler.handle(it)
                    viewState.showImageUploadingError()
                },
                        { viewState.showImageUploaded() })
    }

    fun stopImageUploading() {
        uploadingDisposable?.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        uploadingDisposable?.dispose()
    }

}
