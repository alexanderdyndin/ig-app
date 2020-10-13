package com.intergroupapplication.presentation.feature.navigation.view

import android.content.Context
import android.content.Intent
import ru.terrakok.cicerone.android.support.SupportAppScreen

class NavigationScreen : SupportAppScreen() {

    override fun getActivityIntent(context: Context) =
            NavigationActivity.getIntent(context)

}