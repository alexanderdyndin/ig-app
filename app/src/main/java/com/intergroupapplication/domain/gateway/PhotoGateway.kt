package com.intergroupapplication.domain.gateway

import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.model.ImageUploadDto
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
    fun getImageUrls(): Single<List<ChooseMedia>>
    fun setVideoUrls(videos:List<ChooseMedia>)
    fun setAudioUrls(audios:List<ChooseMedia>)
    fun setImageUrls(images:List<ChooseMedia>)
    fun uploadAudioToAws(chooseMedia: ChooseMedia, groupId: String? = null,
                         upload:(imageExs:String,id:String?)-> Single<ImageUploadDto>): Observable<Float>
    fun uploadVideoToAws(chooseMedia: ChooseMedia, groupId: String? = null,
                         upload:(imageExs:String,id:String?)-> Single<ImageUploadDto>): Observable<Float>
    fun uploadImageToAws(path: String, groupId: String? = null,
                         upload:(imageExs:String,id:String?)-> Single<ImageUploadDto>): Observable<Float>
    fun uploadImage(path: String, groupId: String? = null,
                    upload:(imageExs:String,id:String?)-> Single<ImageUploadDto>): Observable<String>
    fun removeContent(chooseMedia: ChooseMedia)
    fun removeAllContent()
}
