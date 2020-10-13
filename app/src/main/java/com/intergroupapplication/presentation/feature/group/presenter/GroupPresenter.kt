package com.intergroupapplication.presentation.feature.group.presenter

import androidx.paging.RxPagedListBuilder
import moxy.InjectViewState
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.InfoForCommentEntity
import com.intergroupapplication.domain.exception.PageNotFoundException
import com.intergroupapplication.domain.gateway.ComplaintsGetaway
import com.intergroupapplication.domain.gateway.GroupGateway
import com.intergroupapplication.domain.usecase.GroupUseCase
import com.intergroupapplication.presentation.base.BasePagingState
import com.intergroupapplication.presentation.base.BasePagingState.Companion.PAGINATION_PAGE_SIZE
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsScreen
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostScreen
import com.intergroupapplication.presentation.feature.group.pagingsource.GroupPostDataSourceFactory
import com.intergroupapplication.presentation.feature.group.view.GroupView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class GroupPresenter @Inject constructor(private val router: Router,
                                         private val groupGateway: GroupGateway,
                                         private val postSourceFactory: GroupPostDataSourceFactory,
                                         private val groupUseCase: GroupUseCase,
                                         private val imageUploadingDelegate: ImageUploadingDelegate,
                                         private val errorHandler: ErrorHandler,
                                         private val complaintsGetaway: ComplaintsGetaway)
    : BasePresenter<GroupView>() {

    private val postsDisposable = CompositeDisposable()
    private var uploadingImageDisposable: Disposable? = null

    fun attachFromGallery() {
        stopImageUploading()
        uploadingImageDisposable = imageUploadingDelegate.uploadFromGallery(viewState, errorHandler)
    }

    fun attachFromCamera() {
        stopImageUploading()
        uploadingImageDisposable = imageUploadingDelegate.uploadFromCamera(viewState, errorHandler)
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
                    getGroupPosts(groupId)
                }, {
                    errorHandler.handle(it)
                }))
    }

    fun getGroupPosts(groupId: String) {
        postSourceFactory.source.groupId = groupId
        postsDisposable.add(postSourceFactory.source.observeState()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    it.error?.let { throwable ->
                        errorHandler.handle(throwable)
                    }
                    //todo исправить пагинацию
                    if (it.error !is PageNotFoundException) {
                        viewState.handleState(it.type)
                    } else {
                        viewState.handleState(BasePagingState.Type.NONE)
                    }
                }, {}))

        postsDisposable.add(RxPagedListBuilder(postSourceFactory, PAGINATION_PAGE_SIZE)
                .buildObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState.postsLoaded(it)
                }, {
                    errorHandler.handle(it)
                }))
    }

    fun reload() {
        postSourceFactory.source.reload()
    }

    fun refresh(groupId: String) {
        unsubscribe()
        getGroupPosts(groupId)
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

    fun goBack() {
        router.exit()
    }

    fun complaintPost(postId: Int) {
        compositeDisposable.add(complaintsGetaway.complaintPost(postId)
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

    private fun unsubscribe() {
        postsDisposable.clear()
    }

    private fun stopImageUploading() {
        uploadingImageDisposable?.dispose()
    }
}
