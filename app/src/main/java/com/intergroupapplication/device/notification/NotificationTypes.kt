package com.intergroupapplication.device.notification

/**
 * Created by abakarmagomedov on 04/09/2018 at project InterGroupApplication.
 */
enum class NotificationTypes(val value: String) {

    NEW_COMMENT("newComment");

    companion object {

        fun map(value: String): NotificationTypes? = when (value) {
            "newComment" -> NEW_COMMENT
            else -> null
        }

    }

}