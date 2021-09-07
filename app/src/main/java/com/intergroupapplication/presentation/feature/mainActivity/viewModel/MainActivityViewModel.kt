package com.intergroupapplication.presentation.feature.mainActivity.viewModel

import androidx.fragment.app.FragmentManager
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.exception.NewVersionException
import com.intergroupapplication.domain.usecase.AppStatusUseCase
import com.intergroupapplication.domain.usecase.AvatarUploadingUseCase
import com.intergroupapplication.domain.usecase.UserProfileUseCase
import com.intergroupapplication.presentation.base.BaseViewModel
import com.intergroupapplication.presentation.base.ImageUploadingState
import com.intergroupapplication.presentation.feature.newVersionDialog.NewVersionDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    private val appStatusUseCase: AppStatusUseCase,
    private val sessionStorage: UserSession,
    private val userProfileUseCase: UserProfileUseCase,
    private val avatarUploadingUseCase: AvatarUploadingUseCase,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    val imageUploadingState = PublishSubject.create<ImageUploadingState>()

    fun checkNewVersionAvaliable(fragmentManager: FragmentManager) { //todo должно выполняться в активити
        viewModelDisposable.add(
            appStatusUseCase.getAppStatus(BuildConfig.VERSION_NAME)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ },
                    {
                        if (it is NewVersionException) {
                            Timber.d(it)
                            val myDialogFragment = NewVersionDialog()
                            val manager = fragmentManager
                            myDialogFragment.isCancelable = false
                            myDialogFragment.show(manager, "myDialog")
                        } else {
                            Timber.e(it)
                        }
                    })
        )
    }

    fun getAdCount() {
        viewModelDisposable.add(
            appStatusUseCase.getAdParameters()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ sessionStorage.countAd = it },
                    { Timber.e(it) })
        )
    }

    fun getUserProfile() = userProfileUseCase.getUserProfile()

    fun uploadImageFromGallery(file: String) {
        viewModelDisposable.clear()
        viewModelDisposable.add(avatarUploadingUseCase.upload(file)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                imageUploadingState.onNext(ImageUploadingState.ImageUploadingStarted(file))
            }
            .subscribe({
                if (it is ImageUploadingState.ImageUploaded)
                    changeAvatar(it.path)
                else {
                    imageUploadingState.onNext(it)
                }
            }, {
                imageUploadingState.onError(it)
            })
        )
    }

    private fun changeAvatar(photo: String) {
        viewModelDisposable.add(userProfileUseCase.changeAvatar(photo)
            .subscribeOn(Schedulers.io())
            .subscribe({
                imageUploadingState.onNext(ImageUploadingState.ImageUploaded(it))
            }, {
                imageUploadingState.onError(it)
            })
        )
    }

}
