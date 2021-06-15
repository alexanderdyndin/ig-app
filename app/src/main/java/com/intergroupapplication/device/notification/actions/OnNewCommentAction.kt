package com.intergroupapplication.device.notification.actions

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import com.intergroupapplication.R
import com.intergroupapplication.data.model.NotificationCommentModel
import com.intergroupapplication.device.notification.notificationcreators.NotificationCreator
import com.intergroupapplication.device.notification.CreatorType
import timber.log.Timber

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
        val page = map["page"] ?: "1"
        val commentId = map["comment_id"] ?: "228"
        val message = map["push_message"] ?: context.getString(R.string.intergroup_notification)
        val commentModel = NotificationCommentModel(postId, name, groupId, commentId, message, page)
        onNewComment(commentModel)
    }

    private fun onNewComment(comment: NotificationCommentModel) {
        val type = CreatorType.Comment(
                comment.postId,
                comment.name,
                comment.groupId,
                comment.commentId,
                comment.message,
                comment.page
        )
        sendNotification(notificationCreator.create(type),
            createNotificationId(comment.postId.toInt()*10,comment.message))
    }

    private fun sendNotification(notification: Notification, notificationId: Int) {
        notificationManager.cancel(notificationId)
        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationId(postId:Int,message:String):Int{
        return when {
            message.contains(context.getString(R.string.answer_comment)) -> {
                postId
            }
            message.contains(context.getString(R.string.dislike_your_comment)) -> {
                postId + 1
            }
            message.contains(context.getString(R.string.like_your_comment)) -> {
                postId + 2
            }
            message.contains(context.getString(R.string.have_new_comment)) -> {
                postId + 3
            }
            else -> postId
        }

    }

}