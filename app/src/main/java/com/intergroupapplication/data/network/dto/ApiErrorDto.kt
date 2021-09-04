package com.intergroupapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class ApiErrorDto(
    @SerializedName("non_field_errors")
        val nonFieldError: ApiErrorElement?,
    @SerializedName("field_errors")
        val fieldError: List<ApiErrorElement>
)

data class ApiErrorElement(
        @SerializedName("field")
        val field: String?,
        @SerializedName("message")
        val message: String,
        @SerializedName("dev_message")
        val devMessage: String,
        @SerializedName("unique_code")
        val uniqueCode: String
)
