package com.intergroupapplication.data.network

import com.intergroupapplication.data.network.dto.ApiErrorDto
import retrofit2.HttpException

/**
 * Created by abakarmagomedov on 24/08/2018 at project InterGroupApplication.
 */
interface ErrorParser {
    fun parseError(httpException: HttpException): ApiErrorDto?
}
