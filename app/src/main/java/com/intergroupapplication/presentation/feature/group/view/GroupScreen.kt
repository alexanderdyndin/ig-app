package com.intergroupapplication.presentation.feature.group.view

import android.content.Context
import android.content.Intent
import ru.terrakok.cicerone.android.support.SupportAppScreen

class GroupScreen(private val groupId: String) : SupportAppScreen() {

    override fun getActivityIntent(context: Context) =
            GroupActivity.getIntent(context, groupId)

}