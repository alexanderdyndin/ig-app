package com.intergroupapplication.data.network

import com.intergroupapplication.data.model.ApiErrorElement
import com.intergroupapplication.domain.exception.*
import io.reactivex.exceptions.CompositeException
import retrofit2.HttpException
import javax.inject.Inject
import kotlin.Exception

/**
 * Created by abakarmagomedov on 24/08/2018 at project InterGroupApplication.
 */
class BaseErrorAdapter @Inject constructor(private val errorParser: ErrorParser)
    : ErrorAdapter {

    override fun adapt(throwable: Throwable): Throwable {
        return if (throwable is HttpException) {
            val apiError = errorParser.parseError(throwable)
            apiError?.let {
                when (throwable.code()) {
                    400 -> when {
                        it.nonFieldError != null -> {
                            val message = it.nonFieldError.message
                            when (it.nonFieldError.uniqueCode) {
                                IMEI_BLOCKED -> ImeiException(message)
                                USER_BLOCKED -> UserBlockedException(message)
                                else -> BadRequestException(message)
                            }
                        }
                        it.fieldError.isNotEmpty() -> getListFieldException(it.fieldError)
                        else -> UnknowServerException()
                    }
                    401 -> InvalidRefreshException()
                    404 -> when {
                        it.nonFieldError != null -> {
                            val message = it.nonFieldError.message
                            when (it.nonFieldError.uniqueCode) {
                                PAGE_ERROR_GROUP -> PageNotFoundException("Не найдено")
                                PAGE_ERROR_COMMENT_LIST -> PageNotFoundException("Не найдено")
                                PAGE_ERROR_NEWS -> PageNotFoundException("Не найдено")
                                PAGE_ERROR_GROUPS_POST_LIST -> PageNotFoundException("Не найдено")
                                NOT_PROFILE -> UserNotProfileException()
                                else -> NotFoundException(message)
                            }
                        }
                        it.fieldError.isNotEmpty() -> NotFoundException(it.nonFieldError?.message)
                        else -> UnknowServerException()
                    }
                    403 -> {
                        when {
                            it.nonFieldError?.uniqueCode?.contains("no_profile") == true -> UserNotProfileException()
                            it.nonFieldError?.uniqueCode?.contains("not_verified") == true -> UserNotVerifiedException()
                            else -> ForbiddenException(it.nonFieldError?.message.orEmpty())
                        }
                    }
                    500 -> ServerException()
                    else -> UnknowServerException()
                }
            } ?: when (throwable.code()) {
                500 -> ServerException()
                else -> UnknowServerException()
            }
        } else {
            throwable
        }
    }

    private fun getListFieldException(apiErrors: List<ApiErrorElement>): Exception =
            if (apiErrors[0].uniqueCode == INVALID_REFRESH) {
                InvalidRefreshException()
            } else {
                CompositeException(apiErrors.map { FieldException(it.field.orEmpty(), it.message) })
            }
}