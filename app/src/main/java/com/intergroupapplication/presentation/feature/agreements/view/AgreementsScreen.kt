package com.intergroupapplication.presentation.feature.agreements.view

import android.content.Context
import android.content.Intent
import ru.terrakok.cicerone.android.support.SupportAppScreen

class AgreementsScreen : SupportAppScreen() {

    override fun getActivityIntent(context: Context) = AgreementsActivity.getIntent(context)
}