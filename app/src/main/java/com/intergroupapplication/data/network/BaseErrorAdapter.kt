package com.intergroupapplication.data.network

import com.intergroupapplication.data.network.dto.ApiErrorElement
import com.intergroupapplication.domain.exception.*
import io.reactivex.exceptions.CompositeException
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 24/08/2018 at project InterGroupApplication.
 */
class BaseErrorAdapter @Inject constructor(private val errorParser: ErrorParser) : ErrorAdapter {

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
                                GROUP_IS_BLOCKED -> GroupBlockedException()
                                INVALID_VERSION -> NewVersionException(message)
                                else -> BadRequestException(message)
                            }
                        }
                        it.fieldError.isNotEmpty() -> getListFieldException(it.fieldError)
                        else -> UnknownServerException()
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
                                ALREADY_NOT_FOLLOWING -> GroupAlreadyFollowingException()
                                else -> NotFoundException(message)
                            }
                        }
                        it.fieldError.isNotEmpty() -> NotFoundException(it.nonFieldError?.message)
                        else -> UnknownServerException()
                    }
                    403 -> {
                        when {
                            it.nonFieldError?.uniqueCode?.contains("no_profile") == true -> UserNotProfileException()
                            it.nonFieldError?.uniqueCode?.contains("not_verified") == true -> UserNotVerifiedException()
                            else -> ForbiddenException(it.nonFieldError?.message.orEmpty())
                        }
                    }
                    500 -> ServerException()
                    else -> UnknownServerException()
                }
            } ?: when (throwable.code()) {
                500 -> ServerException()
                404 -> NotFoundException("Не найдено")
                403 -> ForbiddenException("Нет доступа")
                401 -> InvalidRefreshException()
                400 -> NotFoundException(null)
                else -> UnknownServerException()
            }
        } else {
            ConnectionException()
        }
    }

    private fun getListFieldException(apiErrors: List<ApiErrorElement>): Exception =
        if (apiErrors[0].uniqueCode == INVALID_REFRESH) {
            InvalidRefreshException()
        } else {
            CompositeException(apiErrors.map { FieldException(it.field.orEmpty(), it.message) })
        }
}
