package com.intergroupapplication.device.notification

/**
 * Created by abakarmagomedov on 26/09/2018 at project InterGroupApplication.
 */
sealed class CreatorType {
    class Comment(val postId: String, val name: String, val groupId: String, val commentId: String,
                  val notificationMessage: String) : CreatorType()
}