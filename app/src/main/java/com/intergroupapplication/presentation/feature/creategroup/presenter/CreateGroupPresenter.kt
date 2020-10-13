package com.intergroupapplication.presentation.feature.creategroup.presenter

import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.feature.creategroup.view.CreateGroupView

import moxy.InjectViewState
import com.intergroupapplication.domain.entity.CreateGroupEntity
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.gateway.CreateGroupGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.Screens
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.group.view.GroupScreen
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class CreateGroupPresenter @Inject constructor(private val router: Router,
                                               private val imageUploadingDelegate: ImageUploader,
                                               private val createGroupGateway: CreateGroupGateway,
                                               private val errorHandler: ErrorHandler)
    : BasePresenter<CreateGroupView>() {

    private var uploadingDisposable: Disposable? = null

    fun createGroup(groupName: String, groupDescription: String) {
        compositeDisposable.add(
                imageUploadingDelegate.getLastPhotoUploadedUrl()
                        .flatMap {
                            if (!it.isEmpty()) {
                                createGroupGateway.createGroup(CreateGroupEntity(groupName,
                                        groupDescription, it))
                            } else {
                                createGroupGateway.createGroup(CreateGroupEntity(groupName,
                                        groupDescription, null))
                            }
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .handleLoading(viewState)
                        .subscribe({
                            goToGroupScreen(it)
                        }, {
                            errorHandler.handle(it)
                        }))
    }

    fun takePhotoFromCamera() {
        stopImageUploading()
        uploadingDisposable = imageUploadingDelegate.uploadFromCamera(viewState, errorHandler)
    }

    fun takePhotoFromGallery() {
        stopImageUploading()
        uploadingDisposable = imageUploadingDelegate.uploadFromGallery(viewState, errorHandler)
    }

    override fun onDestroy() {
        super.onDestroy()
        uploadingDisposable?.dispose()
    }

    private fun stopImageUploading() {
        uploadingDisposable?.dispose()
    }

    fun goToGroupScreen(entity: GroupEntity) {
        router.replaceScreen(GroupScreen(entity.id))
    }

}
