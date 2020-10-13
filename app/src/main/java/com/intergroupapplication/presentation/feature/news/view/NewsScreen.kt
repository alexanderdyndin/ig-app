package com.intergroupapplication.presentation.feature.news.view

import androidx.fragment.app.Fragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

class NewsScreen : SupportAppScreen() {

    override fun getFragment(): Fragment = NewsFragment.getInstance()

}