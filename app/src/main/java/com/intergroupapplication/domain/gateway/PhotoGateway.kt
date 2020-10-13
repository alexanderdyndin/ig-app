package com.intergroupapplication.domain.gateway

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
    fun uploadToAws(): Observable<Float>
    fun getLastPhotoUrl(): Single<String>
}
