package com.intergroupapplication.presentation.feature.mainActivity.viewModel

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.exception.CanNotUploadPhoto
import com.intergroupapplication.domain.exception.NewVersionException
import com.intergroupapplication.domain.usecase.AppStatusUseCase
import com.intergroupapplication.domain.usecase.AvatarUploadingUseCase
import com.intergroupapplication.domain.usecase.UserProfileUseCase
import com.intergroupapplication.presentation.base.ImageUploadingState
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.newVersionDialog.NewVersionDialog
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val appStatusUseCase: AppStatusUseCase,
                                                private val sessionStorage: UserSession,
                                                private val userProfileUseCase: UserProfileUseCase,
                                                private val avatarUploadingUseCase: AvatarUploadingUseCase,
                                                private val errorHandler: ErrorHandler
                                                ): ViewModel() {

    val imageUploadingState: MutableLiveData<ImageUploadingState> by lazy {
        MutableLiveData<ImageUploadingState>()
    }

    private val compositeDisposable = CompositeDisposable()

    fun checkNewVersionAvaliable(fragmentManager: FragmentManager) {
        compositeDisposable.add(appStatusUseCase.getAppStatus(BuildConfig.VERSION_NAME)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({  },
                        { if (it is NewVersionException) {
                            Timber.d(it)
                            val myDialogFragment = NewVersionDialog()
                            val manager = fragmentManager
                            myDialogFragment.isCancelable = false
                            myDialogFragment.show(manager, "myDialog")
                        } else {
                            Timber.e(it)
                        }
                        }))
    }

    fun getAdCount() {
        compositeDisposable.add(appStatusUseCase.getAdParameters()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ sessionStorage.countAd = it },
                        { Timber.e(it) }))
    }

    fun getUserProfile() = userProfileUseCase.getUserProfile()

    fun uploadImageFromGallery(file: String) {
        compositeDisposable.clear()
        compositeDisposable.add(avatarUploadingUseCase.upload(file)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                imageUploadingState.value = ImageUploadingState.ImageUploadingStarted(file)
            }
            .subscribe({
                if (it is ImageUploadingState.ImageUploaded)
                    changeAvatar(it.path)
                else
                    imageUploadingState.value = it
            }, {
                imageUploadingState.value = ImageUploadingState.ImageUploadingError(file)
                errorHandler.handle(it)
            }))
    }

    private fun changeAvatar(photo: String) {
        compositeDisposable.add( userProfileUseCase.changeAvatar(photo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                imageUploadingState.value = ImageUploadingState.ImageUploaded(it)
            }, {
                imageUploadingState.value = ImageUploadingState.ImageUploadingError()
                errorHandler.handle(it)
            }))
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

}