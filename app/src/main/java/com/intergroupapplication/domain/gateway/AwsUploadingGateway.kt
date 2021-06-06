package com.intergroupapplication.domain.gateway

import com.intergroupapplication.data.model.PhotoUploadFields
import com.intergroupapplication.presentation.base.ImageUploadingState
import io.reactivex.Observer
import org.reactivestreams.Subscriber
import java.io.File

interface AwsUploadingGateway {

    fun uploadAvatarToAws(uploadUrl: String, progressObserver: Observer<ImageUploadingState>,
                         fields: PhotoUploadFields,
                         uploadingFile: File)

    fun uploadImageToAws(uploadUrl: String, progressObserver: Observer<Float>,
                         fields: PhotoUploadFields,
                         uploadingFile: File)

    fun uploadVideoToAws(uploadUrl: String, progressObserver: Observer<Float>,
                         fields: PhotoUploadFields,
                         uploadingFile: File)

    fun uploadAudioToAws(uploadUrl: String, progressObserver: Observer<Float>,
                         fields: PhotoUploadFields,
                         uploadingFile: File)
}
