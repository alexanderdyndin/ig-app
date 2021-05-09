package com.intergroupapplication.presentation.feature.userlist.addBlackListById

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.intergroupapplication.domain.usecase.GroupUseCase
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

class AddBlackListByIdViewModel @Inject constructor(
        private val useCase: GroupUseCase
) : ViewModel() {

    fun getUsers(groupId: String, searchFilter: String) : Flowable<PagingData<AddBlackListUserItem>> {
        return useCase.getFollowersBanIds(groupId, searchFilter)
                .cachedIn(viewModelScope)
    }

    fun banUserInGroup(userId: String, reason: String, groupId: String): Completable {
        return useCase.banUserInGroup(userId, reason, groupId)
    }
}