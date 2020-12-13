package com.intergroupapplication.presentation.feature.mediaPlayer

import android.content.Context
import android.content.Intent


class MediaServiceIntentBuilder(private val context: Context) {
    var message: String? = null
    var commandId: Int? = null

    companion object {
        private const val INTENT_KEY = "key"
    }

    fun setMessage(message: String): MediaServiceIntentBuilder {
        this.message = message
        return this
    }

    fun setCommand(command: Int): MediaServiceIntentBuilder {
        this.commandId = command
        return this
    }

    fun build(): Intent {

        val intent = Intent(context, IGMediaService::class.java)

        if (message != null) {
            intent.putExtra(INTENT_KEY, message)
        }
        return intent
    }
}