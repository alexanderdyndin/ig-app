package com.intergroupapplication.data.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.intergroupapplication.data.network.dto.ApiErrorDto
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 24/08/2018 at project InterGroupApplication.
 */
class BaseErrorParser @Inject constructor(private val gson: Gson) : ErrorParser {

    override fun parseError(httpException: HttpException): ApiErrorDto? {
        val apiError: ApiErrorDto?
        return try {
            apiError = httpException.response()?.errorBody()?.let {
                gson.fromJson(it.string(), ApiErrorDto::class.java)
            }
            apiError
        } catch (e: JsonSyntaxException) {
            Timber.e(e)
            null
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }
}
