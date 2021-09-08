package com.intergroupapplication.presentation.feature.agreement.viewmodel

import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.entity.TermsEntity
import com.intergroupapplication.domain.gateway.ImeiGateway
import com.intergroupapplication.domain.usecase.AppStatusUseCase
import com.intergroupapplication.presentation.base.BaseViewModel
import com.intergroupapplication.presentation.exstension.handleLoading
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class AgreementViewModel @Inject constructor(
      private val useCase: AppStatusUseCase
      ): BaseViewModel() {

    val isLoading = PublishSubject.create<Boolean>()
    val value = BehaviorSubject.create<String>()
    val errorState = PublishSubject.create<Throwable>()

    fun getPrivacy() {
        viewModelDisposable.add(useCase.getPrivacy()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { isLoading.onNext(true) }
            .doFinally { isLoading.onNext(false) }
            .subscribe({
                value.onNext(it)
            }, { errorState.onNext(it) }))
    }

    fun getTerms() {
        viewModelDisposable.add(useCase.getTerms()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { isLoading.onNext(true) }
            .doFinally { isLoading.onNext(false) }
            .subscribe({
                value.onNext(it)
            }, { errorState.onNext(it) }))
    }

    fun getRightHolders() {
        viewModelDisposable.add(useCase.getRightHolders()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { isLoading.onNext(true) }
            .doFinally { isLoading.onNext(false) }
            .subscribe({
                value.onNext(it)
            }, { errorState.onNext(it) }))
    }

}