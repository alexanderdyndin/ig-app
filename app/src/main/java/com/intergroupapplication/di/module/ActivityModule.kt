package com.intergroupapplication.di.module

import androidx.appcompat.app.AppCompatActivity

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */

interface ActivityModule<T : AppCompatActivity> {
    fun provideActivity(activity: T): AppCompatActivity = activity
}
