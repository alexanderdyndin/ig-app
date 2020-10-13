package com.intergroupapplication.device.notification.actions

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import com.intergroupapplication.R
import com.intergroupapplication.data.model.NotificationCommentModel
import com.intergroupapplication.device.notification.notificationcreators.NotificationCreator
import com.intergroupapplication.device.notification.CreatorType

/**
 * Created by abakarmagomedov on 05/09/2018 at project InterGroupApplication.
 */
class OnNewCommentAction(
        private val notificationCreator: NotificationCreator<CreatorType.Comment>,
        private val notificationManager: NotificationManager,
        private val context: Context) : NotificationAction<RemoteMessage> {

    override fun proceed(action: RemoteMessage) {
        val map = action.data
        val postId = map["post_id"] ?: ""
        val name = map["name"] ?: ""
        val groupId = map["group_id"] ?: ""
        val commentId = map["comment_id"] ?: ""
        val message = map["push_message"] ?: context.getString(R.string.intergroup_notification)
        val commentModel = NotificationCommentModel(postId, name, groupId, commentId, message)
        onNewComment(commentModel)
    }

    private fun onNewComment(comment: NotificationCommentModel) {
        val type = CreatorType.Comment(
                comment.postId,
                comment.name,
                comment.groupId,
                comment.commentId,
                comment.message
        )
        sendNotification(notificationCreator.create(type), type.commentId.toInt())
    }

    private fun sendNotification(notification: Notification, notificationId: Int) {
        notificationManager.notify(notificationId, notification)
    }

}