package com.intergroupapplication.presentation.exstension

import com.intergroupapplication.presentation.base.CanShowLoading
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by abakarmagomedov on 14/09/2018 at project InterGroupApplication.
 */
fun <T> Observable<T>.handleLoading(view: CanShowLoading?): Observable<T> {
    return this.doOnSubscribe { view?.showLoading(true) }
            .doFinally { view?.showLoading(false) }
}

fun <T> Single<T>.handleLoading(view: CanShowLoading?): Single<T> {
    return this.doOnSubscribe { view?.showLoading(true) }
            .doFinally { view?.showLoading(false) }
}

fun Completable.handleLoading(view: CanShowLoading?): Completable {
    return this.doOnSubscribe { view?.showLoading(true) }
            .doFinally { view?.showLoading(false) }
}
