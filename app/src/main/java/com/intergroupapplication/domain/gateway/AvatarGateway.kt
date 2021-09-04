package com.intergroupapplication.domain.gateway

import com.intergroupapplication.presentation.base.ImageUploadingState
import io.reactivex.Observable

/**
 * Created by abakarmagomedov on 03/08/2018 at project InterGroupApplication.
 */
interface AvatarGateway {
    fun uploadToAws(path: String, groupId: String? = null): Observable<ImageUploadingState>
}
