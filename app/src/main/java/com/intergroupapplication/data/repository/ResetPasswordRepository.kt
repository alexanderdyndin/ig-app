package com.intergroupapplication.data.repository

import com.intergroupapplication.data.mapper.TokenCodeMapper
import com.intergroupapplication.data.model.CodeModel
import com.intergroupapplication.data.model.EmailModel
import com.intergroupapplication.data.model.NewPasswordModel
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.gateway.ResetPasswordGetaway
import javax.inject.Inject

class ResetPasswordRepository @Inject constructor(private val api: AppApi,
                                                  private val tokenCodeMapper: TokenCodeMapper) : ResetPasswordGetaway {

    override fun resetPassword(emailModel: EmailModel) =
            api.resetPassword(emailModel)

    override fun resetPasswordCode(codeModel: CodeModel) =
            api.resetPasswordCode(codeModel)
                    .map { tokenCodeMapper.map(it) }

    override fun newPassword(newPasswordModel: NewPasswordModel) =
            api.resetPasswordNewpassword(newPasswordModel)
}