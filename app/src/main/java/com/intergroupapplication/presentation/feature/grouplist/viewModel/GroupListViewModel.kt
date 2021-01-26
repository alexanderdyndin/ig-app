package com.intergroupapplication.presentation.feature.grouplist.viewModel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.rxjava2.cachedIn
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.usecase.GroupUseCase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class GroupListViewModel @Inject constructor(
        private val useCase: GroupUseCase,
        private val compositeDisposable: CompositeDisposable
): ViewModel() {

    val query: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val searchQuery: MutableStateFlow<String> by lazy {
        MutableStateFlow<String>("")
    }

    val scrollingRecycler: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }


    fun fetchGroups(query: String = ""): Flowable<PagingData<GroupEntity>> {
        return useCase.getGroupList(query).cachedIn(viewModelScope)
    }

    fun fetchSubGroups(query: String = ""): Flowable<PagingData<GroupEntity>> {
        return useCase.getSubscribedGroupList(query).cachedIn(viewModelScope)
    }

    fun fetchAdmGroups(query: String = "") : Flowable<PagingData<GroupEntity>> {
        return useCase.getAdminGroupList(query).cachedIn(viewModelScope)
    }

    fun subscribeGroup(groupID: String): Completable {
        return useCase.subscribeGroup(groupID)
    }

    fun unsubscribeGroup(groupID: String): Completable {
        return useCase.unsubscribeGroup(groupID)
    }




}