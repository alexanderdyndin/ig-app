package com.intergroupapplication.presentation.feature.commentsdetails.view

import androidx.paging.PagedList
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.base.CanShowLoading
import com.intergroupapplication.presentation.base.PagingView

@StateStrategyType(AddToEndSingleStrategy::class)
interface CommentsDetailsView : MvpView, CanShowLoading, PagingView {
    fun commentsLoaded(comments: PagedList<CommentEntity>)
    fun commentCreated(commentEntity: CommentEntity)
    fun answerToCommentCreated(commentEntity: CommentEntity)
    fun showPostDetailInfo(groupPostEntity: GroupPostEntity)
    fun showCommentUploading(show: Boolean)
    fun hideSwipeLayout()
    fun showMessage(value:Int)
}