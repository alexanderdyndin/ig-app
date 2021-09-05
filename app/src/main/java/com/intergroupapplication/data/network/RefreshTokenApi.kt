package com.intergroupapplication.data.network

import com.intergroupapplication.data.model.RefreshTokenModel
import com.intergroupapplication.data.model.TokenModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by abakarmagomedov on 24/09/2018 at project InterGroupApplication.
 */
interface RefreshTokenApi {
    @POST("api/auth/refresh/")
    fun refreshAccessToken(@Body refreshToken: RefreshTokenModel): Call<TokenModel>
}
