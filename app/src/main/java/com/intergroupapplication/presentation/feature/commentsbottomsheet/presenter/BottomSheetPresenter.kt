package com.intergroupapplication.presentation.feature.commentsbottomsheet.presenter

import android.view.View
import android.webkit.MimeTypeMap
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.AudioRequestEntity
import com.intergroupapplication.domain.entity.CreateCommentEntity
import com.intergroupapplication.domain.entity.FileRequestEntity
import com.intergroupapplication.domain.exception.CanNotUploadAudio
import com.intergroupapplication.domain.exception.CanNotUploadPhoto
import com.intergroupapplication.domain.exception.CanNotUploadVideo
import com.intergroupapplication.domain.gateway.CommentGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.addChooseMedia
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.chooseMedias
import com.intergroupapplication.presentation.feature.commentsbottomsheet.view.BottomSheetView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class BottomSheetPresenter @Inject constructor(private val commentGateway: CommentGateway,
                                              private val errorHandler: ErrorHandler,
                                              private val photoGateway: PhotoGateway,
                                              private val appApi: AppApi): BasePresenter<BottomSheetView>() {

    private val commentsDisposable = CompositeDisposable()
    private var uploadingDisposable: Disposable? = null
    private var mediaDisposable = CompositeDisposable()
    var postId: String? = null
    private val processes: MutableMap<String, Disposable> = mutableMapOf()

    fun createComment(postId: String, textComment: String) {
        compositeDisposable.add(Single.zip(photoGateway.getImageUrls(),photoGateway.getAudioUrls(),
                photoGateway.getVideoUrls(),
                object : Function3<List<String>, List<ChooseMedia>, List<ChooseMedia>, CreateCommentEntity> {
                    override fun invoke(photos: List<String>, audios: List<ChooseMedia>, videos: List<ChooseMedia>)
                                : CreateCommentEntity{
                        val create =CreateCommentEntity(textComment,
                                photos.map { FileRequestEntity(file = it, description = null,
                                        title = it.substringAfter("/comments/")) },
                                audios.map { AudioRequestEntity(it.url, null,
                                        it.trackName, it.authorMusic, null) },
                                videos.map { FileRequestEntity(file = it.url, description = null, title =
                                        it.url.substringAfter("/comments/"),it.urlPreview) },
                        )
                        return create
                    }
                })
                .subscribeOn(Schedulers.io())
                .flatMap {commentEntity->
                    commentGateway.createComment(postId,commentEntity)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showCommentUploading(true) }
                .doFinally { viewState.showCommentUploading(false) }
                .subscribe({
                    viewState.commentCreated(it)
                    photoGateway.removeAllContent()
                }, {
                    errorHandler.handle(it)
                    viewState.hideSwipeLayout()
                }))
    }

    fun createAnswerToComment(answerToCommentId: String, textComment:String) {
        compositeDisposable.add(Single.zip(photoGateway.getImageUrls(),photoGateway.getAudioUrls(),
                photoGateway.getVideoUrls(),
                object : Function3<List<String>, List<ChooseMedia>, List<ChooseMedia>, CreateCommentEntity> {
                    override fun invoke(photos: List<String>, audios: List<ChooseMedia>, videos: List<ChooseMedia>): CreateCommentEntity {
                        return CreateCommentEntity(textComment,
                                photos.map { FileRequestEntity(file = it, description = null, title = it.substringAfter("/posts/")) },
                                audios.map { AudioRequestEntity(it.url, null, it.trackName, it.authorMusic, null) },
                                videos.map { FileRequestEntity(file = it.url, description = null, title = it.url.substringAfter("/posts/"),it.urlPreview) },
                        )
                    }

                })
                .subscribeOn(Schedulers.io())
                .flatMap {commentEntity-> commentGateway.createAnswerToComment(answerToCommentId,commentEntity)}
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showCommentUploading(true) }
                .doFinally { viewState.showCommentUploading(false) }
                .subscribe({
                    photoGateway.removeAllContent()
                    viewState.answerToCommentCreated(it) }, {
                    errorHandler.handle(it)
                    viewState.hideSwipeLayout()
                }))
    }

    fun attachMedia(mediasObservable: Observable<ChooseMedia>,
                    loadMedia:(chooseMedia:ChooseMedia)->Unit,
                    loadingView:Map<String, View?>) {
        mediaDisposable.add(mediasObservable
                .filter { !loadingView.containsKey(it.url) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                        loadMedia(it)
                }, {
                    it.printStackTrace()
                    //errorHandler.handle(CanNotUploadPhoto())
                }
            )
        )
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
        processes[chooseMedia.url] = photoGateway.uploadImage(chooseMedia.urlPreview,postId,appApi::uploadCommentsMedia)
                .flatMap {
                    photoGateway.uploadVideoToAws(chooseMedia.url,it, postId, appApi::uploadCommentsMedia)
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
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded(chooseMedia.url) })
        mediaDisposable.add(processes[chooseMedia.url]!!)

    }

    fun loadAudio(chooseMedia: ChooseMedia) {
        var progress = 0f
        processes[chooseMedia.url] = photoGateway.uploadAudioToAws(chooseMedia.url,chooseMedia.trackName
                ,chooseMedia.authorMusic, postId, appApi::uploadCommentsMedia)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showImageUploadingStarted(chooseMedia) }
                .subscribe( {
                    progress = it
                    viewState.showImageUploadingProgress(it, chooseMedia.url)
                }, {
                    errorHandler.handle(CanNotUploadAudio())
                    viewState.showImageUploadingError(chooseMedia.url)
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded(chooseMedia.url) })
        mediaDisposable.add(processes[chooseMedia.url]!!)

    }

    fun loadImage(chooseMedia: ChooseMedia) {
        var progress = 0f
        processes[chooseMedia.url] = photoGateway.uploadImageToAws(chooseMedia.url, postId, appApi::uploadCommentsMedia)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    viewState.showImageUploadingStarted(chooseMedia) }
                .subscribe( {
                    progress = it
                    viewState.showImageUploadingProgress(it, chooseMedia.url)
                }, {
                    errorHandler.handle(CanNotUploadPhoto())
                    viewState.showImageUploadingError(chooseMedia.url)
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded(chooseMedia.url) })
        mediaDisposable.add(processes[chooseMedia.url]!!)
    }


    fun retryLoading(chooseMedia: ChooseMedia) {
        var progress = 0f
        val type = MimeTypeMap.getFileExtensionFromUrl(chooseMedia.url)
        when (MimeTypeMap.getSingleton().getMimeTypeFromExtension(type) ?: "") {
            in listOf("audio/mpeg", "audio/aac", "audio/wav", "audio/mp3") -> {
                processes[chooseMedia.url] = photoGateway.uploadAudioToAws(chooseMedia.url,chooseMedia.trackName,
                        chooseMedia.authorMusic,postId, appApi::uploadCommentsMedia)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( {
                            progress = it
                            viewState.showImageUploadingProgress(it, chooseMedia.url)
                        }, {
                            errorHandler.handle(CanNotUploadPhoto())
                            viewState.showImageUploadingError(chooseMedia.url)
                        }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded(chooseMedia.url) })
            }
            in listOf("video/mpeg", "video/mp4", "video/webm", "video/3gpp") -> {
                processes[chooseMedia.url] = photoGateway.uploadImage(chooseMedia.urlPreview,postId,appApi::uploadCommentsMedia)
                        .flatMap {
                            photoGateway.uploadVideoToAws(chooseMedia.url,it, postId, appApi::uploadCommentsMedia)
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
                        }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded(chooseMedia.url) })
            }
            else -> {
                processes[chooseMedia.url] = photoGateway.uploadImageToAws(chooseMedia.url, postId, appApi::uploadCommentsMedia)
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


    fun stopImageUploading() {
        uploadingDisposable?.dispose()
    }

    fun removeContent(path: String) {
        photoGateway.removeContent(path)
    }

    fun cancelUploading(file: String) {
        processes[file]?.let {
            it.dispose()
            mediaDisposable.remove(it)
        }
        removeContent(file)
        processes.remove(file)
    }

    override fun onDestroy() {
        super.onDestroy()
        commentsDisposable.clear()
        uploadingDisposable?.dispose()
        mediaDisposable.dispose()
    }
}