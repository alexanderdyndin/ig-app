package com.intergroupapplication.presentation.feature.agreements.viewmodel

import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.entity.TermsEntity
import com.intergroupapplication.domain.gateway.ImeiGateway
import com.intergroupapplication.domain.usecase.AppStatusUseCase
import com.intergroupapplication.presentation.base.BaseViewModel
import com.intergroupapplication.presentation.exstension.handleLoading
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class AgreementsViewModel @Inject constructor(
      private val imeiGateway: ImeiGateway,
      private val userSession: UserSession
      ): BaseViewModel() {

    val isLoading = PublishSubject.create<Boolean>()
    val isNext = PublishSubject.create<Boolean>()

    fun next() {
        viewModelDisposable.add(imeiGateway.extractDeviceInfo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            //.andThen(permissionGetaway.isBlocked()) //проверку по imei убрали
            .doOnSubscribe { isLoading.onNext(true) }
            .doFinally { isLoading.onNext(false) }
            .subscribe({
                userSession.acceptTerms = TermsEntity(true)
                isNext.onNext(true)
            }, { isNext.onError(it) }))
    }

}