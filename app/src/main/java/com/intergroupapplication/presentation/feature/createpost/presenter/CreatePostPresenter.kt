package com.intergroupapplication.presentation.feature.createpost.presenter

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
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class CreatePostPresenter @Inject constructor(
    private val groupPostGateway: GroupPostGateway,
    private val errorHandler: ErrorHandler
) : BasePresenter<CreatePostView>() {

    var groupId: String = ""

    fun createPost(
        createGroupPostEntity: CreateGroupPostEntity,
        groupId: String
    ) {
        compositeDisposable.add(
            groupPostGateway.createPost(createGroupPostEntity, groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    viewState.postCreateSuccessfully(it)
                }, { errorHandler.handle(it) })
        )
    }

    fun createPostWithImage(
        postText: String, groupId: String, photos: Single<List<ChooseMedia>>,
        videos: Single<List<ChooseMedia>>, audios: Single<List<ChooseMedia>>,
        finalNamesMedia: List<String>
    ) {
        compositeDisposable.add(Single.zip(photos,
            videos,
            audios,
            object :
                Function3<List<ChooseMedia>, List<ChooseMedia>, List<ChooseMedia>, CreateGroupPostEntity> {
                override fun invoke(
                    photo: List<ChooseMedia>,
                    video: List<ChooseMedia>,
                    audio: List<ChooseMedia>
                ): CreateGroupPostEntity {
                    return CreateGroupPostEntity(postText,
                        photo.filter { finalNamesMedia.contains(it.name) }
                            .map {
                                FileRequestEntity(
                                    file = it.url,
                                    description = null,
                                    title = it.name
                                )
                            },
                        audio.filter { finalNamesMedia.contains(it.name) }
                            .map {
                                AudioRequestEntity(
                                    it.url,
                                    null,
                                    it.name,
                                    it.author,
                                    null,
                                    it.duration
                                )
                            },
                        video.filter { finalNamesMedia.contains(it.name) }
                            .map {
                                FileRequestEntity(
                                    file = it.url, description = null,
                                    title = it.name, it.urlPreview, it.duration
                                )
                            },
                        false,
                        null
                    )
                }
            }
        )
            .subscribeOn(Schedulers.io())
            .flatMap {
                groupPostGateway.createPost(it, groupId)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .handleLoading(viewState)
            .subscribe({ viewState.postCreateSuccessfully(it) }, {
                errorHandler.handle(it)
            })
        )
    }
}
