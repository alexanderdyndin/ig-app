package com.intergroupapplication.presentation.feature.news.view

import androidx.paging.PagedList
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.UserEntity
import com.intergroupapplication.presentation.base.*

@StateStrategyType(AddToEndSingleStrategy::class)
interface NewsView : MvpView, ImageUploadingView {
    fun showMessage(resId: Int)
    fun showMessage(msg: String)
    fun showUserInfo(userEntity: UserEntity)
    fun avatarChanged(url: String)
    fun showLastAvatar(lastAvatar: String?)
}
