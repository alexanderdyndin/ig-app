package com.intergroupapplication.presentation.feature.splash

import android.content.Context
import ru.terrakok.cicerone.android.support.SupportAppScreen

class SplashScreen : SupportAppScreen() {

    override fun getActivityIntent(context: Context) = SplashActivity.getIntent(context)
}