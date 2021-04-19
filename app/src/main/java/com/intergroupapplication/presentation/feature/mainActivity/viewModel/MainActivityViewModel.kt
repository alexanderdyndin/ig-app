package com.intergroupapplication.presentation.feature.mainActivity.viewModel

import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.exception.NewVersionException
import com.intergroupapplication.domain.usecase.AppStatusUseCase
import com.intergroupapplication.domain.usecase.GetProfileUseCase
import com.intergroupapplication.presentation.feature.newVersionDialog.NewVersionDialog
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val appStatusUseCase: AppStatusUseCase,
                                                private val compositeDisposable: CompositeDisposable,
                                                private val sessionStorage: UserSession,
                                                private val errorHandler: ErrorHandler
                                                ): ViewModel() {


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

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

}