package com.intergroupapplication.presentation.feature.mainActivity.viewModel

import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.usecase.AppStatusUseCase
import com.intergroupapplication.domain.usecase.GetProfileUseCase
import com.intergroupapplication.presentation.feature.newVersionDialog.NewVersionDialog
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val errorHandler: ErrorHandler,
                                                private val appStatusUseCase: AppStatusUseCase,
                                                private val compositeDisposable: CompositeDisposable,
                                                private val sessionStorage: UserSession): ViewModel() {


    fun checkNewVersionAvaliable(fragmentManager: FragmentManager) {
        GlobalScope.launch {
            try {
                val isValid = appStatusUseCase.invoke(BuildConfig.VERSION_NAME).blockingGet()
                Log.d("MY", "version_check_response = $isValid ")
                if (isValid == "invalid") {
                    val myDialogFragment = NewVersionDialog()
                    val manager = fragmentManager
                    myDialogFragment.isCancelable = false
                    myDialogFragment.show(manager, "myDialog")
                }
            } catch (e:Throwable) {
                errorHandler.handle(e)
            }
        }
    }

    fun getAdCount() {
        if (sessionStorage.user != null) {
            compositeDisposable.add(appStatusUseCase.getAdParameters()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ sessionStorage.countAd = it },
                            { errorHandler.handle(it) }))
        }
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

}