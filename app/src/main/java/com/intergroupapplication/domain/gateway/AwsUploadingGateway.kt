package com.intergroupapplication.domain.gateway

import com.intergroupapplication.data.model.PhotoUploadFields
import io.reactivex.Observer
import org.reactivestreams.Subscriber
import java.io.File

interface AwsUploadingGateway {
    fun uploadImageToAws(uploadUrl: String, progressObserver: Observer<Float>,
                         fields: PhotoUploadFields,
                         uploadingFile: File)
}
