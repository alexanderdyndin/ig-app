package com.intergroupapplication.domain.exception

import java.lang.Exception

class PageNotFoundException(override val message: String?) : Exception()