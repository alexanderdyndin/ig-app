package com.intergroupapplication.data.network

import io.reactivex.Completable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

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
