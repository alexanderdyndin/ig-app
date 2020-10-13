package com.intergroupapplication.presentation.feature.confirmationmail.view

import android.content.Context
import android.content.Intent
import com.intergroupapplication.domain.entity.RegistrationEntity
import ru.terrakok.cicerone.android.support.SupportAppScreen

class ConfirmationMailScreen(private val entity: String) : SupportAppScreen() {

    override fun getActivityIntent(context: Context) =
            ConfirmationMailActivity.getIntent(context, entity)

}