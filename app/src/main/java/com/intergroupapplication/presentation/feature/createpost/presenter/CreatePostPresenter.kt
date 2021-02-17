package com.intergroupapplication.presentation.feature.createpost.presenter

import com.intergroupapplication.domain.entity.AudiosEntity
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostView

import moxy.InjectViewState
import com.intergroupapplication.domain.entity.CreateGroupPostEntity
import com.intergroupapplication.domain.entity.FilesEntity
import com.intergroupapplication.domain.exception.CanNotUploadAudio
import com.intergroupapplication.domain.exception.CanNotUploadPhoto
import com.intergroupapplication.domain.exception.CanNotUploadVideo
import com.intergroupapplication.domain.gateway.GroupPostGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.exstension.handleLoading
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


import javax.inject.Inject

@InjectViewState
class CreatePostPresenter @Inject constructor(private val groupPostGateway: GroupPostGateway,
                                              private val photoGateway: PhotoGateway,
                                              private val errorHandler: ErrorHandler)
    : BasePresenter<CreatePostView>() {

    private var uploadingDisposable: Disposable? = null
    private var videoDisposable = CompositeDisposable()
    var groupId: String = ""
//    private var audioDisposable: Disposable? = null

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
        compositeDisposable.add(Single.zip(photoGateway.getImageUrls(),
                photoGateway.getVideoUrls(),
                photoGateway.getAudioUrls(),
                object : Function3<List<String>, List<String>, List<String>, CreateGroupPostEntity> {
                    override fun invoke(photo: List<String>, video: List<String>, audio: List<String>): CreateGroupPostEntity =
                        CreateGroupPostEntity(postText,
                                photo.map { FilesEntity(file = it, description = null, title = null) },
                                audio.map { AudiosEntity(it, null, null, null,null) },
                                video.map { FilesEntity(file = it,  description = null, title = null) },
                        false,
                                null)
                }
        )
                .subscribeOn(Schedulers.io())
                .flatMap { groupPostGateway.createPost(it, groupId) }
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({ viewState.postCreateSuccessfully(it) }, { errorHandler.handle(it) }))
    }

    fun attachFromCamera() {
        //stopImageUploading()
        videoDisposable.add(photoGateway.loadFromCamera()
                .subscribeOn(Schedulers.io())
                .filter { it.isNotEmpty() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                     loadImage(it)
                }, { errorHandler.handle(CanNotUploadPhoto())}))
    }

    fun attachFromGallery() {
        //stopImageUploading()
        videoDisposable.add(photoGateway.loadImagesFromGallery()
                .subscribeOn(Schedulers.io())
                .filter { it.isNotEmpty() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ images ->
                    images.forEach { loadImage(it) }
                }, { errorHandler.handle(CanNotUploadPhoto())}))
    }

    fun attachVideo() {
        videoDisposable.add(photoGateway.loadVideo()
                .subscribeOn(Schedulers.io())
                .filter { it.isNotEmpty() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ videos ->
                    videos.forEach { loadVideo(it) }
                }, { errorHandler.handle(CanNotUploadVideo())}))
    }

    fun attachAudio() {
        videoDisposable.add(photoGateway.loadAudio()
                .subscribeOn(Schedulers.io())
                .filter { it.isNotEmpty() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ audios ->
                    audios.forEach { loadAudio(it) }
                }, { errorHandler.handle(CanNotUploadAudio())}))
    }

    fun loadVideo(file: String) {
        var progress = 0f
        videoDisposable.add(photoGateway.uploadVideoToAws(file, groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showImageUploadingStarted(file) }
                .subscribe( {
                    progress = it
                    viewState.showImageUploadingProgress(it, file)
                }, {
                    errorHandler.handle(CanNotUploadPhoto())
                    viewState.showImageUploadingError(file)
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded(file) }))

    }

    private fun loadAudio(file: String) {
        var progress = 0f
        videoDisposable.add(photoGateway.uploadAudioToAws(file, groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showImageUploadingStarted(file) }
                .subscribe( {
                    progress = it
                    viewState.showImageUploadingProgress(it, file)
                }, {
                    errorHandler.handle(CanNotUploadPhoto())
                    viewState.showImageUploadingError(file)
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded(file) }))

    }

    fun loadImage(file: String) {
        var progress = 0f
        videoDisposable.add(photoGateway.uploadImageToAws(file, groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showImageUploadingStarted(file) }
                .subscribe( {
                    progress = it
                    viewState.showImageUploadingProgress(it, file)
                }, {
                    errorHandler.handle(CanNotUploadPhoto())
                    viewState.showImageUploadingError(file)
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded(file) }))

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

    fun removeContent(path: String) {
        photoGateway.removeContent(path)
    }

    override fun onDestroy() {
        super.onDestroy()
        uploadingDisposable?.dispose()
        videoDisposable.dispose()
    }

}
