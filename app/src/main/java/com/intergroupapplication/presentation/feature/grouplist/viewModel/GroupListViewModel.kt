package com.intergroupapplication.presentation.feature.grouplist.viewModel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.paging.rxjava2.cachedIn
import com.appodeal.ads.Appodeal
import com.appodeal.ads.NativeAd
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.usecase.GroupUseCase
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import io.reactivex.Completable
import io.reactivex.Flowable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject

class GroupListViewModel @Inject constructor(
    private val useCase: GroupUseCase
) : ViewModel() {

    private val nativeAdItem: NativeAd?
        get() {
            val ads = Appodeal.getNativeAds(1)
            return if (ads.isNotEmpty()) ads[0] else null
        }

    @SuppressLint("BinaryOperationInTimber")
    @ExperimentalCoroutinesApi
    fun fetchGroups(query: String = ""): Flowable<PagingData<GroupEntity>> {
        return useCase.getGroupList(query)
            .map { pagingData ->
                var i = -GroupListAdapter.AD_FIRST - 1
                pagingData.map {
                    it as GroupEntity.Group
                }
                    .insertSeparators<GroupEntity.Group, GroupEntity>
                    { before: GroupEntity.Group?, after: GroupEntity.Group? ->
                        i++
                        when {
                            before == null -> null
                            after == null -> null
                            else -> if (i % GroupListAdapter.AD_FREQ == 0 && i >= 0) {
                                var nativeAd: NativeAd?
                                Timber.d(
                                    "trying to get all group list ad, avaible ad:" +
                                            "${Appodeal.getAvailableNativeAdsCount()}"
                                )
                                if (nativeAdItem.also { nativeAd = it } != null) {
                                    GroupEntity.AdEntity(i, nativeAd, "all_groups")
                                } else null
                            } else null
                        }
                    }
            }
            .cachedIn(viewModelScope)
    }

    @ExperimentalCoroutinesApi
    fun fetchSubGroups(query: String = ""): Flowable<PagingData<GroupEntity>> {
        return useCase.getSubscribedGroupList(query)
            .map { pagingData ->
                var i = -GroupListAdapter.AD_FIRST - 1
                pagingData.map {
                    it as GroupEntity.Group
                }
                    .insertSeparators<GroupEntity.Group, GroupEntity>
                    { before: GroupEntity.Group?, after: GroupEntity.Group? ->
                        i++
                        when {
                            before == null -> null
                            after == null -> null
                            else -> if (i % GroupListAdapter.AD_FREQ == 0 && i >= 0) {
                                if (Appodeal.getAvailableNativeAdsCount() > 0)
                                    GroupEntity.AdEntity(
                                        i, null,
                                        "subscribed_groups"
                                    )
                                else null
                            } else null
                        }
                    }
            }
            .cachedIn(viewModelScope)
    }

    @ExperimentalCoroutinesApi
    fun fetchAdmGroups(query: String = ""): Flowable<PagingData<GroupEntity>> {
        return useCase.getAdminGroupList(query)
            .map { pagingData ->
                var i = -GroupListAdapter.AD_FIRST - 1
                pagingData.map {
                    it as GroupEntity.Group
                }
                    .insertSeparators<GroupEntity.Group, GroupEntity>
                    { before: GroupEntity.Group?, after: GroupEntity.Group? ->
                        i++
                        when {
                            before == null -> null
                            after == null -> null
                            else -> if (i % GroupListAdapter.AD_FREQ == 0 && i >= 0) {
                                if (Appodeal.getAvailableNativeAdsCount() > 0)
                                    GroupEntity.AdEntity(i, null, "admin_groups")
                                else null
                            } else null
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
