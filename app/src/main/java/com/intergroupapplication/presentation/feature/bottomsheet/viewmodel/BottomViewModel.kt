package com.intergroupapplication.presentation.feature.bottomsheet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.intergroupapplication.data.repository.BottomSheetRepository
import io.reactivex.Flowable
import timber.log.Timber
import javax.inject.Inject

class BottomViewModel @Inject constructor(private val bottomsSheetRepository: BottomSheetRepository):ViewModel() {

    fun fetchComments(key:Int): Flowable<PagingData<String>> {
        return bottomsSheetRepository
                .getMedia(key)
                .cachedIn(viewModelScope)
    }
}