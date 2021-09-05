package com.intergroupapplication.di.module

import androidx.fragment.app.Fragment

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */

interface FragmentModule<T : Fragment> {
    fun provideFragment(fragment: T): Fragment = fragment
}
