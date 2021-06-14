package com.intergroupapplication.presentation.feature.editpost.presenter

import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.domain.entity.AudioRequestEntity
import com.intergroupapplication.domain.entity.CreateGroupPostEntity
import com.intergroupapplication.domain.entity.FileRequestEntity
import com.intergroupapplication.domain.gateway.GroupPostGateway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class EditPostPresenter @Inject constructor(private val groupPostGateway: GroupPostGateway,
                                            private val errorHandler: ErrorHandler)
    :BasePresenter<CreatePostView>() {

    fun editPost(postText: String, postId: String, photos: Single<List<ChooseMedia>>,
                            videos: Single<List<ChooseMedia>>, audios: Single<List<ChooseMedia>>) {
        compositeDisposable.add(Single.zip(photos,
                videos,
                audios,
                object : Function3<List<ChooseMedia>, List<ChooseMedia>, List<ChooseMedia>,
                        CreateGroupPostEntity> {
                    override fun invoke(photo: List<ChooseMedia>, video: List<ChooseMedia>,
                                        audio: List<ChooseMedia>): CreateGroupPostEntity {
                        return CreateGroupPostEntity(postText,
                                photo.map { FileRequestEntity(file = it.url, description = null,
                                    title = it.name) },
                                audio.map { AudioRequestEntity(it.url, null, it.name,
                                    it.authorMusic, null,it.duration) },
                                video.map {
                                    FileRequestEntity(file = it.url, description = null,
                                            title = it.name, it.urlPreview,it.duration)
                                },
                                false,
                                null)
                    }
                }
        )
                .subscribeOn(Schedulers.io())
                .flatMap {
                    groupPostGateway.editPost(it,postId) }
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({ viewState.postCreateSuccessfully(it) }, {
                    errorHandler.handle(it) }))
    }
}