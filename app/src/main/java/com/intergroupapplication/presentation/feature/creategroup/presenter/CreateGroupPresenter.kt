package com.intergroupapplication.presentation.feature.creategroup.presenter

import com.intergroupapplication.domain.entity.CreateGroupEntity
import com.intergroupapplication.domain.gateway.CreateGroupGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.creategroup.view.CreateGroupView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class CreateGroupPresenter @Inject constructor(
    private val imageUploadingDelegate: ImageUploader,
    private val createGroupGateway: CreateGroupGateway,
    private val errorHandler: ErrorHandler,
    private val photoGateway: PhotoGateway
) : BasePresenter<CreateGroupView>() {

    private var uploadingDisposable: Disposable? = null

    fun createGroup(
        groupName: String, groupDescription: String, subject: String, rules: String,
        isClosed: Boolean, ageRestriction: String
    ) {
        compositeDisposable.add(
            imageUploadingDelegate.getLastPhotoUploadedUrl()
                .flatMap {
                    if (it.isNotEmpty()) {
                        createGroupGateway.createGroup(
                            CreateGroupEntity(
                                groupName,
                                groupDescription, it, subject, rules, isClosed,
                                ageRestriction
                            )
                        )
                    } else {
                        createGroupGateway.createGroup(
                            CreateGroupEntity(
                                groupName,
                                groupDescription, null, subject, rules, isClosed,
                                ageRestriction
                            )
                        )
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    viewState.goToGroupScreen(it.id)
                }, {
                    errorHandler.handle(it)
                })
        )
    }

    fun takePhotoFromCamera() {
        stopImageUploading()
        uploadingDisposable = imageUploadingDelegate.uploadFromCamera(
            viewState, errorHandler,
            upload = ::upload
        )
    }

    fun takePhotoFromGallery() {
        stopImageUploading()
        uploadingDisposable = imageUploadingDelegate.uploadFromGallery(
            viewState, errorHandler,
            upload = ::upload
        )
    }

    private fun upload(groupId: String? = null): Observable<Float> = photoGateway
        .uploadAvatarGroup(groupId)


    override fun onDestroy() {
        super.onDestroy()
        uploadingDisposable?.dispose()
    }

    private fun stopImageUploading() {
        uploadingDisposable?.dispose()
    }

}
