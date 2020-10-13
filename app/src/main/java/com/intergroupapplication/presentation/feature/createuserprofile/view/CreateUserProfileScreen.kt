package com.intergroupapplication.presentation.feature.createuserprofile.view

import android.content.Context
import ru.terrakok.cicerone.android.support.SupportAppScreen

class CreateUserProfileScreen : SupportAppScreen() {

    override fun getActivityIntent(context: Context) =
            CreateUserProfileActivity.getIntent(context)

}