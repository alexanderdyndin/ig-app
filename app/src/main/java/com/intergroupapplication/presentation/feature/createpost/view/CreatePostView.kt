package com.intergroupapplication.presentation.feature.createpost.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.base.CanShowLoading

@StateStrategyType(AddToEndSingleStrategy::class)
interface CreatePostView : MvpView, CanShowLoading {
    fun postCreateSuccessfully(postEntity: GroupPostEntity.PostEntity)
}