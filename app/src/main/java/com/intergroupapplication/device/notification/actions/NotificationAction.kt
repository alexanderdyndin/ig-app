package com.intergroupapplication.device.notification.actions

/**
 * Created by abakarmagomedov on 27/09/2018 at project InterGroupApplication.
 */
interface NotificationAction<ACTION> {
    fun proceed(action: ACTION)
}