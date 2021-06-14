package com.intergroupapplication.data.repository

import android.app.Activity
import android.graphics.Bitmap
import android.webkit.MimeTypeMap
import com.androidnetworking.AndroidNetworking
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.model.ImageUploadDto
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo
import com.yalantis.ucrop.UCrop
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import com.intergroupapplication.domain.gateway.AwsUploadingGateway
import id.zelory.compressor.Compressor
import timber.log.Timber
import java.io.File


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

    private val imageUrls: MutableList<ChooseMedia> = mutableListOf()
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
                        lastAttachedImagePath = path
                        lastPhotoUrl = ""
                        if (path != null)
                            imagePaths.add(path)
                        path ?: ""
                    }

    override fun getLastPhotoUrl(): Single<String> = Single.fromCallable { lastPhotoUrl }
    
    override fun getVideoUrls(): Single<List<ChooseMedia>> = Single.fromCallable { videoUrls }

    override fun getAudioUrls(): Single<List<ChooseMedia>> = Single.fromCallable { audioUrls }

    override fun getImageUrls(): Single<List<ChooseMedia>> {
        return Single.fromCallable { imageUrls }
    }

    override fun setVideoUrls(videos: List<ChooseMedia>) {
        videoUrls.addAll(videos)
        videos.forEach {
            fileToUrl[it.url] = it.url
        }
    }

    override fun setAudioUrls(audios: List<ChooseMedia>) {
        audioUrls.addAll(audios)
        audios.forEach {
            fileToUrl[it.url] = it.url
        }
    }

    override fun setImageUrls(images: List<ChooseMedia>) {
        imageUrls.addAll(images)
        images.forEach {
            fileToUrl[it.url] = it.url
        }
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
                        val path = response.data()?.file?.path
                        path?.let {
                            imagePaths.add(it)
                            listOf(it)
                        } ?: emptyList()
                    }

    private var count = 0
    override fun uploadAudioToAws(chooseMedia: ChooseMedia, groupId: String?,
                                  upload: (imageExs: String, id: String?) -> Single<ImageUploadDto>)
                                : Observable<Float> {
        val subject = PublishSubject.create<Float>()
        val file = File(chooseMedia.url)
        val myFile = File(activity.externalCacheDir,"upload_music_$count.mp3")
        myFile.createNewFile()
        val byteArray = file.readBytes()
        myFile.writeBytes(byteArray)
        count++
        return upload(myFile.extension, groupId)
                .doAfterSuccess {
                    awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields, myFile)
                }
                .flatMapObservable { it ->
                    subject.doOnDispose {
                      //  AndroidNetworking.cancelAll()
                    }
                            .doOnComplete {
                                audioUrls.add(ChooseMedia(it.fields.key,name = chooseMedia.name,
                                authorMusic = chooseMedia.authorMusic,duration = chooseMedia.duration ))
                                fileToUrl[chooseMedia.url] = it.fields.key
                                myFile.delete()
                            }
                    //.doOnError { lastPhotoUrl = "" }
                }
    }

    override fun uploadVideoToAws(chooseMedia: ChooseMedia, groupId: String?, upload: (imageExs: String, id: String?) -> Single<ImageUploadDto>): Observable<Float> {
        val subject = PublishSubject.create<Float>()
        val file = File(chooseMedia.url)
        return upload(file.extension, groupId)
                .doAfterSuccess {
                    awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields, file)
                }
                .flatMapObservable { it ->
                    subject.doOnDispose {
                        //AndroidNetworking.cancelAll()
                            }
                            .doOnComplete {
                                videoUrls.add(ChooseMedia(it.fields.key,urlPreview = chooseMedia.urlPreview
                                        ,duration = chooseMedia.duration,
                                        name = chooseMedia.url.substringAfterLast("/")))
                                fileToUrl[chooseMedia.url] = it.fields.key
                            }
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
                    else{
                            try {
                                awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields,
                                        Compressor(activity).setQuality(75).setCompressFormat(Bitmap.CompressFormat.WEBP).compressToFile(file))
                            }catch (e:Exception){
                                awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields,
                                        file)
                            }
                        }
                }
                .flatMapObservable { it ->
                    subject.doOnDispose {
                        //AndroidNetworking.cancelAll()
                    }
                            .doOnComplete {
                                imageUrls.add(ChooseMedia(url = it.fields.key,
                                        name = path.substringAfterLast("/")))
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
                    else{
                        try {
                            awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields,
                                    Compressor(activity).setQuality(75).setCompressFormat(Bitmap.CompressFormat.WEBP)
                                            .compressToFile(file))
                        }catch (e:Exception){
                            awsUploadingGateway.uploadImageToAws(it.url, subject, it.fields,
                                    file)
                        }
                    }
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

    override fun removeContent(path:String) {
        val type = MimeTypeMap.getFileExtensionFromUrl(path)
        when (MimeTypeMap.getSingleton().getMimeTypeFromExtension(type) ?: "") {
            in listOf("audio/mpeg", "audio/aac", "audio/wav") ->{
                audioUrls.removeMedia(fileToUrl[path]).toString()
            }
            in listOf("video/mpeg", "video/mp4", "video/webm", "video/3gpp") -> videoUrls.removeMedia(fileToUrl[path])
            else ->  {
                imageUrls.removeMedia(fileToUrl[path])
                audioUrls.removeMedia(fileToUrl[path])
                videoUrls.removeMedia(fileToUrl[path])
            }
        }
    }

    override fun removeAllContent() {
        imageUrls.clear()
        audioUrls.clear()
        videoUrls.clear()
    }

    private fun MutableList<ChooseMedia>.removeMedia(url: String?):Boolean {
        this.forEach {
            if (it.url == url) {
                remove(it)
                return true
            }
        }
        return false
    }
}
