package com.intergroupapplication.domain.exception

import java.lang.Exception

data class FieldException(val field: String, override val message: String?) : Exception()