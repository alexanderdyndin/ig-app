package com.intergroupapplication.data.network

import io.reactivex.*
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.lang.reflect.Type

/**
 * Created by abakarmagomedov on 17/08/2018 at project InterGroupApplication.
 */
class RxErrorCallAdapterFactory(private val errorAdapter: ErrorAdapter) : CallAdapter.Factory() {

    companion object {
        fun create(errorAdapter: ErrorAdapter):
                RxErrorCallAdapterFactory = RxErrorCallAdapterFactory(errorAdapter)
    }

    private val original: RxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()

    @Suppress("UNCHECKED_CAST")
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *> =
        RxCallAdapterWrapper(
            original.get(returnType, annotations, retrofit) as CallAdapter<Any, Any>,
            errorAdapter
        )


    class RxCallAdapterWrapper<R>(
        private val wrapped: CallAdapter<R, Any>,
        private val errorAdapter: ErrorAdapter
    ) :
        CallAdapter<R, Any> {

        override fun adapt(call: Call<R>): Any {
            return when (val result = wrapped.adapt(call)) {
                is Single<*> -> result.onErrorResumeNext { Single.error(asInterGroupException(it)) }
                is Observable<*> -> result.onErrorResumeNext { t: Throwable ->
                    Observable.error(asInterGroupException(t))
                }
                is Flowable<*> -> result.onErrorResumeNext { t: Throwable ->
                    Flowable.error(asInterGroupException(t))
                }
                is Completable -> result.onErrorResumeNext {
                    Completable.error(
                        asInterGroupException(
                            it
                        )
                    )
                }
                is Maybe<*> -> result.onErrorResumeNext { t: Throwable ->
                    Maybe.error(
                        asInterGroupException(t)
                    )
                }
                else -> result
            }
        }

        override fun responseType(): Type = wrapped.responseType()

        private fun asInterGroupException(throwable: Throwable): Throwable {
            return errorAdapter.adapt(throwable)
        }
    }
}
