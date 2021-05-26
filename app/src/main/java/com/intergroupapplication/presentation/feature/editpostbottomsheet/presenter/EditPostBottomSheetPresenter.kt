package com.intergroupapplication.presentation.feature.editpostbottomsheet.presenter

import android.view.View
import android.webkit.MimeTypeMap
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.exception.CanNotUploadAudio
import com.intergroupapplication.domain.exception.CanNotUploadPhoto
import com.intergroupapplication.domain.exception.CanNotUploadVideo
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.addChooseMedia
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.chooseMedias
import com.intergroupapplication.presentation.feature.editpostbottomsheet.view.EditPostBottomSheetView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class EditPostBottomSheetPresenter @Inject constructor(private val photoGateway: PhotoGateway,
                                                       private val errorHandler: ErrorHandler,
                                                       private val appApi: AppApi)
: BasePresenter<EditPostBottomSheetView>() {
    private var uploadingDisposable: Disposable? = null
    private var mediaDisposable = CompositeDisposable()
    var groupId: String? = null
    private val processes: MutableMap<String, Disposable> = mutableMapOf()

    fun attachMedia(mediasObservable: Observable<ChooseMedia>,
                    loadMedia:(chooseMedia:ChooseMedia)->Unit,loadingView:Map<String, View?>) {
        mediaDisposable.add(mediasObservable
                .filter { !loadingView.containsKey(it.url) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    loadMedia(it)
                }, {
                    it.printStackTrace()
                    errorHandler.handle(CanNotUploadPhoto())}))
    }

    fun attachFromCamera() {
        mediaDisposable.add(photoGateway.loadFromCamera()
                .filter { it.isNotEmpty() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val chooseMedia = ChooseMedia(it)
                    chooseMedias.addChooseMedia(chooseMedia)
                    loadImage(chooseMedia)
                }, {
                    errorHandler.handle(CanNotUploadPhoto())}))
    }

    fun loadVideo(chooseMedia: ChooseMedia) {
        var progress = 0f
        processes[chooseMedia.url] = photoGateway.uploadImage(chooseMedia.urlPreview,groupId,appApi::uploadCommentsMedia)
                .flatMap {
                    photoGateway.uploadVideoToAws(ChooseMedia(chooseMedia.url,urlPreview = it,
                            duration = chooseMedia.duration), groupId, appApi::uploadCommentsMedia)
                }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showImageUploadingStarted(chooseMedia) }
                .subscribe( {
                    progress = it
                    viewState.showImageUploadingProgress(it, chooseMedia.url)
                }, {
                    it.printStackTrace()
                    errorHandler.handle(CanNotUploadVideo())
                    viewState.showImageUploadingError(chooseMedia.url)
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS)
                    viewState.showImageUploaded(chooseMedia.url) })
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
                    viewState.showImageUploadingProgress(it, chooseMedia.url)
                }, {
                    it.printStackTrace()
                    errorHandler.handle(CanNotUploadAudio())
                    viewState.showImageUploadingError(chooseMedia.url)
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS)
                    viewState.showImageUploaded(chooseMedia.url) })
        mediaDisposable.add(processes[chooseMedia.url]!!)

    }

    fun loadImage(chooseMedia:ChooseMedia) {
        var progress = 0f
        processes[chooseMedia.url] = photoGateway.uploadImageToAws(chooseMedia.url, groupId, appApi::uploadCommentsMedia)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    viewState.showImageUploadingStarted(chooseMedia) }
                .subscribe( {
                    progress = it
                    viewState.showImageUploadingProgress(it, chooseMedia.url)
                }, {
                    it.printStackTrace()
                    errorHandler.handle(CanNotUploadPhoto())
                    viewState.showImageUploadingError(chooseMedia.url)
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS)
                    viewState.showImageUploaded(chooseMedia.url) })
        mediaDisposable.add(processes[chooseMedia.url]!!)
    }

    fun retryLoading(chooseMedia: ChooseMedia) {
        var progress = 0f
        val type = MimeTypeMap.getFileExtensionFromUrl(chooseMedia.url)
        when (MimeTypeMap.getSingleton().getMimeTypeFromExtension(type) ?: "") {
            in listOf("audio/mpeg", "audio/aac", "audio/wav") -> {
                processes[chooseMedia.url] = photoGateway.uploadAudioToAws(chooseMedia,
                        groupId, appApi::uploadPhoto)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( {
                            progress = it
                            viewState.showImageUploadingProgress(it, chooseMedia.url)
                        }, {
                            errorHandler.handle(CanNotUploadPhoto())
                            viewState.showImageUploadingError(chooseMedia.url)
                        }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS)
                            viewState.showImageUploaded(chooseMedia.url) })
            }
            in listOf("video/mpeg", "video/mp4", "video/webm", "video/3gpp") -> {
                processes[chooseMedia.url] = photoGateway.uploadImage(chooseMedia.urlPreview,groupId,appApi::uploadCommentsMedia)
                        .flatMap {
                            photoGateway.uploadVideoToAws(ChooseMedia(chooseMedia.url,urlPreview = it,
                                    duration = chooseMedia.duration), groupId, appApi::uploadCommentsMedia)
                        }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe { viewState.showImageUploadingStarted(chooseMedia) }
                        .subscribe( {
                            progress = it
                            viewState.showImageUploadingProgress(it, chooseMedia.url)
                        }, {
                            it.printStackTrace()
                            errorHandler.handle(CanNotUploadVideo())
                            viewState.showImageUploadingError(chooseMedia.url)
                        }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS)
                            viewState.showImageUploaded(chooseMedia.url) })
            }
            else -> {
                processes[chooseMedia.url] = photoGateway.uploadImageToAws(chooseMedia.url, groupId, appApi::uploadPhoto)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( {
                            progress = it
                            viewState.showImageUploadingProgress(it, chooseMedia.url)
                        }, {
                            errorHandler.handle(CanNotUploadPhoto())
                            viewState.showImageUploadingError(chooseMedia.url)
                        }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS)
                            viewState.showImageUploaded(chooseMedia.url) })
            }
        }
        processes[chooseMedia.url]?.let {
            mediaDisposable.add(it)
        }
    }

    fun removeContent(path:String) {
        photoGateway.removeContent(path)
    }

    fun cancelUploading(path:String) {
        processes[path]?.let {
            it.dispose()
            mediaDisposable.remove(it)
        }
        removeContent(path)
        processes.remove(path)
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
            val chooseMedia = ChooseMedia("/groups/0/comments/${audioEntity.file.substringAfterLast("/")}",
                    trackName = audioEntity.song,authorMusic = audioEntity.artist)
            Timber.tag("tut_add_audio").d(chooseMedia.url)
            chooseMedias.addChooseMedia(chooseMedia)
            return@map chooseMedia
        })
    }

    fun addVideoInVideosUrl(videos: List<FileEntity>) {
        photoGateway.setVideoUrls(videos.map { videoEntity ->
            val chooseMedia = ChooseMedia("/groups/0/comments/${videoEntity.file.substringAfterLast("/")}",
            "/groups/0/comments/${videoEntity.preview.substringAfterLast("/")}")
            chooseMedias.addChooseMedia(chooseMedia)
            return@map chooseMedia
        })
    }

    fun addImagesInPhotosUrl(images: List<FileEntity>) {
        photoGateway.setImageUrls(images.map{
            val url = "/groups/0/comments/${it.file.substringAfterLast("/")}"
            chooseMedias.addChooseMedia(ChooseMedia(url))
            return@map url})
    }
}