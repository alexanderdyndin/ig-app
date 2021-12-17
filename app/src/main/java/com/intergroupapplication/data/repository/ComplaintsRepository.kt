package com.intergroupapplication.data.repository

import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.network.dto.ComplaintDto
import com.intergroupapplication.domain.gateway.ComplaintsGateway
import javax.inject.Inject

class ComplaintsRepository @Inject constructor(private val api: AppApi) : ComplaintsGateway {

    override fun complaintPost(id: Int) =
        api.complaints(ComplaintDto(post = id))

    override fun complaintComment(id: Int) =
        api.complaints(ComplaintDto(comment = id))
}
