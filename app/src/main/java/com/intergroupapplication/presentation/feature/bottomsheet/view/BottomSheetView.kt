package com.intergroupapplication.presentation.feature.bottomsheet.view

import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.base.ImageUploadingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface BottomSheetView: MvpView, ImageUploadingView{
    fun commentCreated(commentEntity: CommentEntity)
    fun answerToCommentCreated(commentEntity: CommentEntity)
    fun showCommentUploading(show: Boolean)
    fun hideSwipeLayout()
}