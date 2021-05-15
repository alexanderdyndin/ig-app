package com.intergroupapplication.presentation.feature.group.presenter

import moxy.InjectViewState
import com.intergroupapplication.R
import com.intergroupapplication.domain.exception.ForbiddenException
import com.intergroupapplication.domain.gateway.ComplaintsGateway
import com.intergroupapplication.domain.gateway.GroupGateway
import com.intergroupapplication.domain.usecase.GroupUseCase
import com.intergroupapplication.domain.usecase.PostsUseCase
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.group.view.GroupView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import javax.inject.Inject

@InjectViewState
class GroupPresenter @Inject constructor(private val groupGateway: GroupGateway,
                                         private val groupUseCase: GroupUseCase,
                                         private val postsUseCase: PostsUseCase,
                                         private val imageUploadingDelegate: ImageUploadingDelegate,
                                         private val errorHandler: ErrorHandler,
                                         private val complaintsGateway: ComplaintsGateway)
    : BasePresenter<GroupView>() {

    private val postsDisposable = CompositeDisposable()
    private var uploadingImageDisposable: Disposable? = null

    fun attachFromGallery(groupId: String? = null) {
        stopImageUploading()
        uploadingImageDisposable = imageUploadingDelegate.uploadFromGallery(viewState, errorHandler, groupId)
    }

    fun attachFromCamera(groupId: String?) {
        stopImageUploading()
        uploadingImageDisposable = imageUploadingDelegate.uploadFromCamera(viewState, errorHandler, groupId)
    }

    fun changeGroupAvatar(groupId: String) {
        compositeDisposable.add(imageUploadingDelegate.getLastPhotoUploadedUrl()
                .flatMap {
                    groupGateway.changeGroupAvatar(groupId, it)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ it.avatar?.let { avatar -> viewState.avatarChanged(avatar) } }, {
                    viewState.showImageUploadingError()
                    errorHandler.handle(it)
                }))
    }

    fun getGroupLastAvatar(groupId: String) {
        compositeDisposable.add(groupGateway.getGroupDetailInfo(groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, { errorHandler.handle(it) }))
    }

    fun getGroupDetailInfo(groupId: String) {
        compositeDisposable.add(groupGateway.getGroupDetailInfo(groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    viewState.showGroupInfo(it)
                    it
                }
                .observeOn(Schedulers.io())
                .flatMap { groupUseCase.getUserRole(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .map { viewState.renderViewByRole(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showGroupInfoLoading(true) }
                .doFinally { viewState.showGroupInfoLoading(false) }
                .subscribe({
                    //getGroupPosts(groupId)
                }, {
                    if (it !is ForbiddenException) errorHandler.handle(it)
                }))
    }


    fun followGroup(groupId: String, followersCount: Int) {
        compositeDisposable.add(groupGateway.followGroup(groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showSubscribeLoading(true) }
                .doFinally { viewState.showSubscribeLoading(false) }
                .subscribe({ viewState.groupFollowed(followersCount + 1) }, {
                    viewState.groupFollowedError()
                    errorHandler.handle(it)
                }))
    }

    fun unfollowGroup(groupId: String, followersCount: Int) {
        compositeDisposable.add(groupGateway.unfollowGroup(groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showSubscribeLoading(true) }
                .doFinally { viewState.showSubscribeLoading(false) }
                .subscribe({ viewState.groupUnfollowed(followersCount - 1) }, {
                    viewState.groupUnfollowedError()
                    errorHandler.handle(it)
                }))
    }

    fun setReact(isLike: Boolean, isDislike: Boolean, postId: String) {
        compositeDisposable.add(postsUseCase.setReact(isLike, isDislike, postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    if (isLike) {
                        viewState.showMessage("Лайк отправляется")
                    } else {
                        viewState.showMessage("Дизлайк отправляется")
                    }
                }
                //.doFinally { viewState.showSubscribeLoading(false) }
                .subscribe({
                    if (isLike) {
                        viewState.showMessage("Лайк поставлен")
                    } else {
                        viewState.showMessage("Дизлайк поставлен")
                    }
                }, {
                    viewState.showMessage("Ошибка установки реакции")
                    errorHandler.handle(it)
                }))
    }


    fun complaintPost(postId: Int) {
        compositeDisposable.add(complaintsGateway.complaintPost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState?.showMessage(R.string.complaint_send)
                }, {
                    errorHandler.handle(it)
                }))
    }

    override fun onDestroy() {
        super.onDestroy()
        postsDisposable.clear()
        stopImageUploading()
    }

    private fun stopImageUploading() {
        uploadingImageDisposable?.dispose()
    }
}
