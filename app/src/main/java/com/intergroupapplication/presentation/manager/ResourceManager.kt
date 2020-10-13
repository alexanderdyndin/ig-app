package com.intergroupapplication.presentation.manager

import android.content.Context
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 31/07/2018 at project InterGroupApplication.
 */
class ResourceManager @Inject constructor(private val context: Context) {

    fun getString(id: Int): String = context.getString(id)
}
