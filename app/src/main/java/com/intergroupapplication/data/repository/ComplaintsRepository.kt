package com.intergroupapplication.data.repository

import com.intergroupapplication.data.model.ComplaintModel
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.gateway.ComplaintsGateway
import javax.inject.Inject

class ComplaintsRepository @Inject constructor(private val api: AppApi) : ComplaintsGateway {

    override fun complaintPost(id: Int) =
            api.complaints(ComplaintModel(post = id))

    override fun complaintComment(id: Int) =
            api.complaints(ComplaintModel(comment = id))
}