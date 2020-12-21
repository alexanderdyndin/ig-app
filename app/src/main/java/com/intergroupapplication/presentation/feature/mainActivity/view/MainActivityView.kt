package com.intergroupapplication.presentation.feature.mainActivity.view

import androidx.paging.PagedList
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.UserEntity
import com.intergroupapplication.presentation.base.CanShowLoading
import com.intergroupapplication.presentation.base.ImageUploadingView
import com.intergroupapplication.presentation.base.PagingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface MainActivityView : MvpView {

}