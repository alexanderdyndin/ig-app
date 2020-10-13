package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class PhotoUploadFields(
        val key: String,
        @SerializedName("AWSAccessKeyId") val awsAccessKeyId: String,
        val policy: String,
        val signature: String
)