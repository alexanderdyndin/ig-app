package com.intergroupapplication.presentation.feature.grouplist.presenter

import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.paging.RxPagedListBuilder
import moxy.InjectViewState
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.gateway.GroupGateway
import com.intergroupapplication.domain.gateway.UserProfileGateway
import com.intergroupapplication.domain.usecase.AppStatusUseCase
import com.intergroupapplication.presentation.base.BasePagingState.Companion.PAGINATION_PAGE_SIZE
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.exstension.handleLoading
import com.intergroupapplication.presentation.feature.grouplist.pagingsource.*
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListView
import com.intergroupapplication.presentation.feature.newVersionDialog.NewVersionDialog
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
class GroupListPresenter @Inject constructor(private val errorHandler: ErrorHandler,
                                             private val appStatusUseCase: AppStatusUseCase,
                                             private val dsAll: GroupListDataSourceFactory,
                                             private val dsSub: GroupListDataSourceFactory,
                                             private val dsAdm: GroupListDataSourceFactory,
                                             private val userProfileGateway: UserProfileGateway,
                                             private val imageUploadingDelegate: ImageUploadingDelegate,
                                             private val sessionStorage: UserSession)
    : BasePresenter<GroupListView>() {


    private val groupsDisposable = CompositeDisposable()

    @Inject
    lateinit var groupGateway: GroupGateway

    var currentScreen = 1


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        dsAll.source.applyAllGroupList()
        dsSub.source.applySubscribedGroupList()
        dsAdm.source.applyOwnedGroupList()
        groupList()
    }

    fun checkNewVersionAvaliable(fragmentManager: FragmentManager) {

        GlobalScope.launch {
            try {
                val isValid = appStatusUseCase.invoke(BuildConfig.VERSION_NAME).blockingGet()
                Log.d("MY", "version_check_response = $isValid ")
                if (isValid == "invalid") {
                    val myDialogFragment = NewVersionDialog()
                    myDialogFragment.isCancelable = false
                    myDialogFragment.show(fragmentManager, "myDialog")
                }
            }  catch (e:Throwable) {
                //errorHandler.handle(e)
            }
        }
    }

    fun groupList() {
//        when (currentScreen) {
//            1 -> getGroupsList()
//            2 -> getFollowGroupsList()
//            3 -> getOwnedGroupsList()
//            else -> {
                getGroupsList()
                getFollowGroupsList()
                getOwnedGroupsList()
//            }
//        }
    }

    fun getGroupsList() {
        groupsDisposable.add(dsAll.source.observeState()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    it.error?.let { throwable ->
                        errorHandler.handle(throwable)
                    }
                    viewState.handleState(it.type)
        }, {}))
        groupsDisposable.add(RxPagedListBuilder(dsAll, PAGINATION_PAGE_SIZE)
                .buildObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    viewState.groupListLoaded(it)
                }, {
                    errorHandler.handle(it)
                }))
    }

    private fun getFollowGroupsList() {
        groupsDisposable.add(dsSub.source.observeState()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    it.error?.let { throwable ->
                        errorHandler.handle(throwable)
                    }
                    viewState.handleState1(it.type)
                }, {}))

        groupsDisposable.add(RxPagedListBuilder(dsSub, PAGINATION_PAGE_SIZE)
                .buildObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    viewState.groupListSubLoaded(it)
                }, {
                    errorHandler.handle(it)
                }))
    }

    private fun getOwnedGroupsList() {
        groupsDisposable.add(dsAdm.source.observeState()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    it.error?.let { throwable ->
                        errorHandler.handle(throwable)
                    }
                    viewState.handleState2(it.type)
                }, {}))

        groupsDisposable.add(RxPagedListBuilder(dsAdm, PAGINATION_PAGE_SIZE)
                .buildObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .handleLoading(viewState)
                .subscribe({
                    viewState.groupListAdmLoaded(it)
                }, {
                    errorHandler.handle(it)
                }))
    }

    fun applySearchQuery(searchQuery:String){
        dsAll.source.applySearchFilter(searchQuery)
        dsSub.source.applySearchFilter(searchQuery)
        dsAdm.source.applySearchFilter(searchQuery)
        refresh()
    }

    fun reload() {
        dsAll.source.reload()
        dsSub.source.reload()
        dsAdm.source.reload()
    }

    fun refresh() {
        unsubscribe()
        groupList()
    }

    fun sub(groupId: String) {
        groupsDisposable.add(groupGateway.followGroup(groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    groupList()
                    Timber.d("Subscribed")
                }, {
                    errorHandler.handle(it)
                }))
    }

    fun unsub(groupId: String) {
        groupsDisposable.add(groupGateway.unfollowGroup(groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    groupList()
                    Timber.d("UnSubscribed")
                }, {
                    errorHandler.handle(it)
                }))
    }


    override fun onDestroy() {
        super.onDestroy()
        groupsDisposable.clear()
        stopImageUploading()
    }

    private fun unsubscribe() {
        groupsDisposable.clear()
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

    fun goOutFromProfile() {
        sessionStorage.logout()
    }


}
