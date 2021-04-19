package com.intergroupapplication.domain.gateway

import androidx.paging.PagingData
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import io.reactivex.Flowable

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
interface MediaGateway {
    fun getAudioList(): Flowable<PagingData<AudioEntity>>
    fun getVideoList(): Flowable<PagingData<FileEntity>>
    fun getImageList(): Flowable<PagingData<FileEntity>>
}
