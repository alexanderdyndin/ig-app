package com.intergroupapplication.domain.gateway

import io.reactivex.Completable

interface ComplaintsGateway {

    fun complaintPost(id: Int): Completable

    fun complaintComment(id: Int): Completable
}
