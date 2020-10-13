package com.intergroupapplication.presentation.feature.grouplist.view

import ru.terrakok.cicerone.android.support.SupportAppScreen

class GroupListScreen : SupportAppScreen() {

    override fun getFragment() = GroupListFragment.getInstance()

}