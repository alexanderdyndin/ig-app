package com.intergroupapplication.presentation.feature.commentsdetails.view

import android.content.Context
import android.content.Intent
import com.intergroupapplication.domain.entity.InfoForCommentEntity
import ru.terrakok.cicerone.android.support.SupportAppScreen

class CommentsDetailsScreen(private val entity: InfoForCommentEntity) : SupportAppScreen() {

    override fun getActivityIntent(context: Context): Intent? =
            CommentsDetailsActivity.getIntent(context, entity)
}