package com.intergroupapplication.presentation.feature.news.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.feature.news.pagingsource.NewsRepository
import io.reactivex.Flowable
import javax.inject.Inject

class NewsViewModel @Inject constructor(): ViewModel() {

    val s = "12345"

}