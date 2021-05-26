package com.intergroupapplication.presentation.feature.createpost.presenter
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.domain.entity.AudioRequestEntity
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostView

import moxy.InjectViewState
import com.intergroupapplication.domain.entity.CreateGroupPostEntity
import com.intergroupapplication.domain.entity.FileRequestEntity
import com.intergroupapplication.domain.gateway.GroupPostGateway
import com.intergroupapplication.presentation.exstension.handleLoading
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


import javax.inject.Inject
import kotlin.math.min

@InjectViewState
class CreatePostPresenter @Inject constructor(private val groupPostGateway: GroupPostGateway,
                                              private val errorHandler: ErrorHandler)
    : BasePresenter<CreatePostView>() {

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

    fun createPostWithImage(postText: String, groupId: String,photos:Single<List<String>>,
            videos:Single<List<ChooseMedia>>,audios:Single<List<ChooseMedia>>) {
        compositeDisposable.add(Single.zip(photos,
                videos,
                audios,
                object : Function3<List<String>, List<ChooseMedia>, List<ChooseMedia>, CreateGroupPostEntity> {
                    override fun invoke(photo: List<String>, video: List<ChooseMedia>, audio: List<ChooseMedia>): CreateGroupPostEntity {
                        return CreateGroupPostEntity(postText,
                                photo.map { FileRequestEntity(file = it, description = null, title = it.substringAfter("/posts/")) },
                                audio.map { AudioRequestEntity(it.url, null, it.trackName, it.authorMusic, null,it.duration) },
                                video.map {
                                    FileRequestEntity(file = it.url, description = null,
                                            title = it.url.substringAfter("/posts/"), it.urlPreview,it.duration)
                                },
                                false,
                                null)
                    }
                }
        )
                .subscribeOn(Schedulers.io())
                .flatMap {
                    Timber.tag("tut_flatMap").d(it.toString())
                    Timber.tag("tut_groupId").d(groupId)
                    groupPostGateway.createPost(it, groupId) }
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({ viewState.postCreateSuccessfully(it) }, {
                    Timber.tag("tut_error").e(it)
                    errorHandler.handle(it) }))
    }
}
