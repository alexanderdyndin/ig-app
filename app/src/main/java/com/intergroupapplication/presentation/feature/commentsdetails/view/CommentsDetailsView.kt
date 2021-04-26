package com.intergroupapplication.presentation.feature.commentsdetails.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.base.ImageUploadingView
import com.intergroupapplication.presentation.feature.bottomsheet.view.BottomSheetFragment

@StateStrategyType(AddToEndSingleStrategy::class)
interface CommentsDetailsView : MvpView,ImageUploadingView{
    fun showPostDetailInfo(groupPostEntity: GroupPostEntity.PostEntity)
    fun hideSwipeLayout()
    fun showMessage(value:Int)
}