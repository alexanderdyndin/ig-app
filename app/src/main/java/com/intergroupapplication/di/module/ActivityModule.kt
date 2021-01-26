package com.intergroupapplication.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */

interface ActivityModule<T : FragmentActivity> {
    fun provideActivity(activity: T): FragmentActivity = activity
}
