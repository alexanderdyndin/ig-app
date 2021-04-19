package com.intergroupapplication.presentation.feature.userlist.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.intergroupapplication.domain.entity.GroupUserEntity
import com.intergroupapplication.domain.usecase.GroupUseCase
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

class UserListViewModel @Inject constructor(
        private val useCase: GroupUseCase
) : ViewModel() {

    fun getFollowers(groupId: String): Flowable<PagingData<GroupUserEntity>> {
        return useCase.getGroupFollowers(groupId)
                .cachedIn(viewModelScope)
    }

    fun getAdministrators(groupId: String): Flowable<PagingData<GroupUserEntity>> {
        return useCase.getGroupAdministrators(groupId)
                .cachedIn(viewModelScope)
    }

    fun getBans(groupId: String): Flowable<PagingData<GroupUserEntity>> {
        return useCase.getGroupBans(groupId)
                .cachedIn(viewModelScope)
    }

    fun setUserBans(userId: String, reason: String, groupId: String): Completable {
        return useCase.banUserInGroup(userId, reason, groupId)
    }

    fun deleteUserFromBansGroup(userId: String): Completable {
        return useCase.deleteUserFromBansGroup(userId)
    }

    fun assignToAdmins(subscriptionId: String) =
            useCase.updateGroupAdmin(subscriptionId, true)

    fun demoteFromAdmins(subscriptionId: String) =
            useCase.updateGroupAdmin(subscriptionId, false)

}