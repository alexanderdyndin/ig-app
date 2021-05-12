package com.intergroupapplication.domain.gateway

import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.model.ImageUploadDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
interface PhotoGateway {
    fun loadFromGallery(): Observable<String>
    fun loadFromCamera(): Observable<String>
    fun uploadToAws(groupId: String? = null): Observable<Float>
    fun uploadAvatarUser(userId: String? = null): Observable<Float>
    fun uploadAvatarGroup(groupId: String? = null): Observable<Float>
    fun getLastPhotoUrl(): Single<String>
    fun getVideoUrls(): Single<List<ChooseMedia>>
    fun getAudioUrls(): Single<List<ChooseMedia>>
    fun getImageUrls(): Single<List<String>>
    fun loadAudio(): Observable<List<String>>
    fun loadVideo(): Observable<List<String>>
    fun loadImagesFromGallery(): Observable<List<String>>
    fun uploadAudioToAws(path: String,trackName:String,author:String, groupId: String? = null,
                         upload:(imageExs:String,id:String?)-> Single<ImageUploadDto>): Observable<Float>
    fun uploadVideoToAws(path: String,urlPreview:String, groupId: String? = null,
                         upload:(imageExs:String,id:String?)-> Single<ImageUploadDto>): Observable<Float>
    fun uploadImageToAws(path: String, groupId: String? = null,
                         upload:(imageExs:String,id:String?)-> Single<ImageUploadDto>): Observable<Float>
    fun uploadImage(path: String, groupId: String? = null,
                    upload:(imageExs:String,id:String?)-> Single<ImageUploadDto>): Observable<String>
    fun removeContent(path: String)
    fun removeAllContent()
}
