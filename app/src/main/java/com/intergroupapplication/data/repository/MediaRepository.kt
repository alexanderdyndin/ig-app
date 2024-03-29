package com.intergroupapplication.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.intergroupapplication.data.mappers.MediaMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.network.PAGE_SIZE
import com.intergroupapplication.data.remotedatasource.AudiosRemoteRXDataSource
import com.intergroupapplication.data.remotedatasource.ImagesRemoteRXDataSource
import com.intergroupapplication.data.remotedatasource.VideosRemoteRXDataSource
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.gateway.MediaGateway
import io.reactivex.Flowable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
@ExperimentalCoroutinesApi
class MediaRepository @Inject constructor(
    private val api: AppApi,
    private val mediaMapper: MediaMapper
) : MediaGateway {

    override fun getAudioList(): Flowable<PagingData<AudioEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = 40,
                prefetchDistance = 5
            ),
            pagingSourceFactory = { AudiosRemoteRXDataSource(api, mediaMapper) }
        ).flowable
    }

    override fun getVideoList(): Flowable<PagingData<FileEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = 40,
                prefetchDistance = 5
            ),
            pagingSourceFactory = { VideosRemoteRXDataSource(api, mediaMapper) }
        ).flowable
    }

    override fun getImageList(): Flowable<PagingData<FileEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = 40,
                prefetchDistance = 5
            ),
            pagingSourceFactory = { ImagesRemoteRXDataSource(api, mediaMapper) }
        ).flowable
    }
}
