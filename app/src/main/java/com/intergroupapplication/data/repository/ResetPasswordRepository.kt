package com.intergroupapplication.data.repository

import com.intergroupapplication.data.mappers.TokenCodeMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.network.dto.CodeDto
import com.intergroupapplication.data.network.dto.EmailDto
import com.intergroupapplication.data.network.dto.NewPasswordDto
import com.intergroupapplication.domain.gateway.ResetPasswordGateway
import javax.inject.Inject

class ResetPasswordRepository @Inject constructor(
    private val api: AppApi,
    private val tokenCodeMapper: TokenCodeMapper
) : ResetPasswordGateway {

    override fun resetPassword(emailDto: EmailDto) =
        api.resetPassword(emailDto)

    override fun resetPasswordCode(codeDto: CodeDto) =
        api.resetPasswordCode(codeDto)
            .map(tokenCodeMapper)

    override fun newPassword(newPasswordDto: NewPasswordDto) =
        api.resetPasswordNewPassword(newPasswordDto)
}
