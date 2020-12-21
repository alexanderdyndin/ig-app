package com.intergroupapplication.presentation.feature.mainActivity.presenter

import androidx.paging.RxPagedListBuilder
import androidx.fragment.app.FragmentManager
import android.util.Log
import moxy.InjectViewState
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.gateway.ComplaintsGetaway
import com.intergroupapplication.domain.gateway.UserProfileGateway
import com.intergroupapplication.domain.usecase.AppStatusUseCase
import com.intergroupapplication.presentation.base.BasePagingState.Companion.PAGINATION_PAGE_SIZE
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivityView
import com.intergroupapplication.presentation.feature.newVersionDialog.NewVersionDialog
import com.intergroupapplication.presentation.feature.news.pagingsource.NewsDataSourceFactory
import com.intergroupapplication.presentation.feature.news.view.NewsView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import javax.inject.Inject

@InjectViewState
class MainActivityPresenter @Inject constructor(private val errorHandler: ErrorHandler,
                                        private val appStatusUseCase: AppStatusUseCase,
                                        private val userProfileGateway: UserProfileGateway,
                                        private val imageUploadingDelegate: ImageUploadingDelegate,
                                        private val sessionStorage: UserSession)
    : BasePresenter<MainActivityView>() {


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
                //errorHandler.handle(e)
            }
        }
    }

}
