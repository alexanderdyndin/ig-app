package com.intergroupapplication.presentation.feature.navigation.presenter

import moxy.InjectViewState
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.gateway.UserProfileGateway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListScreen
import com.intergroupapplication.presentation.feature.login.view.LoginScreen
import com.intergroupapplication.presentation.feature.navigation.view.NavigationView
import com.intergroupapplication.presentation.feature.news.view.NewsScreen
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class NavigationPresenter @Inject constructor(private val router: Router,
                                              private val errorHandler: ErrorHandler,
                                              private val userProfileGateway: UserProfileGateway,
                                              private val imageUploadingDelegate: ImageUploadingDelegate,
                                              private val sessionStorage: UserSession)
    : BasePresenter<NavigationView>() {

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
                .subscribe({ viewState.showUserInfo(it) }, {
                    viewState.showImageUploadingError()
                    errorHandler.handle(it)
                }))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopImageUploading()
    }

    private fun stopImageUploading() {
        uploadingImageDisposable?.dispose()
    }

    fun goOutFromProfile() {
        sessionStorage.logout()
        router.newRootScreen(LoginScreen())
    }

    fun goToGroupListScreen() {
        router.navigateTo(GroupListScreen())
    }

    fun goToNewsScreen() {
        router.navigateTo(NewsScreen())
    }
}
