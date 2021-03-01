package com.intergroupapplication.presentation.feature.commentsdetails.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.GroupPostEntity

@StateStrategyType(AddToEndSingleStrategy::class)
interface CommentsDetailsView : MvpView {
    fun commentCreated(commentEntity: CommentEntity)
    fun answerToCommentCreated(commentEntity: CommentEntity)
    fun showPostDetailInfo(groupPostEntity: GroupPostEntity)
    fun showCommentUploading(show: Boolean)
    fun hideSwipeLayout()
    fun showMessage(value:Int)
}