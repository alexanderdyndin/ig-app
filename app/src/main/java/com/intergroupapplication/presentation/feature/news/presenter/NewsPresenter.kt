package com.intergroupapplication.presentation.feature.news.presenter

import androidx.paging.RxPagedListBuilder
import androidx.fragment.app.FragmentManager
import android.util.Log
import androidx.lifecycle.LiveData
import moxy.InjectViewState
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.InfoForCommentEntity
import com.intergroupapplication.domain.exception.PageNotFoundException
import com.intergroupapplication.domain.gateway.ComplaintsGetaway
import com.intergroupapplication.domain.usecase.AppStatusUseCase
import com.intergroupapplication.presentation.base.BasePagingState
import com.intergroupapplication.presentation.base.BasePagingState.Companion.PAGINATION_PAGE_SIZE
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsScreen
import com.intergroupapplication.presentation.feature.group.view.GroupScreen
import com.intergroupapplication.presentation.feature.newVersionDialog.NewVersionDialog
import com.intergroupapplication.presentation.feature.news.pagingsource.NewsDataSourceFactory
import com.intergroupapplication.presentation.feature.news.view.NewsView
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import java.lang.Exception
import javax.inject.Inject

@InjectViewState
class NewsPresenter @Inject constructor(private val router: Router,
                                        private val errorHandler: ErrorHandler,
                                        private val newsDataSourceFactory: NewsDataSourceFactory,
                                        private val complaintsGetaway: ComplaintsGetaway,
                                        private val appStatusUseCase: AppStatusUseCase)
    : BasePresenter<NewsView>() {
    lateinit var liveData: LiveData<String>
    fun getNews() {
        compositeDisposable.add(newsDataSourceFactory.source.observeState()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    it.error?.let { throwable ->
                        errorHandler.handle(throwable)
                    }
                    viewState.handleState(it.type)
                }, {}))

        compositeDisposable.add(RxPagedListBuilder(newsDataSourceFactory, PAGINATION_PAGE_SIZE)
                .buildObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                //.handleLoading(viewState)
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
        compositeDisposable.add(complaintsGetaway.complaintPost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState?.showMessage(R.string.complaint_send)
                }, {
                    errorHandler.handle(it)
                }))
    }

    fun goToGroupScreen(groupId: String) {
        router.navigateTo(GroupScreen(groupId))
    }

    fun reload() {
        newsDataSourceFactory.source.reload()
    }

    fun refresh() {
        unsubscribe()
        getNews()
    }

    private fun unsubscribe() {
        compositeDisposable.clear()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getNews()
    }
}
