package com.intergroupapplication.presentation.feature.createpost.view

import android.content.Context
import android.content.Intent
import ru.terrakok.cicerone.android.support.SupportAppScreen

class CreatePostScreen(private val groupId: String) : SupportAppScreen() {

    override fun getActivityIntent(context: Context) =
            CreatePostActivity.getIntent(context, groupId)

}