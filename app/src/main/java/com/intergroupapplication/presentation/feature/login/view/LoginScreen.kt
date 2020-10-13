package com.intergroupapplication.presentation.feature.login.view

import android.content.Context
import android.content.Intent
import ru.terrakok.cicerone.android.support.SupportAppScreen

class LoginScreen : SupportAppScreen() {

    override fun getActivityIntent(context: Context) =
            LoginActivity.getIntent(context)

}