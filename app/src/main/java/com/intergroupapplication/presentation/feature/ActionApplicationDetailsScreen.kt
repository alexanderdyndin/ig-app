package com.intergroupapplication.presentation.feature

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import ru.terrakok.cicerone.android.support.SupportAppScreen

class ActionApplicationDetailsScreen : SupportAppScreen() {

    override fun getActivityIntent(context: Context): Intent? = Intent().apply{
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", context.packageName, null)
    }

}