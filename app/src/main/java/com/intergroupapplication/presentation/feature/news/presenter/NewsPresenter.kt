package com.intergroupapplication.presentation.feature.news.presenter

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
import com.intergroupapplication.presentation.feature.newVersionDialog.NewVersionDialog
import com.intergroupapplication.presentation.feature.news.pagingsource.NewsDataSourceFactory
import com.intergroupapplication.presentation.feature.news.view.NewsView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

import javax.inject.Inject

@InjectViewState
class NewsPresenter @Inject constructor(private val errorHandler: ErrorHandler,
                                        private val newsDataSourceFactory: NewsDataSourceFactory,
                                        private val complaintsGetaway: ComplaintsGetaway,
                                        private val appStatusUseCase: AppStatusUseCase,
                                        private val userProfileGateway: UserProfileGateway,
                                        private val imageUploadingDelegate: ImageUploadingDelegate)
    : BasePresenter<NewsView>() {

    private val newsDisposable = CompositeDisposable()

    fun getNews() {
        newsDisposable.add(newsDataSourceFactory.source.observeState()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    it.error?.let { throwable ->
                        errorHandler.handle(throwable)
                    }
                    viewState.handleState(it.type)
                }, {}))

        newsDisposable.add(RxPagedListBuilder(newsDataSourceFactory, PAGINATION_PAGE_SIZE)
                .buildObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    viewState.newsLoaded(it)
                }, {
                    errorHandler.handle(it)
                }))
    }

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

    fun complaintPost(postId: Int) {
        newsDisposable.add(complaintsGetaway.complaintPost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState?.showMessage(R.string.complaint_send)
                }, {
                    errorHandler.handle(it)
                }))
    }

    fun reload() {
        newsDataSourceFactory.source.reload()
    }

    fun refresh() {
        unsubscribe()
        getNews()
    }

    fun unsubscribe() {
        newsDisposable.clear()
    }

    private var uploadingImageDisposable: Disposable? = null

    fun attachFromGallery() {
        stopImageUploading()
        uploadingImageDisposable = imageUploadingDelegate.uploadFromGallery(viewState, errorHandler)
    }

    fun attachFromCamera() {
        stopImageUploading()
        uploadingImageDisposable = imageUploadingDelegate.uploadFromCamera(viewState, errorHandler)
    }

    fun changeUserAvatar() {
        compositeDisposable.add(imageUploadingDelegate.getLastPhotoUploadedUrl()
                .flatMap { userProfileGateway.changeUserProfileAvatar(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ viewState.avatarChanged(it) }, {
                    viewState.showImageUploadingError()
                    errorHandler.handle(it)
                }))
    }

    fun showLastUserAvatar() {
        compositeDisposable.add(userProfileGateway.getUserProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ viewState.showLastAvatar(it.avatar) }, { errorHandler.handle(it) }))
    }

    fun getUserInfo() {
        compositeDisposable.add(userProfileGateway.getUserProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState.showUserInfo(it)
                           }, {
                    viewState.showImageUploadingError()
                    errorHandler.handle(it)
                }))
    }


    private fun stopImageUploading() {
        uploadingImageDisposable?.dispose()
    }


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getNews()
    }

    override fun destroyView(view: NewsView?) {
        unsubscribe()
        super.destroyView(view)
    }


}
