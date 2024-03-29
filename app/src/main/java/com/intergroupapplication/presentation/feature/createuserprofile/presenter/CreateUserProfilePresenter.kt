package com.intergroupapplication.presentation.feature.createuserprofile.presenter

import com.intergroupapplication.di.qualifier.UserProfileHandler
import com.intergroupapplication.domain.entity.CreateUserEntity
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.domain.gateway.UserProfileGateway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.createuserprofile.view.CreateUserProfileView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class CreateUserProfilePresenter @Inject constructor(
    private val userProfileGateway: UserProfileGateway,
    private val imageUploadingDelegate: ImageUploader,
    @UserProfileHandler private val errorHandler: ErrorHandler,
    private val photoGateway: PhotoGateway
) : BasePresenter<CreateUserProfileView>() {

    private var uploadingDisposable: Disposable? = null


    fun createUserProfile(
        name: String, surName: String,
        birthDay: String, gender: String
    ) {
        compositeDisposable.add(
            imageUploadingDelegate.getLastPhotoUploadedUrl()
                .flatMap {
                    if (it.isNotEmpty()) {
                        userProfileGateway.createUserProfile(
                            CreateUserEntity(
                                name,
                                surName, birthDay, gender, it
                            )
                        )
                    } else {
                        userProfileGateway.createUserProfile(
                            CreateUserEntity(
                                name,
                                surName, birthDay, gender, null
                            )
                        )
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    viewState.completed()
                }, {
                    errorHandler.handle(it)
                })
        )
    }

    fun takePhotoFromCamera() {
        stopImageUploading()
        uploadingDisposable = imageUploadingDelegate.uploadFromCamera(
            viewState,
            errorHandler,
            upload = photoGateway::uploadAvatarUser
        )
    }

    fun takePhotoFromGallery() {
        stopImageUploading()
        uploadingDisposable = imageUploadingDelegate.uploadFromGallery(
            viewState,
            errorHandler,
            upload = photoGateway::uploadAvatarUser
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        uploadingDisposable?.dispose()
    }

    private fun stopImageUploading() {
        uploadingDisposable?.dispose()
    }
}
