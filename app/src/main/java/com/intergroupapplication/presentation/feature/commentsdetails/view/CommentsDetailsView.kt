package com.intergroupapplication.presentation.feature.commentsdetails.view

import com.intergroupapplication.domain.entity.CommentEntity
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface CommentsDetailsView : MvpView{
    fun showPostDetailInfo(groupPostEntity: CommentEntity.PostEntity)
    fun hideSwipeLayout()
    fun showMessage(value:Int)
}