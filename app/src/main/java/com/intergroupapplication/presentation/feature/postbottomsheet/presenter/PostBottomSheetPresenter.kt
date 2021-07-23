package com.intergroupapplication.presentation.feature.postbottomsheet.presenter

import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.MediaType
import com.intergroupapplication.domain.exception.CanNotUploadAudio
import com.intergroupapplication.domain.exception.CanNotUploadPhoto
import com.intergroupapplication.domain.exception.CanNotUploadVideo
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.postbottomsheet.view.PostBottomSheetView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PostBottomSheetPresenter @Inject constructor(private val photoGateway: PhotoGateway,
                                                   private val errorHandler: ErrorHandler,
                                                   private val appApi: AppApi)
: BasePresenter<PostBottomSheetView>() {
    private var uploadingDisposable: Disposable? = null
    private var mediaDisposable = CompositeDisposable()
    var groupId: String? = null
    private val processes: MutableMap<String, Disposable> = mutableMapOf()

    fun attachMedia(loadMedia:(chooseMedia:ChooseMedia)->Unit, chooseMedias:Set<ChooseMedia>) {
        mediaDisposable.add(Observable.fromIterable(chooseMedias)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    loadMedia(it)
                }, {
                    it.printStackTrace()
                    errorHandler.handle(CanNotUploadPhoto())
                }))
    }

    fun attachFromCamera() {
        mediaDisposable.add(photoGateway.loadFromCamera()
                .filter { it.isNotEmpty() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val chooseMedia = ChooseMedia(it,
                        name = it.substringAfterLast("/"),
                        type = MediaType.IMAGE)
                    loadImage(chooseMedia)
                }, {
                    errorHandler.handle(CanNotUploadPhoto())}))
    }

    fun loadVideo(chooseMedia: ChooseMedia) {
        var progress = 0f
        processes[chooseMedia.url] = photoGateway.uploadImage(chooseMedia.urlPreview,groupId,
                appApi::uploadCommentsMedia)
                .flatMap {
                    photoGateway.uploadVideoToAws(ChooseMedia(chooseMedia.url,urlPreview = it,
                            name = chooseMedia.name,
                            duration = chooseMedia.duration,
                            type = chooseMedia.type), groupId, appApi::uploadCommentsMedia)
                }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showImageUploadingStarted(chooseMedia) }
                .subscribe( {
                    progress = it
                    viewState.showImageUploadingProgress(it, chooseMedia)
                }, {
                    it.printStackTrace()
                    errorHandler.handle(CanNotUploadVideo())
                    viewState.showImageUploadingError(chooseMedia)
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS)
                    viewState.showImageUploaded(chooseMedia) })
        mediaDisposable.add(processes[chooseMedia.url]!!)
    }

    fun loadAudio(chooseMedia: ChooseMedia) {
        var progress = 0f
        processes[chooseMedia.url] = photoGateway.uploadAudioToAws(chooseMedia,
                groupId, appApi::uploadCommentsMedia)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showImageUploadingStarted(chooseMedia) }
                .subscribe( {
                    progress = it
                    viewState.showImageUploadingProgress(it, chooseMedia)
                }, {
                    it.printStackTrace()
                    errorHandler.handle(CanNotUploadAudio())
                    viewState.showImageUploadingError(chooseMedia)
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS)
                    viewState.showImageUploaded(chooseMedia) })
        mediaDisposable.add(processes[chooseMedia.url]!!)

    }

    fun loadImage(chooseMedia:ChooseMedia) {
        var progress = 0f
        processes[chooseMedia.url] = photoGateway.uploadImageToAws(chooseMedia.url, groupId,
                appApi::uploadCommentsMedia)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    viewState.showImageUploadingStarted(chooseMedia) }
                .subscribe( {
                    progress = it
                    viewState.showImageUploadingProgress(it, chooseMedia)
                }, {
                    it.printStackTrace()
                    errorHandler.handle(CanNotUploadPhoto())
                    viewState.showImageUploadingError(chooseMedia)
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS)
                    viewState.showImageUploaded(chooseMedia) })
        mediaDisposable.add(processes[chooseMedia.url]!!)
    }

    fun retryLoading(chooseMedia: ChooseMedia) {
        var progress = 0f
        when (chooseMedia.type) {
           MediaType.AUDIO -> {
                processes[chooseMedia.url] = photoGateway.uploadAudioToAws(chooseMedia,
                        groupId, appApi::uploadPhoto)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( {
                            progress = it
                            viewState.showImageUploadingProgress(it, chooseMedia)
                        }, {
                            errorHandler.handle(CanNotUploadPhoto())
                            viewState.showImageUploadingError(chooseMedia)
                        }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS)
                            viewState.showImageUploaded(chooseMedia) })
            }
            MediaType.VIDEO -> {
                processes[chooseMedia.url] = photoGateway.uploadImage(chooseMedia.urlPreview,groupId,
                    appApi::uploadPhoto)
                        .flatMap {
                            photoGateway.uploadVideoToAws(ChooseMedia(chooseMedia.url,urlPreview = it,
                                    name = chooseMedia.name,
                                    duration = chooseMedia.duration, type = MediaType.VIDEO),
                                    groupId, appApi::uploadPhoto)
                        }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe { viewState.showImageUploadingStarted(chooseMedia) }
                        .subscribe( {
                            progress = it
                            viewState.showImageUploadingProgress(it, chooseMedia)
                        }, {
                            it.printStackTrace()
                            errorHandler.handle(CanNotUploadVideo())
                            viewState.showImageUploadingError(chooseMedia)
                        }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS)
                            viewState.showImageUploaded(chooseMedia) })
            }
            MediaType.IMAGE -> {
                processes[chooseMedia.url] = photoGateway.uploadImageToAws(chooseMedia.url, groupId,
                    appApi::uploadPhoto)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( {
                            progress = it
                            viewState.showImageUploadingProgress(it, chooseMedia)
                        }, {
                            errorHandler.handle(CanNotUploadPhoto())
                            viewState.showImageUploadingError(chooseMedia)
                        }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS)
                            viewState.showImageUploaded(chooseMedia) })
            }
        }
        processes[chooseMedia.url]?.let {
            mediaDisposable.add(it)
        }
    }

    fun removeContent(chooseMedia: ChooseMedia) {
        photoGateway.removeContent(chooseMedia)
    }

    fun cancelUploading(chooseMedia: ChooseMedia) {
        processes[chooseMedia.url]?.dispose()
        removeContent(chooseMedia)
        processes.remove(chooseMedia.url)
    }

    fun getPhotosUrl() = photoGateway.getImageUrls()
    fun getVideosUrl() = photoGateway.getVideoUrls()
    fun getAudiosUrl() = photoGateway.getAudioUrls()

    override fun onDestroy() {
        super.onDestroy()
        uploadingDisposable?.dispose()
        mediaDisposable.dispose()
    }

    fun addAudioInAudiosUrl(audios: List<AudioEntity>) {
        photoGateway.setAudioUrls(audios.map { audioEntity ->
            return@map ChooseMedia(
                "/groups/0/comments/${audioEntity.file.substringAfterLast("/")}",
                name = audioEntity.song, author = audioEntity.artist,
                duration = audioEntity.duration,
                type = MediaType.AUDIO
            )
        })
    }

    fun addVideoInVideosUrl(videos: List<FileEntity>) {
        photoGateway.setVideoUrls(videos.map { videoEntity ->
            return@map ChooseMedia(
                "/groups/0/comments/${videoEntity.file.substringAfterLast("/")}",
                "/groups/0/comments/${videoEntity.preview.substringAfterLast("/")}",
                name = videoEntity.title,duration = videoEntity.duration,
                type = MediaType.VIDEO
            )
        })
    }

    fun addImagesInPhotosUrl(images: List<FileEntity>) {
        photoGateway.setImageUrls(images.map {
            val url = "/groups/0/comments/${it.file.substringAfterLast("/")}"
            return@map ChooseMedia(url = url, name = it.title, type = MediaType.IMAGE)
        })
    }
}