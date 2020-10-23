package com.intergroupapplication.data.model

import com.google.gson.annotations.SerializedName

data class PhotoUploadFields(
        val key: String,
        //@SerializedName("AWSAccessKeyId") val awsAccessKeyId: String,
        @SerializedName("x-amz-algorithm") val algorithm: String,
        @SerializedName("x-amz-signature") val signature: String,
        @SerializedName("x-amz-credential") val credential: String,
        @SerializedName("x-amz-date") val date: String,
        val policy: String,
)