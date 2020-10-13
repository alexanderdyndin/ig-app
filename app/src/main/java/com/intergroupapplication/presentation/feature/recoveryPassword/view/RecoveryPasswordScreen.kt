package com.intergroupapplication.presentation.feature.recoveryPassword.view

import android.content.Context
import ru.terrakok.cicerone.android.support.SupportAppScreen

class RecoveryPasswordScreen : SupportAppScreen() {

    override fun getActivityIntent(context: Context) =
            RecoveryPasswordActivity.getIntent(context)

}