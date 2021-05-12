package com.intergroupapplication.data.repository

import android.app.Activity
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
import com.intergroupapplication.data.model.ChooseMedia
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
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import id.zelory.compressor.Compressor
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.Response
import timber.log.Timber
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor


/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
class PhotoRepository @Inject constructor(private val activity: Activity,
                                          private val cropOptions: UCrop.Options,
                                          private val appApi: AppApi,
                                          private val awsUploadingGateway: AwsUploadingGateway) : PhotoGateway {
    companion object {
        const val CAN_NOT_GET_PICTURE = "Can not get picture"
    }

    private var lastAttachedImagePath: String? = null
    private var lastPhotoUrl: String = ""

    private val imagePaths: MutableList<String> = mutableListOf()
    private val videoPaths: MutableList<String> = mutableListOf()
    private val audioPaths: MutableList<String> = mutableListOf()

    private val imageUrls: MutableList<String> = mutableListOf()
    private val videoUrls: MutableList<ChooseMedia> = mutableListOf()
    private val audioUrls: MutableList<ChooseMedia> = mutableListOf()

    private val fileToUrl: MutableMap<String, String> = mutableMapOf()

    override fun loadFromGallery(): Observable<String> =
            RxPaparazzo.single(activity)
                    .crop(cropOptions)
                    .usingGallery()
                    .map {
                        val path = it.data()?.file?.path
                        lastAttachedImagePath = path
                        lastPhotoUrl = ""
                        path ?: ""
                    }

    override fun loadFromCamera(): Observable<String> =
            RxPaparazzo.single(activity)
                    .crop(cropOptions)
                    .usingCamera()
                    .map {
                        val path = it.data()?.file?.path
                        Timber.tag("tut_response").d(path)
                        lastAttachedImagePath = path
                        lastPhotoUrl = ""
                        if (path != null)
                            imagePaths.add(path)
                        path ?: ""
                    }

    override fun getLastPhotoUrl(): Single<String> = Single.fromCallable { lastPhotoUrl }
    
    override fun getVideoUrls(): Single<List<ChooseMedia>> = Single.fromCallable { videoUrls }

    override fun getAudioUrls(): Single<List<ChooseMedia>> = Single.fromCallable { audioUrls }

    override fun getImageUrls(): Single<List<String>> {
        return Single.fromCallable { imageUrls }
    }

    override fun loadAudio(): Observable<List<String>> =
        RxPaparazzo.single(activity)
                .setMultipleMimeType("audio/mpeg", "audio/aac", "audio/wav")
                .useInternalStorage()
                .useDocumentPicker()
                .usingFiles()
                .map { response ->
//                    val paths = response.data()?.map { it.file.path } ?: emptyList()
//                    audioPaths.addAll(paths)
//                    paths
                    val path = response.data()?.file?.path
                    path?.let {
                        videoPaths.add(it)
                        listOf(it)
                    } ?: emptyList()
                }

    override fun loadVideo(): Observable<List<String>> =
        RxPaparazzo.single(activity)
                .setMultipleMimeType("video/mpeg", "video/mp4", "video/webm", "video/3gpp")
                .useInternalStorage()
                .useDocumentPicker()
                .usingFiles()
                .map { response ->
//                    val paths = response.data()?.map { it.file.path } ?: emptyList()
//                    videoPaths.addAll(paths)
//                    paths
                    val path = response.data()?.file?.path
                    path?.let {
                        videoPaths.add(it)
                        listOf(it)
                    } ?: emptyList()
                }

    override fun loadImagesFromGallery(): Observable<List<String>> =
            RxPaparazzo.single(activity)
                    .crop(cropOptions)
                    .usingGallery()
                    .map { response ->
                        //val paths = response.data()?.map { it.file.path } ?: emptyList()
                        //imagePaths.addAll(paths)
                        val path = response.data()?.file?.path
                        path?.let {
                            imagePaths.add(it)
                            listOf(it)
                        } ?: emptyList()
                    }

    override fun uploadAudioToAws(path: String,trackName:String,author:String, groupId: String?,
                                  upload: (imageExs: String, id: String?) -> Single<ImageUploadDto>)
                                : Observable<Float> {
        val subject = PublishSubject.create<Float>()
        val file = File(path)
        val myFile = File(activity.externalCacheDir,"temporary_file_with_music.${file.extension}")
        myFile.createNewFile()
        val byteArray = file.readBytes()
        myFile.writeBytes(byteArray)
        Timber.tag("tut_path").d(myFile.absolutePath)
        return upload(myFile.extension, groupId)
                .doAfterSuccess {
                    awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields, myFile)
                }
                .flatMapObservable { it ->
                    subject.doOnDispose { AndroidNetworking.cancelAll() }
                            .doOnComplete {
                                audioUrls.add(ChooseMedia(it.fields.key,trackName = trackName,
                                        authorMusic = author))
                                fileToUrl[path] = it.fields.key
                                myFile.delete()
                            }
                    //.doOnError { lastPhotoUrl = "" }
                }
    }

    override fun uploadVideoToAws(path: String, urlPreview: String, groupId: String?, upload: (imageExs: String, id: String?) -> Single<ImageUploadDto>): Observable<Float> {
        val subject = PublishSubject.create<Float>()
        val file = File(path)
        return upload(file.extension, groupId)
                .doAfterSuccess {
                    awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields, file)
                }
                .flatMapObservable { it ->
                    subject.doOnDispose { AndroidNetworking.cancelAll() }
                            .doOnComplete {
                                videoUrls.add(ChooseMedia(it.fields.key,urlPreview))
                                fileToUrl[path] = it.fields.key
                            }
                    //.doOnError { lastPhotoUrl = "" }
                }
    }

    override fun uploadImageToAws(path: String, groupId: String?, upload: (imageExs: String, id: String?) -> Single<ImageUploadDto>): Observable<Float> {
        val subject = PublishSubject.create<Float>()
        val file = File(path)
        return upload(file.extension, groupId)
                .doAfterSuccess {
                    if (file.extension == "gif")
                        awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields,
                                file)
                    else
                        awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields,
                                Compressor(activity).setQuality(75).setCompressFormat(Bitmap.CompressFormat.WEBP).compressToFile(file))
                }
                .flatMapObservable { it ->
                    subject.doOnDispose { AndroidNetworking.cancelAll() }
                            .doOnComplete {
                                imageUrls.add(it.fields.key)
                                fileToUrl[path] = it.fields.key
                            }
                    //.doOnError { lastPhotoUrl = "" }
                }
    }

    override fun uploadImage(path: String, groupId: String?, upload: (imageExs: String, id: String?) -> Single<ImageUploadDto>): Observable<String> {
        val subject = PublishSubject.create<Float>()
        val file = File(path)
        return upload(file.extension, groupId)
                .doAfterSuccess {
                    if (file.extension == "gif")
                        awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields,
                                file)
                    else
                        awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields,
                                Compressor(activity).setQuality(75).setCompressFormat(Bitmap.CompressFormat.WEBP).compressToFile(file))
                }
                .flatMapObservable { it ->
                    subject.doOnDispose { AndroidNetworking.cancelAll() }
                            .doOnComplete{
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                    return@flatMapObservable Observable.just(it.fields.key)
                }
    }

    override fun uploadToAws(groupId: String?): Observable<Float> {
        val subject = PublishSubject.create<Float>()
        val file = File(lastAttachedImagePath)
        return appApi.uploadPhoto(file.extension, groupId)
                .doAfterSuccess {
                    if (file.extension == "gif")
                        awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields,
                                file)
                    else
                        awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields,
                                Compressor(activity).setQuality(75).setCompressFormat(Bitmap.CompressFormat.WEBP).compressToFile(file))
                }
                .flatMapObservable { it ->
                    subject.doOnDispose { AndroidNetworking.cancelAll() }
                            .doOnComplete {
                                lastPhotoUrl = it.fields.key
                            }
                            .doOnError { lastPhotoUrl = "" }
                }
    }
    override fun uploadAvatarUser(groupId: String?): Observable<Float> {
        val subject = PublishSubject.create<Float>()
        val file = File(lastAttachedImagePath)
        return appApi.uploadUserAvatar(file.extension, groupId)
                .doAfterSuccess {
                    if (file.extension == "gif")
                        awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields,
                                file)
                    else
                        awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields,
                                Compressor(activity).setQuality(75).setCompressFormat(Bitmap.CompressFormat.WEBP).compressToFile(file))
                }
                .flatMapObservable { it ->
                    subject.doOnDispose { AndroidNetworking.cancelAll() }
                            .doOnComplete {
                                lastPhotoUrl = it.fields.key }
                            .doOnError { lastPhotoUrl = "" }
                }
    }

    override fun uploadAvatarGroup(groupId: String?): Observable<Float> {
        val subject = PublishSubject.create<Float>()
        val file = File(lastAttachedImagePath)
        return appApi.uploadGroupAvatar(file.extension, groupId)
                .doAfterSuccess {
                    if (file.extension == "gif")
                        awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields,
                                file)
                    else
                        awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields,
                                Compressor(activity).setQuality(75).setCompressFormat(Bitmap.CompressFormat.WEBP).compressToFile(file))
                }
                .flatMapObservable { it ->
                    subject.doOnDispose { AndroidNetworking.cancelAll() }
                            .doOnComplete {
                                lastPhotoUrl = it.fields.key }
                            .doOnError { lastPhotoUrl = "" }
                }
    }

    override fun removeContent(path: String) {
        val type = MimeTypeMap.getFileExtensionFromUrl(path)
        when (MimeTypeMap.getSingleton().getMimeTypeFromExtension(type) ?: "") {
            in listOf("audio/mpeg", "audio/aac", "audio/wav") -> audioUrls.remove(fileToUrl[path])
            in listOf("video/mpeg", "video/mp4", "video/webm", "video/3gpp") -> videoUrls.remove(fileToUrl[path])
            else ->  {
                imageUrls.remove(fileToUrl[path])
                audioUrls.remove(fileToUrl[path])
                videoUrls.remove(fileToUrl[path])
            }
        }
    }

    override fun removeAllContent() {
        imageUrls.clear()
        audioUrls.clear()
        videoUrls.clear()
    }
}
