package com.intergroupapplication.presentation.feature.commentsdetails.pagingsource

import androidx.paging.DataSource
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import javax.inject.Inject

class CommentsDataSourceFactory @Inject constructor(val source: CommentsDataSource) :
        DataSource.Factory<Int, CommentEntity>() {
    override fun create(): DataSource<Int, CommentEntity> = source
}