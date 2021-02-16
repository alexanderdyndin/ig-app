package com.intergroupapplication.presentation.feature.createpost.presenter

import com.intergroupapplication.domain.entity.AudiosEntity
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostView

import moxy.InjectViewState
import com.intergroupapplication.domain.entity.CreateGroupPostEntity
import com.intergroupapplication.domain.entity.FilesEntity
import com.intergroupapplication.domain.exception.CanNotUploadPhoto
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
                                              private val errorHandler: ErrorHandler,
                                              private val imageUploadingDelegate: ImageUploader)
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
        compositeDisposable.add(Single.zip(photoGateway.getLastPhotoUrl(),
                photoGateway.getVideoUrls(),
                photoGateway.getAudioUrls(),
                object : Function3<String, List<String>, List<String>, CreateGroupPostEntity> {
                    override fun invoke(photo: String, video: List<String>, audio: List<String>): CreateGroupPostEntity =
                        CreateGroupPostEntity(postText,
                                if (photo.isEmpty()) null else photo,
                                if (photo.isNotEmpty()) List(1) { FilesEntity(file = photo, post = groupId.toInt(), description = null, title = null) } else emptyList(),
                                audio.map { AudiosEntity(it, null, null, null,null, groupId.toInt()) },
                                video.map { FilesEntity(file = it, post = groupId.toInt(), description = null, title = null) })
                }
        )
                .subscribeOn(Schedulers.io())
                .flatMap { groupPostGateway.createPost(it, groupId) }
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

    fun attachAudio() {
        var progress = 0f
        compositeDisposable.add(photoGateway.loadAudio()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .filter { it.isNotEmpty() }
//                .doOnNext { viewState.showImageUploadingStarted(it.to) }
                .observeOn(Schedulers.io())
//                .flatMap { photoGateway.uploadToAws() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
//                    progress = it
//                    viewState.showImageUploadingProgress(it)
                }, {
                    errorHandler?.handle(CanNotUploadPhoto())
//                    viewState.showImageUploadingError()
                }, /*{ if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded() }*/))
    }

    fun attachVideo() {
        compositeDisposable.add(photoGateway.loadVideo()
                .subscribeOn(Schedulers.io())
                .filter { it.isNotEmpty() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ videos ->
                    videos.forEach { loadVideo(it) }
                }, { errorHandler.handle(CanNotUploadPhoto())}))
    }

    private fun loadVideo(file: String) {
        var progress = 0f
        compositeDisposable.add(photoGateway.uploadVideoToAws(file, groupId)
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

    override fun onDestroy() {
        super.onDestroy()
        uploadingDisposable?.dispose()
    }

}
