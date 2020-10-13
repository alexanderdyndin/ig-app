package com.intergroupapplication.domain.exception

data class BadRequestException(override val message: String) : Exception()