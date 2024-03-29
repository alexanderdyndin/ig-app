package com.intergroupapplication.domain.gateway

import com.intergroupapplication.data.model.PhotoUploadFields
import io.reactivex.Observer
import java.io.File

interface AwsUploadingGateway {
    fun uploadImageToAws(
        uploadUrl: String, progressObserver: Observer<Float>,
        fields: PhotoUploadFields,
        uploadingFile: File
    )

    fun uploadVideoToAws(
        uploadUrl: String, progressObserver: Observer<Float>,
        fields: PhotoUploadFields,
        uploadingFile: File
    )

    fun uploadAudioToAws(
        uploadUrl: String, progressObserver: Observer<Float>,
        fields: PhotoUploadFields,
        uploadingFile: File
    )
}
