package com.intergroupapplication.presentation.feature.grouplist.viewModel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.paging.rxjava2.cachedIn
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.usecase.GroupUseCase
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import com.intergroupapplication.presentation.feature.grouplist.other.GroupEntityUI
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


    fun fetchGroups(query: String = ""): Flowable<PagingData<GroupEntityUI>> {
        return useCase.getGroupList(query)
                .map { pagingData ->
                    var i = -GroupListAdapter.AD_FIRST - 1
                    pagingData.map {
                        GroupEntityUI.GroupEntity (
                                it.id,
                                it.followersCount,
                                it.postsCount,
                                it.postsLikes,
                                it.postsDislikes,
                                it.CommentsCount,
                                it.timeBlocked,
                                it.name,
                                it.description,
                                it.isBlocked,
                                it.owner,
                                it.isFollowing,
                                it.avatar,
                                it.subject,
                                it.rules,
                                it.isClosed,
                                it.ageRestriction
                                )
                    }
                            .insertSeparators<GroupEntityUI.GroupEntity, GroupEntityUI>
                            { before: GroupEntityUI.GroupEntity?, after: GroupEntityUI.GroupEntity? ->
                                i++
                                when {
                                    before == null -> null
                                    after == null -> null
                                    else -> if ( i % GroupListAdapter.AD_FREQ == 0 && i >= 0) {
                                        GroupEntityUI.AdEntity(i)
                                    } else {
                                        null
                                    }
                                }
                            }
                }
                .cachedIn(viewModelScope)
    }

    fun fetchSubGroups(query: String = ""): Flowable<PagingData<GroupEntityUI>> {
        return useCase.getSubscribedGroupList(query)
                .map { pagingData ->
                    var i = -GroupListAdapter.AD_FIRST - 1
                    pagingData.map {
                        GroupEntityUI.GroupEntity (
                                it.id,
                                it.followersCount,
                                it.postsCount,
                                it.postsLikes,
                                it.postsDislikes,
                                it.CommentsCount,
                                it.timeBlocked,
                                it.name,
                                it.description,
                                it.isBlocked,
                                it.owner,
                                it.isFollowing,
                                it.avatar,
                                it.subject,
                                it.rules,
                                it.isClosed,
                                it.ageRestriction
                        )
                    }
                            .insertSeparators<GroupEntityUI.GroupEntity, GroupEntityUI>
                            { before: GroupEntityUI.GroupEntity?, after: GroupEntityUI.GroupEntity? ->
                                i++
                                when {
                                    before == null -> null
                                    after == null -> null
                                    else -> if ( i % GroupListAdapter.AD_FREQ == 0 && i >= 0) {
                                        GroupEntityUI.AdEntity(i)
                                    } else {
                                        null
                                    }
                                }
                            }
                }
                .cachedIn(viewModelScope)
    }

    fun fetchAdmGroups(query: String = "") : Flowable<PagingData<GroupEntityUI>> {
        return useCase.getAdminGroupList(query)
                .map { pagingData ->
                    var i = -GroupListAdapter.AD_FIRST - 1
                    pagingData.map {
                        GroupEntityUI.GroupEntity (
                                it.id,
                                it.followersCount,
                                it.postsCount,
                                it.postsLikes,
                                it.postsDislikes,
                                it.CommentsCount,
                                it.timeBlocked,
                                it.name,
                                it.description,
                                it.isBlocked,
                                it.owner,
                                it.isFollowing,
                                it.avatar,
                                it.subject,
                                it.rules,
                                it.isClosed,
                                it.ageRestriction
                        )
                    }
                            .insertSeparators<GroupEntityUI.GroupEntity, GroupEntityUI>
                            { before: GroupEntityUI.GroupEntity?, after: GroupEntityUI.GroupEntity? ->
                                i++
                                when {
                                    before == null -> null
                                    after == null -> null
                                    else -> if ( i % GroupListAdapter.AD_FREQ == 0 && i >= 0) {
                                        GroupEntityUI.AdEntity(i)
                                    } else {
                                        null
                                    }
                                }
                            }
                }
                .cachedIn(viewModelScope)
    }

    fun subscribeGroup(groupID: String): Completable {
        return useCase.subscribeGroup(groupID)
    }

    fun unsubscribeGroup(groupID: String): Completable {
        return useCase.unsubscribeGroup(groupID)
    }




}