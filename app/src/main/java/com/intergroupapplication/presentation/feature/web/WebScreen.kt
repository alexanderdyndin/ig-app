package com.intergroupapplication.presentation.feature.web

import android.content.Context
import androidx.annotation.StringRes
import ru.terrakok.cicerone.android.support.SupportAppScreen

class WebScreen(private val path: String,
                @StringRes
                private val title: Int) : SupportAppScreen() {

    override fun getActivityIntent(context: Context) =
            WebActivity.getIntent(context, path, title)

}