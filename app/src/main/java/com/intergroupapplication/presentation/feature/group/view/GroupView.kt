package com.intergroupapplication.presentation.feature.group.view

import androidx.paging.PagedList
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.InfoForCommentEntity
import com.intergroupapplication.presentation.base.CanShowLoading
import com.intergroupapplication.domain.entity.UserRole
import com.intergroupapplication.presentation.base.BasePagingState
import com.intergroupapplication.presentation.base.ImageUploadingView
import com.intergroupapplication.presentation.base.PagingView

@StateStrategyType(AddToEndSingleStrategy::class)
interface GroupView : MvpView, CanShowLoading, PagingView, ImageUploadingView {
    fun postsLoaded(posts: PagedList<GroupPostEntity>)
    fun renderViewByRole(userRole: UserRole)
    fun showGroupInfo(groupEntity: GroupEntity)
    fun groupFollowed(followersCount: Int)
    fun groupUnfollowed(followersCount: Int)
    fun showSubscribeLoading(show: Boolean)
    fun groupFollowedError()
    fun groupUnfollowedError()
    fun showGroupInfoLoading(show: Boolean)
    fun changeCommentsCount(pair: Pair<String, String>)
    fun avatarChanged(url: String)
    fun showMessage(res:Int)
    fun showMessage(msg: String)
}