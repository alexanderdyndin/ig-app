package com.intergroupapplication.data.service

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.OkHttpResponseListener
import com.intergroupapplication.data.model.PhotoUploadFields
import com.intergroupapplication.domain.exception.ImageUploadingException
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import io.reactivex.Observer
import okhttp3.Response
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject

class AwsUploadingService @Inject constructor() : AwsUploadingGateway {

    override fun uploadImageToAws(uploadUrl: String, progressObserver: Observer<Float>,
                                  fields: PhotoUploadFields,
                                  uploadingFile: File) {
        AndroidNetworking.upload(uploadUrl)
                .addMultipartParameter(
                        mutableMapOf("AWSAccessKeyId" to fields.awsAccessKeyId,
                                "key" to fields.key,
                                "policy" to fields.policy,
                                "signature" to fields.signature))
                .addMultipartFile("file", uploadingFile)
                .setPriority(Priority.HIGH)
                .setPercentageThresholdForCancelling(50)
                .setExecutor(Executors.newSingleThreadExecutor())
                .build()
                .setUploadProgressListener { bytesUploaded, totalBytes ->
                    progressObserver.onNext(((100 * bytesUploaded / totalBytes).toFloat()))
                }
                .getAsOkHttpResponse(object : OkHttpResponseListener {
                    override fun onResponse(response: Response) {
                        if (response.isSuccessful) {
                            progressObserver.onComplete()
                        } else {
                            progressObserver.onError(ImageUploadingException())
                        }
                    }

                    override fun onError(anError: ANError) {
                        progressObserver.onError(anError)
                    }
                })
    }
}
