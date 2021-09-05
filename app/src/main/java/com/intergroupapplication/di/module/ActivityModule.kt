package com.intergroupapplication.di.module

import android.app.Activity

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */

interface ActivityModule<T : Activity> {
    fun provideActivity(activity: T): Activity = activity
}
