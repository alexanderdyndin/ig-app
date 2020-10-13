package com.intergroupapplication.domain.gateway

import io.reactivex.Completable

interface ComplaintsGetaway {

    fun complaintPost(id: Int): Completable

    fun complaintComment(id: Int): Completable

}