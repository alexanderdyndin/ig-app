package com.intergroupapplication.domain.exception

class ForbiddenException(
    override val message: String?
) : Exception(message)
