package com.intergroupapplication.presentation.feature.addBlackListById.viewmodel

import androidx.lifecycle.ViewModel
import com.intergroupapplication.domain.usecase.GroupUseCase
import com.intergroupapplication.data.model.AddBlackListUserModel
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class AddBlackListByIdViewModel @Inject constructor(
        private val useCase: GroupUseCase
) : ViewModel() {

    fun getUsers(groupId: String, searchFilter: String) : Single<List<AddBlackListUserModel>> {
        return useCase.getGroupFollowersForSearch(groupId, searchFilter)
    }

    fun banUserInGroup(userId: String, reason: String, groupId: String): Completable {
        return useCase.banUserInGroup(userId, reason, groupId)
    }
}