package com.intergroupapplication.presentation.feature.news.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.rxjava2.cachedIn
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.usecase.NewsUseCase
import io.reactivex.Flowable
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NewsViewModel @Inject constructor(private val useCase: NewsUseCase): ViewModel() {


    fun getNews(): Flowable<PagingData<GroupPostEntity>> {
        return useCase
                .getNewsPosts()
                .cachedIn(viewModelScope)
    }

}