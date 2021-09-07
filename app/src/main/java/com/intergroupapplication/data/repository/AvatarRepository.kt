package com.intergroupapplication.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.androidnetworking.AndroidNetworking
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.gateway.AvatarGateway
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.presentation.base.ImageUploadingState
import id.zelory.compressor.Compressor
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import java.io.File
import javax.inject.Inject


/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class AvatarRepository @Inject constructor(
    private val context: Context,
    private val appApi: AppApi,
    private val awsUploadingGateway: AwsUploadingGateway
) : AvatarGateway {
    companion object {
        const val FULL_UPLOADED_PROGRESS = 100F
    }

    override fun uploadToAws(path: String, groupId: String?): Flowable<ImageUploadingState> {
        val subject = PublishSubject.create<Float>()
        val file = File(path)
        return appApi.uploadPostsMedia(file.extension, groupId)
            .doAfterSuccess {
                if (file.extension == "gif")
                    awsUploadingGateway.uploadImageToAws(
                        it.url, subject, it.fields,
                        file
                    )
                else
                    awsUploadingGateway.uploadImageToAws(
                        it.url, subject, it.fields,
                        Compressor(context).setQuality(75)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG).compressToFile(file)
                    )
            }
            .flatMapObservable {
                subject.map { progress ->
                    when (progress) {
                        FULL_UPLOADED_PROGRESS -> {
                            ImageUploadingState.ImageUploaded(it.fields.key)
                        }
                        else -> {
                            ImageUploadingState.ImageUploadingProgress(progress)
                        }
                    }
                }
                    .doOnDispose { AndroidNetworking.cancelAll() }
            }
            .toFlowable(BackpressureStrategy.LATEST)
    }


}
