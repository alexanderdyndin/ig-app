package com.intergroupapplication.domain.exception

data class FieldException(val field: String, override val message: String?) : Exception()