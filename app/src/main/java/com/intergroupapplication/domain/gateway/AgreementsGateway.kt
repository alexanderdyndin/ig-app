package com.intergroupapplication.domain.gateway

import io.reactivex.Single

interface AgreementsGateway {
    fun getPrivacy(): Single<String>
    fun getTerms(): Single<String>
    fun getRightHolders(): Single<String>
}