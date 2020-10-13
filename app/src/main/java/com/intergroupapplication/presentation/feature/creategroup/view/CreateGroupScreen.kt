package com.intergroupapplication.presentation.feature.creategroup.view

import android.content.Context
import android.content.Intent
import ru.terrakok.cicerone.android.support.SupportAppScreen

class CreateGroupScreen : SupportAppScreen() {

    override fun getActivityIntent(context: Context) =
            CreateGroupActivity.getIntent(context)

}