package com.intergroupapplication.presentation.feature.userlist.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.rxjava2.cachedIn
import com.intergroupapplication.domain.usecase.GroupUseCase
import com.intergroupapplication.presentation.feature.userlist.adapter.UserListEntityUI
import io.reactivex.Flowable
import javax.inject.Inject

class UserListViewModel @Inject constructor(
        private val useCase: GroupUseCase
): ViewModel() {

    fun getUsers(groupId: String): Flowable<PagingData<UserListEntityUI>> {
        return useCase.getGroupFollowers(groupId)
                .map { pagingData ->
                    pagingData.map {
                        // todo в маппер
                        UserListEntityUI(
                                name = it.firstName
                        )
                    }
                }.cachedIn(viewModelScope)
    }

}