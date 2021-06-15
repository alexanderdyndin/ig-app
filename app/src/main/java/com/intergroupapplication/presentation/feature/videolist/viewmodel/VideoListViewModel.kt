package com.intergroupapplication.presentation.feature.videolist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.usecase.MediaUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class VideoListViewModel @Inject constructor(private val useCase: MediaUseCase):ViewModel() {

    fun getVideos(): Flowable<PagingData<FileEntity>>{
        return useCase.getVideo()
            .cachedIn(viewModelScope)
    }
}