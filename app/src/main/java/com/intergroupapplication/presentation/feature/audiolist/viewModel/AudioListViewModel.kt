package com.intergroupapplication.presentation.feature.audiolist.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.usecase.MediaUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class AudioListViewModel @Inject constructor(private val useCase: MediaUseCase): ViewModel() {

    fun getAudios(): Flowable<PagingData<AudioEntity>> {
        return useCase.getAudio()
                .cachedIn(viewModelScope)
    }

}