package com.intergroupapplication.data.repository

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.OkHttpResponseListener
import com.androidnetworking.interfaces.UploadProgressListener
import com.intergroupapplication.data.model.ImageUploadDto
import com.intergroupapplication.data.model.PhotoUploadFields
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo
import com.yalantis.ucrop.UCrop
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import com.intergroupapplication.data.network.AmazonApi
import com.intergroupapplication.domain.exception.CanNotUploadPhoto
import com.intergroupapplication.domain.exception.ImageUploadingException
import com.intergroupapplication.domain.gateway.AvatarGateway
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import com.intergroupapplication.presentation.base.ImageUploadingState
import id.zelory.compressor.Compressor
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor


/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class AvatarRepository @Inject constructor(private val context: Context,
                                           private val appApi: AppApi,
                                           private val awsUploadingGateway: AwsUploadingGateway) : AvatarGateway {
    companion object {
        const val CAN_NOT_GET_PICTURE = "Can not get picture"
        const val FULL_UPLOADED_PROGRESS = 100F
    }

   override fun uploadToAws(path: String, groupId: String?): Observable<ImageUploadingState> {
        val subject = PublishSubject.create<ImageUploadingState>()
        val file = File(path)
        return appApi.uploadPhoto(file.extension, groupId)
            .doAfterSuccess {
                if (file.extension == "gif")
                    awsUploadingGateway.uploadAvatarToAws(it.url, subject, it.fields,
                        file)
                else
                    awsUploadingGateway.uploadAvatarToAws(it.url, subject, it.fields,
                        Compressor(context).setQuality(75).setCompressFormat(Bitmap.CompressFormat.WEBP).compressToFile(file))
            }
            .flatMapObservable {
                subject.doOnDispose { AndroidNetworking.cancelAll() }
            }
    }


}
