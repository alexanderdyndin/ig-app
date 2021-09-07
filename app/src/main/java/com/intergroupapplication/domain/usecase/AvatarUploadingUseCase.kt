package com.intergroupapplication.domain.usecase

import android.app.Activity
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.repository.PhotoRepository
import com.intergroupapplication.domain.exception.CanNotUploadPhoto
import com.intergroupapplication.domain.gateway.AvatarGateway
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.ImageUploadingState
import com.intergroupapplication.presentation.base.ImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.workable.errorhandler.ErrorHandler
import com.yalantis.ucrop.UCrop
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Named

class AvatarUploadingUseCase @Inject constructor(
    private val avatarGateway: AvatarGateway
) {

    fun upload(
        file: String,
        groupId: String? = null
    ) = avatarGateway.uploadToAws(file, groupId)

}