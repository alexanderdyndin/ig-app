package com.intergroupapplication.presentation.feature.bottomsheet.presenter

import android.view.View
import android.webkit.MimeTypeMap
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.AudioRequestEntity
import com.intergroupapplication.domain.entity.CreateCommentEntity
import com.intergroupapplication.domain.entity.FileRequestEntity
import com.intergroupapplication.domain.exception.CanNotUploadAudio
import com.intergroupapplication.domain.exception.CanNotUploadPhoto
import com.intergroupapplication.domain.exception.CanNotUploadVideo
import com.intergroupapplication.domain.gateway.CommentGateway
import com.intergroupapplication.domain.gateway.ComplaintsGateway
import com.intergroupapplication.domain.gateway.GroupPostGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.bottomsheet.adapter.chooseMedia
import com.intergroupapplication.presentation.feature.bottomsheet.view.BottomSheetView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import timber.log.Timber
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
                object : Function3<List<String>, List<String>, List<String>, CreateCommentEntity> {
                    override fun invoke(photos: List<String>, audios: List<String>, videos: List<String>): CreateCommentEntity{
                        val create =CreateCommentEntity(textComment,
                                photos.map { FileRequestEntity(file = it, description = null, title = it.substringAfter("/comments/")) },
                                audios.map { AudioRequestEntity(it, null, it.substringAfter("/comments/"), "Unknown", null) },
                                videos.map { FileRequestEntity(file = it, description = null, title = it.substringAfter("/comments/")) },
                        )
                        Timber.tag("tut_zip").d(create.toString())
                        return create
                    }
                })
                .subscribeOn(Schedulers.io())
                .flatMap {commentEntity->
                    Timber.tag("tut_flatmap").d(commentEntity.toString())
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
                    Timber.tag("tut_error").d(it)
                }))
    }

    fun createAnswerToComment(answerToCommentId: String, textComment:String) {
        compositeDisposable.add(Single.zip(photoGateway.getImageUrls(),photoGateway.getAudioUrls(),
                photoGateway.getVideoUrls(),
                object : Function3<List<String>, List<String>, List<String>, CreateCommentEntity> {
                    override fun invoke(photos: List<String>, audios: List<String>, videos: List<String>): CreateCommentEntity {
                        return CreateCommentEntity(textComment,
                                photos.map { FileRequestEntity(file = it, description = null, title = it.substringAfter("/posts/")) },
                                audios.map { AudioRequestEntity(it, null, it.substringAfter("/posts/"), "Unknown", null) },
                                videos.map { FileRequestEntity(file = it, description = null, title = it.substringAfter("/posts/")) },
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

    fun attachMedia(mediasObservable: Observable<String>, loadMedia:(path:String)->Unit, loadingView:Map<String, View?>) {
        mediaDisposable.add(mediasObservable
                .filter { !loadingView.containsKey(it) }
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
                    chooseMedia.add(it)
                    loadImage(it)
                }, {
                    errorHandler.handle(CanNotUploadPhoto())}))
    }

    fun loadVideo(file: String) {
        var progress = 0f
        processes[file] = photoGateway.uploadVideoToAws(file, postId, appApi::uploadCommentsMedia)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showImageUploadingStarted(file) }
                .subscribe( {
                    progress = it
                    viewState.showImageUploadingProgress(it, file)
                }, {
                    errorHandler.handle(CanNotUploadVideo())
                    viewState.showImageUploadingError(file)
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded(file) })
        mediaDisposable.add(processes[file]!!)

    }

    fun loadAudio(file: String) {
        var progress = 0f
        processes[file] = photoGateway.uploadAudioToAws(file, postId, appApi::uploadCommentsMedia)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showImageUploadingStarted(file) }
                .subscribe( {
                    progress = it
                    viewState.showImageUploadingProgress(it, file)
                }, {
                    errorHandler.handle(CanNotUploadAudio())
                    viewState.showImageUploadingError(file)
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded(file) })
        mediaDisposable.add(processes[file]!!)

    }

    fun loadImage(file: String) {
        var progress = 0f
        processes[file] = photoGateway.uploadImageToAws(file, postId, appApi::uploadCommentsMedia)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    viewState.showImageUploadingStarted(file) }
                .subscribe( {
                    progress = it
                    viewState.showImageUploadingProgress(it, file)
                }, {
                    errorHandler.handle(CanNotUploadPhoto())
                    viewState.showImageUploadingError(file)
                }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded(file) })
        mediaDisposable.add(processes[file]!!)
    }


    fun retryLoading(file: String) {
        var progress = 0f
        val type = MimeTypeMap.getFileExtensionFromUrl(file)
        when (MimeTypeMap.getSingleton().getMimeTypeFromExtension(type) ?: "") {
            in listOf("audio/mpeg", "audio/aac", "audio/wav") -> {
                processes[file] = photoGateway.uploadAudioToAws(file, postId, appApi::uploadCommentsMedia)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( {
                            progress = it
                            viewState.showImageUploadingProgress(it, file)
                        }, {
                            errorHandler.handle(CanNotUploadPhoto())
                            viewState.showImageUploadingError(file)
                        }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded(file) })
            }
            in listOf("video/mpeg", "video/mp4", "video/webm", "video/3gpp") -> {
                processes[file] = photoGateway.uploadVideoToAws(file, postId,appApi::uploadCommentsMedia)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( {
                            progress = it
                            viewState.showImageUploadingProgress(it, file)
                        }, {
                            errorHandler.handle(CanNotUploadPhoto())
                            viewState.showImageUploadingError(file)
                        }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded(file) })
            }
            else -> {
                processes[file] = photoGateway.uploadImageToAws(file, postId, appApi::uploadCommentsMedia)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( {
                            progress = it
                            viewState.showImageUploadingProgress(it, file)
                        }, {
                            errorHandler.handle(CanNotUploadPhoto())
                            viewState.showImageUploadingError(file)
                        }, { if (progress >= ImageUploadingDelegate.FULL_UPLOADED_PROGRESS) viewState.showImageUploaded(file) })
            }
        }
        processes[file]?.let {
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