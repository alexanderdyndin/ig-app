package com.intergroupapplication.data.network

import io.reactivex.Completable
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.http.*

interface AmazonApi {

    @Multipart
    @POST("")
    fun uploadImageToAmazon(@Url url: String,
                            @Part("AWSAccessKeyId") awsKey: RequestBody,
                            @Part("key") key: RequestBody,
                            @Part("policy") policy: RequestBody,
                            @Part("signature") signature: RequestBody,
                            @Part filePart: MultipartBody.Part): Completable
}
