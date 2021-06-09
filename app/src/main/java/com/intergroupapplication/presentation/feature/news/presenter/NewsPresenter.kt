package com.intergroupapplication.presentation.feature.news.presenter

import moxy.InjectViewState
import com.intergroupapplication.R
import com.intergroupapplication.domain.gateway.ComplaintsGateway
import com.intergroupapplication.domain.gateway.UserProfileGateway
import com.intergroupapplication.domain.usecase.PostsUseCase
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.news.view.NewsView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import javax.inject.Inject

@InjectViewState
class NewsPresenter @Inject constructor(private val errorHandler: ErrorHandler,
                                        private val postsUseCase: PostsUseCase,
                                        private val userProfileGateway: UserProfileGateway,
                                        private val imageUploadingDelegate: ImageUploadingDelegate)
    : BasePresenter<NewsView>() {

    private val newsDisposable = CompositeDisposable()

    fun complaintPost(postId: Int) {
        newsDisposable.add(postsUseCase.sendComplaint(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState?.showMessage(R.string.complaint_send)
                }, {
                    errorHandler.handle(it)
                }))
    }
    fun refresh() {
        unsubscribe()
    }

    fun unsubscribe() {
        newsDisposable.clear()
    }

    private var uploadingImageDisposable: Disposable? = null

    fun attachFromGallery() {
        stopImageUploading()
        uploadingImageDisposable = imageUploadingDelegate.uploadFromGallery(viewState, errorHandler)
    }

    fun attachFromCamera() {
        stopImageUploading()
        uploadingImageDisposable = imageUploadingDelegate.uploadFromCamera(viewState, errorHandler)
    }

    fun changeUserAvatar() {
        compositeDisposable.add(imageUploadingDelegate.getLastPhotoUploadedUrl()
                .flatMap { userProfileGateway.changeUserProfileAvatar(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ viewState.avatarChanged(it) }, {
                    viewState.showImageUploadingError()
                    errorHandler.handle(it)
                }))
    }

    fun showLastUserAvatar() {
        compositeDisposable.add(userProfileGateway.getUserProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ viewState.showLastAvatar(it.avatar) }, { errorHandler.handle(it) }))
    }

    fun getUserInfo() {
        compositeDisposable.add(userProfileGateway.getUserProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState.showUserInfo(it)
                           }, {
                    viewState.showImageUploadingError()
                    errorHandler.handle(it)
                }))
    }


    private fun stopImageUploading() {
        uploadingImageDisposable?.dispose()
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

    override fun destroyView(view: NewsView?) {
        unsubscribe()
        super.destroyView(view)
    }


}
