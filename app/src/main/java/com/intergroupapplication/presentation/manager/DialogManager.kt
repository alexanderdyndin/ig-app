package com.intergroupapplication.presentation.manager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Created by abakarmagomedov on 02/08/2018 at project InterGroupApplication.
 */
class DialogManager(private val fragmentManager: FragmentManager) {

    fun getManager(): FragmentManager = fragmentManager


    fun findFragmentByTag(tag: String): Fragment? =
            fragmentManager.findFragmentByTag(tag)

}
