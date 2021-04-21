package com.intergroupapplication.presentation.feature.commentsdetails.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.base.ImageUploadingView

@StateStrategyType(AddToEndSingleStrategy::class)
interface CommentsDetailsView : MvpView,ImageUploadingView {
    fun commentCreated(commentEntity: CommentEntity)
    fun answerToCommentCreated(commentEntity: CommentEntity)
    fun showPostDetailInfo(groupPostEntity: GroupPostEntity.PostEntity)
    fun showCommentUploading(show: Boolean)
    fun hideSwipeLayout()
    fun showMessage(value:Int)
}